FROM openjdk:17
VOLUME /tmp

EXPOSE 8080

WORKDIR /jars

COPY xm-recommendation-service/target/*.jar xm-recommendation-service.jar

CMD ["sh", "-c", "java -jar /jars/xm-recommendation-service.jar & wait"]