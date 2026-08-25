# TSUE Backend

Spring Boot API для фронтенда TSUE (`index.html` / `style.css` / `script.js`).

## Запуск

```bash
cd backend
./mvnw spring-boot:run
```

Если `mvnw` нет — установите Maven и используйте `mvn spring-boot:run`.

Сервер поднимется на `http://localhost:8080`.

## Эндпоинты

Все соответствуют `data-endpoint` в `index.html`:

| Метод  | URL                        | Описание                     |
|--------|-----------------------------|-------------------------------|
| GET    | `/api/subjects`             | список предметов              |
| GET    | `/api/homework`              | список домашних заданий       |
| GET    | `/api/presentations`         | список презентаций            |
| GET    | `/api/notes`                 | список конспектов             |
| GET    | `/api/schedule`               | расписание                    |
| POST   | `/api/{category}`             | создать элемент в разделе     |
| DELETE | `/api/{category}/{id}`        | удалить элемент по id         |

Пример ответа `GET /api/subjects`:

```json
[
  { "id": 1, "title": "Математика", "description": "Преподаватель: Иванов И.И." },
  { "id": 2, "title": "Физика", "description": "Преподаватель: Петров П.П." }
]
```

Пример создания нового элемента:

```bash
curl -X POST http://localhost:8080/api/subjects \
  -H "Content-Type: application/json" \
  -d '{"title": "Химия", "description": "Преподаватель: Смирнова О.В."}'
```

## База данных

Используется H2 in-memory — при каждом перезапуске данные сбрасываются
и заново наполняются тестовыми значениями (`DataInitializer`).

Консоль H2 доступна на `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:tsuedb`, логин `sa`, пароль пустой).

Чтобы переключиться на постоянную БД (например, PostgreSQL),
замените зависимость `h2` в `pom.xml` и настройки в `application.properties`.

## CORS

Разрешены запросы с:
- `http://localhost:5500`, `http://127.0.0.1:5500` (VS Code Live Server)
- `http://localhost:5173` (Vite)
- `http://localhost:3000` (CRA / другие dev-серверы)

Если открываете `index.html` напрямую двойным кликом (`file://...`) —
CORS может блокировать запросы. Используйте Live Server или любой
локальный dev-сервер.

Настройка в `src/main/java/com/tsue/backend/config/CorsConfig.java`.
