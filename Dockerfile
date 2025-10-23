FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /work
COPY pom.xml .
COPY parcel-service/pom.xml parcel-service/pom.xml
COPY . .
RUN mvn -B -pl parcel-service -am -DskipTests package

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /work/parcel-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]