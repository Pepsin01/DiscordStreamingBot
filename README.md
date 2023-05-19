# Discord streaming bot
## Overview
This bot is a simple bot that can stream audio from YouTube and other sources to a discord voice channel.
It uses the Lavaplayer library to stream audio from YouTube and JDA library to connect to discord.
## Build
To build this bot with maven, run the following command in the root directory of this project:
```
mvn clean package
```
This version of the bot requires Java 19 or higher.
## Run
To run this bot, you need to have a discord bot token.
You can get one by creating a new application on the [discord developer portal](https://discord.com/developers/applications).
Remember to give this bot the following permissions:
- Send messages
- Manage messages
- Connect
- Speak
- Use voice activity
- View channels

Once you have a token, added the bot to your server and gave it the required permissions,
put the token in a text file in the root directory of this project.
Then run the following command in the root directory of this project:
```
java -jar target/Zapoctak-1.0-SNAPSHOT.jar <token file>
```
## Usage
To use this bot, you need to be in a voice channel.
Then you can use the following commands:
- `/play <url>` - Plays a song or adds it to the queue
- `/add <url>` - Adds a song to the queue
- `/stop` - Stops the player and removes the current song from queue
- `/start` - Starts the player
- `/pause` - Pauses the player
- `/resume` - Resumes the player
- `/volume <volume>` - Sets the volume of the player
- `/skip` - Skips the current song
- `/queue` - Shows the current queue
- `/clear` - Clears the queue
- `/remove <index>` - Removes a song from the queue
- `/shuffle` - Shuffles the queue
- `/loop` - Loops the current song
- `/loopqueue` - Loops the queue
- `/nowplaying` - Shows the current song
- `/help` - Shows this message

## Documentation
To generate the documentation, run the following command in the root directory of this project:
```
mvn javadoc:javadoc
```
The documentation will be generated in the `target/site/apidocs` directory.