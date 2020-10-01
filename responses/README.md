# Rasa Advanced Workshop: Externalizing Responses

Responses server written in Java that exposes a REST API adhering to Rasa [responses](https://legacy-docs.rasa.com/docs/core/responses/) format.

## Development
This project is developed on top of [vert.x](http://vertx.io/) using JDK 11.

> You can use docker to build and run the project. To use docker, run this command docker run -it --rm -v "$PWD":/workshop -v "$PWD/.m2":/root/.m2/ -w /workshop -p 8080:8080 openjdk:11 bash

* Install the latest version of JDK 11.
* Build the code by running the maven wrapper "./mvnw clean install"
* To start the server, run "java -jar target/workshop-1.0.0-SNAPSHOT-fat.jar"

## Credits
Repurposed from https://github.com/floc-crisis-center
