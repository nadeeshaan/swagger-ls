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
    
    private int cursorLine = -1;
    
    private int cursorCol = -1;

    FieldIdentifier(int cursorLine, int cursorCol) {
        this.cursorLine = cursorLine;
        this.cursorCol = cursorCol;
    }

    void calculateFieldStack(List<NodeTuple> tuples) {
        for (NodeTuple tuple : tuples) {
            if (terminateVisit) {
                // During the recursive visit, terminateVisitor is set to true when the desired scope identified
                break;
            }
            String key = ((ScalarNode) tuple.getKeyNode()).getValue();
            int line = tuple.getKeyNode().getStartMark().getLine();

            if (cursorLine < line) {
                // If the cursor is before the evaluating token then break the iteration
                this.terminateVisit = true;
                break;
            } else if (this.withinObjectNode(tuples, tuple)) {
                /*
                    If the given tuple is a mapping node and the cursor is within the mapping node then add the
                    mapping node key to field stack.
                    After traversing all the members of the mapping node remove the added entry to the field stack if 
                    necessary (During the last item evaluation).
                 */
                this.fieldStack.push(key);
                /*
                    If at least one field is not within the current field, it is a scalar node and during the check
                    for mapping node, will set the visitor termination
                 */
                if (terminateVisit) {
                    break;
                }
                this.calculateFieldStack(((MappingNode) tuple.getValueNode()).getValue());
            }

            if (!terminateVisit && tuples.indexOf(tuple) == tuples.size() - 1) {
                this.terminateVisit = true;
            }
        }
    }
    
    private boolean withinObjectNode(List<NodeTuple> tuples, NodeTuple tuple) {
        /*
            If the cursor within the mapping node, need to check whether cursor col is inside the tuple and also
            nee to check the cursor line within the tuple. Since the tupl
         */
        int tupleNodeEndLine = tuple.getValueNode().getEndMark().getLine();
        int tupleNodeStartCol = tuple.getKeyNode().getStartMark().getColumn();
        /*
            If the value node is a scalar node then, you are writing the first field within the object node (YAML parser
            cannot identify) and handle the visitor termination.
            ex: 
            swagger: "2.0"
            info:
              title: Simple API overview
              version: v2
              contact:
                name: API Support
              license:
                <cursor>
            servers:
         */
        if (tuple.getValueNode() instanceof ScalarNode && this.withinScalarNodeAsObject(tuples, tuple)) {
            this.terminateVisit = true;
            return true;
        }
        return tuple.getValueNode() instanceof MappingNode
                && this.cursorCol > tupleNodeStartCol
                && this.cursorLine <= tupleNodeEndLine;
    }
    
    private boolean withinScalarNodeAsObject(List<NodeTuple> tuples, NodeTuple tuple) {
        /*
            check the cases of
            info:
                license
                    <cursor>
                    
            AND
            
            info:
                license
                    <cursor>
                contact
            
            respectively
         */
        int nodeStartCol = tuple.getKeyNode().getStartMark().getColumn();
        return tuples.indexOf(tuple) == tuples.size() - 1 && this.cursorCol > nodeStartCol
                || (tuples.indexOf(tuple) < tuples.size() - 1
                && this.cursorLine < tuples.get(tuples.indexOf(tuple) + 1).getKeyNode().getStartMark().getLine()
                && this.cursorCol > nodeStartCol);

    }

    Deque<String> getFieldStack() {
        return fieldStack;
    }
}
