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

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.swagger.langserver.SwaggerLanguageServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * STDIO Launcher to launch the Language Server.
 */
public class StdioLauncher {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(Level.OFF);
        
        startServer(System.in, System.out);
    }

    private static void startServer(InputStream in, OutputStream out)
            throws InterruptedException, ExecutionException {
        SwaggerLanguageServer server = new SwaggerLanguageServer();
        Launcher<LanguageClient> l = LSPLauncher.createServerLauncher(server, in, out);
        LanguageClient client = l.getRemoteProxy();
        server.connect(client);
        Future<?> startListening = l.startListening();
        startListening.get();
    }
}
