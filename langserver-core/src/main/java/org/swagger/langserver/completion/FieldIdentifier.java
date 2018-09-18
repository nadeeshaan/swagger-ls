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

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Go through the yml config to identify the field stack where the cursor belongs.
 */
class FieldIdentifier {

    private Deque<String> fieldStack = new ArrayDeque<>();
    
    private boolean terminateVisit = false;

    void calculateFieldStack(List<NodeTuple> tuples, int cursorLine, int cursorCol, int parentIndentation) {
        for (NodeTuple tuple : tuples) {
            String key = ((ScalarNode) tuple.getKeyNode()).getValue();
            int line = tuple.getKeyNode().getStartMark().getLine();
            int col = tuple.getKeyNode().getStartMark().getColumn();

            if (cursorLine <= line) {
                if (cursorCol <= parentIndentation) {
                    this.fieldStack.pop();
                }
                this.terminateVisit = true;
                break;
            } else if (tuple.getValueNode() instanceof MappingNode && cursorCol > col) {
                this.fieldStack.push(key);
                this.calculateFieldStack(((MappingNode) tuple.getValueNode()).getValue(), cursorLine, cursorCol, col);
            }
            
            if (!terminateVisit && tuples.indexOf(tuple) == tuples.size() - 1 && !fieldStack.isEmpty()) {
                this.fieldStack.pop();
            }
        }
    }

    Deque<String> getFieldStack() {
        return fieldStack;
    }
}
