# java-project-99

Веб-приложение для управления задачами (Task Manager). Бэкенд на Spring Boot 4 с JWT-аутентификацией, REST API и встроенным React-фронтендом.

### Hexlet tests and linter status:
[![Actions Status](https://github.com/necasper/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/necasper/java-project-99/actions)

### Сборка CI

[![build](https://github.com/necasper/java-project-99/actions/workflows/build.yml/badge.svg)](https://github.com/necasper/java-project-99/actions/workflows/build.yml)

### SonarCloud

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=necasper_java-project-99&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=necasper_java-project-99)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=necasper_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=necasper_java-project-99)

### Задеплоенное приложение

[Демо на Render](https://java-project-99-55fv.onrender.com/welcome)

## Required

- Java 25+
- Gradle (используется wrapper из репозитория)

## Development

Скопируйте файл с переменными окружения (опционально, но рекомендуется):

```bash
copy .env.example .env
```

Запуск приложения в режиме разработки:

```bash
./gradlew bootRun
```

Gradle автоматически подхватит переменные из файла `.env`, если он существует.

Приложение будет доступно по адресу:

```
http://localhost:8080
```

Полезные URL:

- `http://localhost:8080` — веб-интерфейс
- `http://localhost:8080/welcome` — проверка, что сервер запущен
- `http://localhost:8080/docs` — Swagger UI (документация API)
- `http://localhost:8080/h2-console` — консоль H2 (только dev-профиль)

### Учётные данные по умолчанию

При первом запуске создаётся администратор. В dev-профиле используются такие данные:

| Поле     | Значение              |
|----------|-----------------------|
| username | `hexlet@example.com`  |
| password | `password`            |

Пароль можно переопределить через переменную окружения `ADMIN_PASSWORD` (в `.env` или в системе).

### Тесты и проверка стиля

```bash
./gradlew test
./gradlew checkstyleMain checkstyleTest
```

## API

Аутентификация выполняется через `POST /api/login`. В ответ приходит JWT-токен, который нужно передавать в заголовке `Authorization: Bearer <token>` для защищённых эндпоинтов.
