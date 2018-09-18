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
package org.swagger.langserver;

import java.nio.file.Path;

/**
 * Document Manager is responsible for maintaining the content of the documents.
 */
public interface DocumentManager {
    /**
     * Checks whether the given file is open in workspace.
     *
     * @param filePath Path of the file
     * @return True if the given file is open
     */
    boolean isFileOpen(Path filePath);

    /**
     * Opens the given file in document manager.
     *
     * @param filePath Path of the file
     * @param content  Content of the file
     */
    void openFile(Path filePath, String content);

    /**
     * Updates given file in document manager with new content.
     *
     * @param filePath       Path of the file
     * @param updatedContent New content of the file
     */
    void updateFile(Path filePath, String updatedContent);

    /**
     * Close the given file in document manager.
     *
     * @param filePath Path of the file
     */
    void closeFile(Path filePath);

    /**
     * Gets uptodate content of the file.
     *
     * @param filePath Path of the file
     * @return Content of the file
     */
    String getFileContent(Path filePath);
}
