# Bank REST — Система Управления Банковскими Картами

## Запуск dev окружения (Docker)
1. Соберите jar: `mvn -Pprod -DskipTests package`
2. Запустите: `docker-compose up --build`

Приложение будет доступно на http://localhost:8080

## Настройки
- DB: PostgreSQL (настройки в `application.yml`, можно переопределить переменными окружения)
- JWT secret и AES key задаются через переменные окружения: `JWT_SECRET`, `AES_KEY`

## API
- Swagger/OpenAPI: открыть `docs/openapi.yaml` (при желании подключите Swagger UI)

## Требования выполнения ТЗ
- Spring Boot, Spring Security + JWT
- Роли: ADMIN и USER
- CRUD для карт, переводы между своими картами, пагинация/фильтрация
- Liquibase миграции: `src/main/resources/db/changelog/...`
- Docker Compose для dev (Postgres)
- Юнит-тесты (см. src/test)
