# Mindbot

## Overview

Mindtech Technical Server's custom bot that provides a moderation, administration, rcon, chatlink, and many more features.

For all the features, please refer to [Features](Features.md).

## Commands

Please refer [Commands](COMMANDS.md)

### Building the bot

Note: Only required if you're following [Compiled Jar](#compiled-jar). Otherwise, you can skip this section.
```shell
# Clone the project
$ git clone https://github.com/MindTechMC/MindBot

# Change directory to the project
$ cd MindBot

# Build using gradle
$ gradlew shadowJar
```

## Using the bot

There are several ways and run the bot

- [Docker](#docker)
- [Source](#source)
- [Compiled Jar](#compiled-jar)

### Docker
Docker image is not public, however it's easier to setup.
Simply get `Dockerfile`, `_netrc.sample` and `download_artifact.sh` from the repository.
- Rename `_netrc.sample` to `_netrc`
- Replace `<user>` with your username and `<token>` with your personal access token with `public_repo` scope.
- Run the commands below
- Don't forget to replace `DISCORD_TOKEN` with your bot token.

```shell
# Build the docker container
docker build -t <botname>:<tag> .

# Run the docker container using discord token for your bot
docker run --env TOKEN="DISCORD_TOKEN" <botname>:<tag>
```

### Source
```shell
# Clone the project
git clone https://github.com/MindTechMC/MindBot

# Change directory to the project
cd MindBot

# Run using gradle
gradlew run --args="DISCORD_TOKEN"
```

### Compiled Jar

- Get from `builds/libs` directory if you choose to Build it yourself using [Build Jar](#build-jar)
```shell
# Run the jar
java -jar Mindbot-major.minor.patch.jar DISOCRD_TOKEN
```
### Issues

If you find any bugs or have feature requests, please make an issue on the issue tracker
