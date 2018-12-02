# Telemetry Prototype

Prototype that connects a server and a client over a tcp connection

## How to run
Compile and run server side:
```
javac Main.java Client.java
java Main
```
and in another terminal window

```
java Client
```
Also be sure to make a directory `working_directory/temp` , as this is where the log is stored. Read log with `tail -F server_log.log`

---

Or to run with the C++ client, compile `cpp\ client/client.cpp` with
```
g++ -o client -std=c++11 client.cpp
```
and run with
```
./client
```
