# TempBot
*A basic Discord bot*

TempBot was originally created as a temperature-conversion bot. As time goes on, more ~~memes~~ features will be added.

## Building the Project

### Requirements:

- an internet connection to download dependencies (only on first build or when dependencies have changed)
- Java `11` executables available to Gradle

To build the project without running it, you can run the following command:
```
./gradlew clean build
```

The output is a single file named `tempbot.jar` in the project's root directory and includes all dependencies.

Currently, the default log level is set by the default logger in `src/main/resource/log4j2.xml`. In order to change the log level, you will have to either manually change this file or set the logging level via system property. (There is an outstanding issue to allow setting the log level depending on build/environment.)

## Running the project

You can run the project via gradle using the following command:
```
./gradlew run
```

Because of how Gradle incremental builds work, this will first compile any changed files before running.

At the most basic level, running the project requires running `java -jar <path-to-output-jar>` in a directory with a valid `client.yml` file. All configuration is contained within `client.yml`; there is no need to set any environment variables or system properties.

[An example client.yml is provided in the root directory of this project.](./client.example.yml) You will need to fill in the relevant fields with valid `cliendId` and `secret` values for a valid Discord application. [Discord's application dashboard can be found here.](https://discord.com/developers/applications)

## Running Tests

You can run all tests with the following command:
```
./gradlew test
```
