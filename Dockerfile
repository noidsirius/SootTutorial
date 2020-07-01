from openjdk:8
WORKDIR /app
COPY . /app
RUN ./gradlew check

