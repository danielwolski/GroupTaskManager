# GroupTaskManager - Architektura Mikroserwisowa

## Przegląd architektury

```
                    ┌─────────────────┐
                    │   Frontend      │
                    │   (Angular)     │
                    │   Port: 4200    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   Port: 8080    │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  Auth Service   │ │  Task Service   │ │ Report Service  │
│  Port: 8081     │ │  Port: 8082     │ │  Port: 8083     │
│  DB: authdb     │ │  DB: taskdb     │ │  DB: reportdb   │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         │                   └─────────┬─────────┘
         │                             │
         │                      ┌──────▼──────┐
         │                      │    Kafka    │
         │                      │  Port: 9092 │
         │                      └─────────────┘
         │
         └──────► Inter-service REST calls
```

## Serwisy

### 1. API Gateway (Port 8080)
- Routing requestów do odpowiednich serwisów
- Weryfikacja JWT tokenów
- CORS handling

### 2. Auth Service (Port 8081)
- Rejestracja i logowanie użytkowników
- Zarządzanie użytkownikami i grupami
- Generowanie i walidacja JWT tokenów
- **Tabele:** `users`, `groups`, `tokens`

### 3. Task Service (Port 8082)
- CRUD dla zwykłych zadań (Tasks)
- CRUD dla dziennych zadań (DailyTasks)
- Archiwizacja daily tasków (wysyła eventy do Kafki)
- **Tabele:** `tasks`, `daily_tasks`

### 4. Report Generator Service (Port 8083)
- Generowanie raportów PDF
- Statystyki dziennych zadań
- Konsumuje eventy z Kafki
- **Tabele:** `daily_task_entries`

## Uruchomienie

### Wymagania
- Docker
- Docker Compose

### Szybki start

```bash
# Uruchomienie wszystkich serwisów
docker-compose up -d --build

# Sprawdzenie statusu
docker-compose ps

# Logi
docker-compose logs -f

# Zatrzymanie
docker-compose down
```

### Bazy danych

| Serwis | Port | Baza | User | Password |
|--------|------|------|------|----------|
| Auth Service | 5433 | authdb | admin | admin |
| Task Service | 5434 | taskdb | admin | admin |
| Report Service | 5435 | reportdb | admin | admin |

### Seed danych

```bash
# Po uruchomieniu serwisów, załaduj dane testowe:
docker exec -i -e PGPASSWORD=admin auth-db psql -U admin -d authdb < postgresql/seed-microservices.sql
```

## API Endpoints

### Auth Service (via Gateway: /api/auth/*)
- `POST /api/auth/register` - Rejestracja
- `POST /api/auth/login` - Logowanie
- `POST /api/auth/refresh-token` - Odświeżenie tokenu

### Task Service (via Gateway: /api/tasks/*, /api/daily-tasks/*)
- `GET /api/tasks` - Lista zadań
- `POST /api/tasks` - Tworzenie zadania
- `DELETE /api/tasks/{id}` - Usunięcie zadania
- `PATCH /api/tasks/{id}` - Toggle done
- `GET /api/daily-tasks` - Lista dziennych zadań
- `POST /api/daily-tasks` - Tworzenie dziennego zadania
- `DELETE /api/daily-tasks/{id}` - Usunięcie
- `PATCH /api/daily-tasks/{id}` - Toggle done

### Report Service (via Gateway: /api/reports/*)
- `GET /api/reports/stats/current-user?daysBack=7` - Statystyki użytkownika
- `GET /api/reports/stats/all-users?daysBack=7` - Statystyki grupy
- `GET /api/reports/pdf?daysBack=7` - Raport PDF

## Kafka Topics

- `daily-task-archived` - Eventy archiwizacji dziennych zadań

## Komunikacja między serwisami

1. **API Gateway → Wszystkie serwisy**: Routing + JWT verification
2. **Task Service → Auth Service**: REST (pobieranie danych użytkownika)
3. **Report Service → Auth Service**: REST (pobieranie danych użytkownika)
4. **Task Service → Report Service**: Kafka (eventy archiwizacji)

## Testowe dane logowania

Po załadowaniu seed'a:
- Login: `kowalski@test.pl`, Password: `password123`, Grupa: `alpha123`
- Login: `nowak@test.pl`, Password: `password123`, Grupa: `alpha123`
- Login: `zielinski@test.pl`, Password: `password123`, Grupa: `bravo456`

