/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';

import * as path from 'path';

import { workspace, Disposable, ExtensionContext } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient';

const main: string = 'StdioLauncher';

export function activate(context: ExtensionContext) {

	const { JAVA_HOME } = process.env;
	// in windows class path seperated by ';'
	const sep = process.platform === 'win32' ? ';' : ':';
	const customClassPath: string | undefined = workspace.getConfiguration('swaggerls').get('classpath');
	console.log(`Custom Class Path: ${customClassPath}`);
	console.log(`Using java from JAVA_HOME: ${JAVA_HOME}`);
	let excecutable : string = path.join(JAVA_HOME, 'bin', 'java');
	
	// let jarPath = path.join(__dirname, '..', 'launcher', 'ls-launcher.jar');
	let classPath = path.join(__dirname, '..', 'launcher', 'ls-launcher.jar');
	if(customClassPath) {
		classPath = classPath + sep + customClassPath;
	}
	const args: string[] = ['-cp', classPath];
	console.log(`Custom Class Path: ${customClassPath}`);

	if (process.env.LSDEBUG === "true") {
        console.log('LSDEBUG is set to "true". Services will run on debug mode');
        args.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y');
    }
	
	let serverOptions: ServerOptions = {
		command: excecutable,
		args: [...args, main],
		options: {}
	};
	
	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{scheme: 'file', language: 'yaml'}]
	};
	
	// Create the language client and start the client.
	let disposable = new LanguageClient('swaggerLS', 'Swagger Language Server', serverOptions, clientOptions).start();
	
	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	context.subscriptions.push(disposable);
}
