# =====================================================================
# S.B.P. - Smart Bus Point | imagem da API
#
# Build em duas etapas: a primeira compila com Maven, a segunda carrega
# apenas o JRE e o jar, para que a imagem final fique pequena.
#
# Uso (a partir da raiz do projeto):
#   docker compose --profile app up -d --build
# =====================================================================

# ---------- etapa 1: compilacao ----------
FROM maven:3.9-eclipse-temurin-21 AS construcao
WORKDIR /construcao

# As dependencias mudam pouco; copiar o pom primeiro aproveita o cache
# de camadas do Docker e evita baixar tudo de novo a cada build.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- etapa 2: execucao ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# A aplicacao nao precisa de root para servir HTTP.
RUN addgroup -S sbp && adduser -S sbp -G sbp
USER sbp

COPY --from=construcao /construcao/target/*.jar app.jar

EXPOSE 8080
ENV TZ=America/Sao_Paulo

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
