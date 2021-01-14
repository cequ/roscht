# Roscht Server

Simple Web-Server with CRUD Options for text files (HTML, JSON, plain text) and primitive permission checks.

Use it like:
1. Start the Roscht Server for the first time (uses default IP of the computer)
2. Hash a string (see CLI)
3. Enter hash and a endpoint name into created `root/endpointDataBase.json` like `{"<ENDPOINT NAME>":"<HASH>"}`
4. Shut down Server (see CLI)
5. Start Roscht Server again
6. Place any text files and directories in `root`
7. Open HTML files in `root` like `http://<DEFAULT IP>/file.html`
8. Use XMLHttpRequests from those HTML files in `root` to perform XMLHttpRequests like below

```
var xhr = new XMLHttpRequest();
xhr.open("PUT", window.location.origin + "/newFile.json", true);
xhr.send(JSON.stringify({"roscht":{"endpoint":"<ENDPOINT NAME>", "secret":"<STRING THAT WAS HASHED>"}, "data": {}}));
```