FROM maven:3.6.2-jdk-11-slim AS builder

COPY . /

# build main
RUN mvn clean install -DskipTests --quiet 

FROM adoptopenjdk/openjdk11

COPY --from=builder target/*.jar /app/ApplicationMain.jar


WORKDIR /app

COPY entrypoint.sh /app
RUN chmod +x entrypoint.sh
#ENTRYPOINT ["sh", "-c", "/app/entrypoint.sh"]
CMD ["/app/entrypoint.sh"]
