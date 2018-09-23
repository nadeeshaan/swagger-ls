/*
 * Copyright (c) 2018, Nadeeshaan Gunasinghe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swagger.langserver.completion;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import joptsimple.internal.Strings;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilities to parse content.
 */
public class ContentParserUtil {

    private ContentParserUtil() {
    }

    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    private static final String SWAGGER_MODEL_PACKAGE = "io.swagger.models";
    
    private static Iterable<Node> getYAMLNodeTree(String content) {
        Constructor constructor = new Constructor();
        Yaml yaml = new Yaml(constructor);
        return yaml.composeAll(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));
    }

    /**
     * Mapping node where the cursor belongs to.
     *
     * @param line              Cursor Line
     * @param content           Document Content to Parse
     */
    public static void getMappingNodeForCursor(int line, String content) {
        Iterable<Node> nodes = getYAMLNodeTree(content);
        nodes.forEach(node -> {
            if (node instanceof MappingNode) {
                ((MappingNode) node).getValue().forEach(nodeTuple -> {
                    // TODO: Need Implementation
                });
            }
        });
    }

    /**
     * Get the modified document content.
     * 
     * Note: Here replace the line content at the cursor with spaces to avoid parser issues
     * 
     * @param fileUri           Document uri
     * @param cursorLine        Current cursor line
     * @return {@link String}   Modified content
     * @throws IOException      IOException if the file read fails
     */
    public String modifyContent(String fileUri, int cursorLine) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(URI.create(fileUri))));
        String[] lines = content.split("\\r?\\n");
        lines[cursorLine] = lines[cursorLine].replaceAll("\\w", " ");
        return Strings.join(lines, LINE_SEPARATOR);
    }
    
    public static List<CompletionItem> getCompletions(String content, Position position) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Deque<String> fieldStack = new ArrayDeque<>();
        SwaggerDeserializationResult swaggerDeserializationResult = new SwaggerParser().readWithInfo(content);
        Constructor constructor = new Constructor();
        Yaml yaml = new Yaml(constructor);
        Iterable<Node> iterable = yaml.composeAll(new InputStreamReader(new ByteArrayInputStream(content.getBytes())));
        iterable.forEach(o -> {
            FieldIdentifier fieldIdentifier = new FieldIdentifier(position.getLine(), position.getCharacter());
            fieldIdentifier
                    .calculateFieldStack(((MappingNode) o).getValue());
            if (!fieldIdentifier.getFieldStack().isEmpty()) {
                fieldStack.addAll(fieldIdentifier.getFieldStack());
            }
        });
        Swagger swagger = swaggerDeserializationResult.getSwagger();
        List<String> fields = new ArrayList<>(fieldStack);
        Collections.reverse(fields);
        List<String> completions = getCompletionFields(swagger, fields);
        return completions.stream().map(field -> {
            CompletionItem completionItem = new CompletionItem();
            completionItem.setInsertText(field);
            completionItem.setLabel(field);
            completionItem.setKind(CompletionItemKind.Field);
            completionItem.setDetail(field);
            return completionItem;
        }).collect(Collectors.toList());
    }

    private static List<String> getCompletionFields(Swagger swagger, List<String> fieldStack)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class cls = swagger.getClass();
        Object invokeAgainst = swagger;

        for (int i = 0; i < fieldStack.size(); i++) {
            String field = fieldStack.get(i);
            Method m = cls.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
            cls = m.getReturnType();
            
            if (cls.equals(Map.class)) {
                LinkedHashMap invokeResult = (LinkedHashMap) m.invoke(invokeAgainst);
                if (fieldStack.size() == i + 1) {
                    break;
                }
                invokeAgainst = invokeResult.get(fieldStack.get(++i));
                cls = invokeAgainst.getClass();
            } else {
                invokeAgainst = m.invoke(invokeAgainst);
            }
        }

        Field[] fields = cls.getPackage().getName().startsWith(SWAGGER_MODEL_PACKAGE)
                ? cls.getDeclaredFields()
                : new Field[0];  

        return Arrays.stream(fields)
                .filter(field -> !field.getName().equalsIgnoreCase("vendorExtensions"))
                .map(Field::getName).collect(Collectors.toList());
    }
}
