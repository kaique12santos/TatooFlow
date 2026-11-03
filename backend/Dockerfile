# Estágio 1: Build (Compilação do projeto com Maven)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código-fonte e compila o .jar (ignorando testes para ser mais rápido)
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução do Spring Boot com uma imagem Java leve)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copia o arquivo .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar
# Expõe a porta padrão do Spring Boot
EXPOSE 8080
# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]