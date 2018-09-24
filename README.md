## Swagger Language Server
**_(Swagger Language Server is currently work in progress_)**

This is a language server implementation for swagger scripting. Swagger LS supports the LSP v3.7.0

### Supported LSP Features
- Completion

### Build from source

**Prerequisites**
- **Java 1.8** and set **JAVA_HOME** (Will be include the capability to configure through settings in the future)
- **Maven v3.5.3** at least
- **npm 5.6.0** at least

**How to build**
- Execute **mvn clean install** from the project root. This will create the server launcher at <PROJECT_ROOT>/client/launcher
- Go to *<PROJECT_ROOT>/client* and execute command **npm install** and then **npm run package**. This will build the **.vsix** VSCode extention under **_<PROJECT_ROOT>/client_**
- Now you can install the generated extension as usual.

### Editor Configurations
In order to get the auto completion on typing, add the following user settings to **settings.json**

``"editor.quickSuggestions": {
    "other": true,
    "comments": false,
    "strings": true
}``

In order to disable the word based suggestions add the following,

``"editor.wordBasedSuggestions": false``

