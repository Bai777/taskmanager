# Task Manager

Сервис для управления задачами с JWT аутентификацией.

## Технологии
- Java 21
- Spring Boot 3.4.3
- Spring Security, JWT
- PostgreSQL
- Docker, Docker Compose
- Micrometer (Prometheus)
- Swagger/OpenAPI

## Запуск

### Локально (без Docker)
1. Установить PostgreSQL и создать базу `taskmanager` с пользователем `taskuser` / `taskpass` (или изменить в `application.yml`).
2. Запустить:
   ```bash
   mvn spring-boot:run