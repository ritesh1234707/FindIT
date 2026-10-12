# ── Stage 1: Build Frontend (Vite + React) ──
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ── Stage 2: Build Backend (Java Maven) ──
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests && cp target/findit-1.0.0.jar target/app.jar

# ── Stage 3: Minimal JRE Runtime ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled JAR and React build assets
COPY --from=backend-builder /app/target/app.jar ./app.jar
COPY --from=frontend-builder /app/frontend/dist ./frontend/dist

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
