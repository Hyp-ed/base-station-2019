# Telemetry Prototype

Prototype that connects a server and a client over a tcp connection

## How to run
This project uses gradle as its build system. The gradle wrapper is already checked into this repo, so no need to explicitly download gradle (unless you want to).

Build project:
```
./gradlew build
```
(If on windows use `gradlew.bat` instead of `./gradlew`)

Run server side:
```
./gradlew run
```

Run client side: In another terminal window, run
```
./build/exe/main/main
```
(there's a `runClient` task in `build.gradle` but for some reason doing this only prints client output once the program ends, which isn't so useful)

Also be sure to make a `/temp` directory in your project's root directory, as this is where the log is stored. Read log with `tail -F temp/server_log.log`
