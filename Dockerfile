# Utiliser une image Java 17
FROM openjdk:17-jdk-slim

# Installer Maven
RUN apt-get update && apt-get install -y maven

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers du projet
COPY . .

# Construire l'application
RUN mvn clean package -DskipTests

# Exposer le port
EXPOSE 8080

# Démarrer l'application
CMD ["java", "-jar", "target/*.jar", "--spring.profiles.active=railway"]