# Telemetry Prototype

Prototype for the University of Edinburgh Hyperloop team that connects a server and a client over a TCP connection

## How to run
This project uses gradle as its build system. The gradle wrapper is already checked into this repo, so no need to explicitly download gradle (unless you want to).

#### Build project:
```
./gradlew build
```
(If on windows use `gradlew.bat` instead of `./gradlew`)

#### Start up spring server:
```
./gradlew bootRun
```

Go to `localhost:8080` and click the 'connect' button to start the websocket connection between the browser and the spring server.

#### Run client side: In another terminal window, run
```
./build/exe/main/main
```
(there's a `runClient` task in `build.gradle` but for some reason doing this only prints client output once the program ends, which isn't so useful)

#### Start data communication:
Now click 'start pulling data' back on the frontend to start the data communication.

---

Be sure to make a `/temp` directory in your project's root directory, as this is where the log is stored. Read log with `tail -F temp/server_log.log`

Also don't forget to compile the protobuf files:
```
protoc --cpp_out=src/main/cpp/types/ src/main/proto_types/message.proto
protoc --java_out=src/main/java/server/ src/main/proto_types/message.proto
```
