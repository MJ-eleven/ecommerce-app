FROM eclipse-temurin:17-jdk-jammy

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers du projet
COPY . .

# Installer Maven et construire le projet
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# Exposer le port
EXPOSE 8080

# Démarrer l'application
CMD ["java", "-jar", "target/ecommerce-app-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=railway"]