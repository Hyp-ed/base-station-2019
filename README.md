# Base-Station 2019

This project is responsible for communicating with the hyperloop pod and sending relevant commands and information back and forth. The web interface is also included in here.

### How to run
This project uses gradle as its build system. The gradle wrapper is already checked into this repo, so no need to explicitly download gradle (unless you want to).

Download the latest release from Github, and run:
```
$ java -jar build/libs/base-station-2019.jar
```

Go to `localhost:8080` for the gui.

### Build project:
```
$ ./gradlew build
```
(If on windows use `gradlew.bat` instead of `./gradlew`)

This will create a jar file in `build/libs/` that contains both the backend and the static frontend that it serves.

Also compile protobuf files (not really necessary as the generated files are already checked in, but just in case):
```
$ protoc -I=src/main/proto_types/ --java_out=src/main/java/server/ message.proto
```

Be sure to make a `/temp` directory in your project's root directory, as this is where the log is stored. Read log with `tail -F temp/server_log.log`
