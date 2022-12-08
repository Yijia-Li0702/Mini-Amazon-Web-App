FROM ubuntu:18.04

RUN apt-get update && apt-get install -yq --no-install-recommends openjdk-11-jdk && apt-get clean
RUN apt-get update && apt-get install -yq --no-install-recommends maven && apt-get clean

RUN mkdir /amazonCode
WORKDIR /amazonCode
ADD . /amazonCode