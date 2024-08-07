# Podcreep Android client

The Podcreep Android client

## Getting started

Simply import the project into Android Studio, and it should be good to go.

## Running

The client will attempt to connect to 127.0.0.1:8080 when running in debug mode. To allow this to
connect to a server that you're running on your host machine (see
[server/README.md](https://github.com/podcreep/server/blob/master/README.md) for instructions on
running the server), you'll want to start a reverse host forward, like so:

    $ adb reverse tcp:8080 tcp:8080

This will cause connections to 127.0.0.1:8080 on the device to be forwarded to port 8080 on your
host machine (where the server is running).
