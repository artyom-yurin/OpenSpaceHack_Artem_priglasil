FROM maven:3.6.3-jdk-8 AS build
RUN mkdir /spring-app/
COPY ./pom.xml /spring-app
COPY ./spring-boot.iml /spring-app
RUN mkdir /spring-app/src
COPY ./src /spring-app/src/
WORKDIR /spring-app
EXPOSE 8080
RUN mvn clean package
CMD mvn spring-boot:run
