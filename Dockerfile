FROM bdhwan/parse-server:3.0
MAINTAINER bdhwan@gmail.com


RUN sudo apt-get update
RUN sudo apt-get install -y software-properties-common
RUN sudo apt install -y build-essential
RUN sudo apt install -y openjdk-8-jdk  
RUN sudo apt install -y maven
RUN sudo npm install -g node-gyp
RUN sudo npm install -g java --unsafe-perm

ADD open-korean-text /home/open-korean-text 
WORKDIR /home/open-korean-text
RUN mvn compile

WORKDIR /home
