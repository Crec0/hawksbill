FROM alpine:latest

RUN apk update && apk add --update-cache curl \
    python3 \
    unzip \
    openjdk16-jre-headless

RUN mkdir -p usr/src/bot
COPY ../../test /usr/src/bot/

WORKDIR /usr/src/bot

RUN sh ./download_artifact.sh MindTechMC MindBot

CMD java -jar bot.jar $TOKEN
