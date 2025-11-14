# -- V4 --
# 1st Docker build stage: build the project with Maven
FROM maven:3.6.3-openjdk-11 AS builder
WORKDIR /project
COPY . /project/
RUN mvn package -DskipTests -B

# 2nd Docker build stage: copy builder output and configure entry point
#FROM openjdk:21-jdk
FROM eclipse-temurin:21-jre
ENV APP_DIR /application
ENV APP_FILE ex-dock-0.0.1-SNAPSHOT.jar

EXPOSE 8888

WORKDIR $APP_DIR
COPY --from=builder /project/target/*-fat.jar $APP_DIR/$APP_FILE
COPY ./swagger.json $APP_DIR/swagger.json
COPY ./webroot $APP_DIR/webroot

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]
