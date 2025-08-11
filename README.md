# Envisalink to Blue Iris Sync Service

## Overview

This service connects to an Envisalink security system interface (for DSC or Honeywell panels) and synchronizes its state with Blue Iris video surveillance software. Specifically, it listens for alarm partition arm/disarm events and changes the active Blue Iris profile accordingly.

This allows you to, for example, automatically activate a more sensitive camera profile when you arm your alarm system and revert to a less sensitive one when you disarm it.

## Features

- Listens for `PartitionArmAway` and `PartitionDisarmed` events from Envisalink.
- Changes Blue Iris profile based on received events.
- All connection and logic parameters are configured via a simple `config.properties` file.
- Packaged as a single, executable "fat jar" for easy deployment.
- Robust logging using Log4j2.

## Prerequisites

- Java 11 or higher.
- A running Envisalink device (e.g., EVL-3 or EVL-4) on your network.
- A running Blue Iris server (version 5.x) on your network.

## Configuration

Before running the application, you must create a `config.properties` file in the same directory where you will run the JAR file. You can copy the `config.properties.example` file and rename it to `config.properties`.

Then, edit the file with your specific settings:

| Property              | Description                                                                 | Example         |
|-----------------------|-----------------------------------------------------------------------------|-----------------|
| `envisalink.host`     | The IP address of your Envisalink device.                                   | `192.168.1.10`  |
| `envisalink.port`     | The port for the Envisalink TPI (usually 4025).                             | `4025`          |
| `envisalink.password` | The password for the Envisalink TPI user.                                   | `user`          |
| `blueiris.host`       | The IP address of your Blue Iris server.                                    | `192.168.1.11`  |
| `blueiris.port`       | The web server port for Blue Iris (usually 81).                             | `81`            |
| `blueiris.username`   | The username for a Blue Iris user account.                                  | `admin`         |
| `blueiris.password`   | The password for that Blue Iris user.                                       | `admin`         |
| `profile.armed.away`  | The Blue Iris profile number to activate when the partition is armed away.  | `1`             |
| `profile.disarmed`    | The Blue Iris profile number to activate when the partition is disarmed.    | `2`             |

## Building from Source

To build the application yourself, you will need Apache Maven.

1.  Clone the repository:
    ```sh
    git clone <repository_url>
    cd envisalink-blueiris-sync
    ```
2.  Run the Maven package command:
    ```sh
    mvn clean package
    ```
    This will create a fat jar in the `target` directory named `envisalink-blueiris-sync-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Running the Application

1.  Build the project as described above, or download the pre-built JAR from the releases page.
2.  Place the JAR file in a directory of your choice.
3.  Create and configure your `config.properties` file in the same directory.
4.  Run the application from your terminal:
    ```sh
    java -jar envisalink-blueiris-sync-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

The service will start, connect to both Envisalink and Blue Iris, and begin listening for events. All activity and errors will be logged to the console. To stop the service, press `Ctrl+C`.
