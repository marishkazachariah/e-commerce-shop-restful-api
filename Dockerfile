FROM eclipse-temurin:17-jdk-focal
LABEL authors="Marishka Zachariah"

WORKDIR /zalando-ecommerce
COPY target/e-commerce-shop-0.0.1-SNAPSHOT.jar /zalando-ecommerce/zalando-ecommerce-springboot.jar
ENTRYPOINT ["java", "-jar", "zalando-ecommerce-springboot.jar"]

EXPOSE 8080
