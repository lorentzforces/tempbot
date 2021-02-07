# TempBot
*A basic Discord bot*

TempBot was originally created as a temperature-conversion bot. As time goes on, more ~~memes~~ features will be added.

## Building the Project
Run `./gradlew clean assemble` to build the project jar (which includes all dependencies). The jar output will be in `build/libs/tempbot.jar`. (There is an outstanding issue to change the output to the top-level directory.)

Currently, the default log level is set by the default logger in `src/main/resource/log4j2.xml`. In order to change the log level, you will have to either manually change this file or set the logging level via system property. (There is an outstanding issue to allow setting the log level depending on build/environment.)
