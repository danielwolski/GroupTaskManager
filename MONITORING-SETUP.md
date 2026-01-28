# Monitoring i Load Testing - Wersja Mikroserwisów

Przewodnik setup'u narzędzi monitoringu (Prometheus, Grafana) i testów obciążeniowych (K6) dla wersji mikroserwisowej GroupTaskManager.

## 📋 Co się zmieniło w stosunku do wersji monolitycznej

### ✅ Co działa bez zmian
- Testy K6 (`monitoring/k6/load-test.js`) działają bez żadnych zmian
- Scenariusze testów (rejestracja, login, operacje na taskach)
- Thresholds (próg błędów, latencja)
- Struktura dashboardów Grafany

### ⚠️ Co zostało dostosowane
| Element | Monolityczne | Mikroserwisy |
|---------|-------------|-------------|
| **Endpoint Backendu** | `http://backend:8080` | `http://api-gateway:8080` |
| **Metryki Auth** | Backend (8080) | auth-service (8081) |
| **Metryki Task** | Backend (8080) | task-service (8082) |
| **Metryki Report** | Backend (8080) | report-service (8083) |
| **Docker Compose** | `docker-compose.yml` | `docker-compose-monitoring.yml` |
| **Prometheus Config** | `prometheus.yml` | `prometheus-microservices.yml` |

---

## 🚀 Szybki Start

### 1. Uruchomienie pełnego stacku z monitoringiem

```bash
cd /home/daniel/Desktop/gitrepos/GroupTaskManager

# Uruchom wszystkie serwisy + monitoring stack
docker-compose -f docker-compose-monitoring.yml up -d
```

Czekaj ~60 sekund na uruchomienie wszystkich kontenerów.

### 2. Weryfikacja zdrowia serwisów

```bash
# Sprawdź status wszystkich kontenerów
docker-compose -f docker-compose-monitoring.yml ps

# Sprawdź czy API Gateway jest gotowy
curl http://localhost:8080/actuator/health

# Sprawdź metryki na każdym serwisie
curl http://localhost:8081/actuator/prometheus | head -20  # auth-service
curl http://localhost:8082/actuator/prometheus | head -20  # task-service
curl http://localhost:8083/actuator/prometheus | head -20  # report-service
curl http://localhost:8080/actuator/prometheus | head -20  # api-gateway
```

### 3. Uruchomienie testów obciążeniowych

#### Opcja A: Uruchom testy z domyślnym setup'em
```bash
docker-compose -f docker-compose-monitoring.yml run --rm --profile testing k6 \
  run /scripts/load-test.js
```

#### Opcja B: Uruchom testy z custom parametrami
```bash
docker-compose -f docker-compose-monitoring.yml run --rm --profile testing k6 \
  run /scripts/load-test.js -u 50 -d 5m
```

#### Opcja C: Uruchom testy lokalnie (jeśli masz k6 zainstalowany)
```bash
BASE_URL=http://localhost:8080 k6 run monitoring/k6/load-test.js
```

### 4. Monitorowanie wyników

#### Grafana Dashboard
```
URL: http://localhost:3000
Credentials: admin / admin
```

Domyślne dashboardy:
- **Spring Boot Metrics** - metryki ze wszystkich serwisów
- **JVM Metrics** - pamięć, garbage collection
- **HTTP Requests** - latencja, throughput

#### Prometheus Explorer
```
URL: http://localhost:9090
```

Przykładowe QueryID:
- `rate(http_requests_total[5m])` - request rate
- `http_request_duration_seconds_bucket` - latency
- `jvm_memory_used_bytes` - memory usage
- `process_cpu_usage` - CPU usage

---

## 🔧 Konfiguracja

### Prometheus - Microservices (`monitoring/prometheus-microservices.yml`)

Scrapes wszystkie 4 mikroserwisy:
```yaml
- api-gateway:8080 (port 8080/actuator/prometheus)
- auth-service:8081 (port 8081/actuator/prometheus)
- task-service:8082 (port 8082/actuator/prometheus)
- report-service:8083 (port 8083/actuator/prometheus)
```

### Management Endpoints - Spring Boot

Wszystkie serwisy mają exposure dla metrics:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### K6 Test Variables

```javascript
const BASE_URL = __ENV.BASE_URL || 'http://api-gateway:8080';
```

W docker-compose ustawione: `BASE_URL=http://api-gateway:8080`

---

## 📊 Porównanie Wydajności Monolityczne vs Mikroserwisy

### Metryki do porównania

1. **Response Time (Latencja)**
   - Monolityczne: 1 request do 1 serwisu
   - Mikroserwisy: 1 request → API Gateway → serwis docelowy
   - API Gateway dodaje ~10-30ms overhead

2. **Throughput (Żądania/sec)**
   - Monolityczne: singleton backend
   - Mikroserwisy: rozłożone na 4 serwisy, mogą skalować niezależnie

3. **Error Rate**
   - Monolityczne: 1 punkt awarii
   - Mikroserwisy: distributed failures (network timeouts, Kafka issues)

4. **Resource Usage**
   - Monolityczne: 1 process (mniej memory)
   - Mikroserwisy: 4 procesy + Kafka (więcej memory, ale skalowalne)

### Jak porównać

1. **Uruchom test na wersji monolitycznej:**
   ```bash
   cd /path/to/monolityczna/aplikacja
   docker-compose -f docker-compose-monitoring.yml up -d
   docker-compose -f docker-compose-monitoring.yml run --rm --profile testing k6 \
     run /scripts/load-test.js
   ```

2. **Zbierz wyniki z Prometheus:**
   - Response times: `histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))`
   - Throughput: `rate(http_requests_total[5m])`
   - Errors: `rate(http_requests_failed[5m])`

3. **Porównaj w Grafanie:**
   - Stwórz dashboard ze wspólnymi metrykami
   - Zmień źródło danych Prometheus
   - Porównaj wykresy side-by-side

---

## ⚠️ Troubleshooting

### Prometheus nie zbiera metryki
```bash
# Sprawdź konfigurację
curl http://localhost:9090/api/v1/status/config

# Sprawdź targets
curl http://localhost:9090/api/v1/targets

# Sprawdź czy serwisy expose metrics
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8082/actuator/prometheus
curl http://localhost:8083/actuator/prometheus
```

### K6 testy się nie łączą do API
```bash
# Sprawdź czy API Gateway działa
curl http://localhost:8080/actuator/health

# Sprawdź czy testy widzą serwer
docker-compose -f docker-compose-monitoring.yml run --rm --profile testing k6 \
  run -u 1 -d 10s /scripts/load-test.js -v
```

### Brakuje danych w Grafanie
- Poczekaj ~2 minuty na Prometheus scrape
- Sprawdź czy metrics są dostępne: http://localhost:9090
- Sprawdź czy dashboard ma prawidłowy UID/ID
- Zrestartuj Grafanę: `docker-compose -f docker-compose-monitoring.yml restart grafana`

### Out of Memory (OOM)
- Mikroserwisy zużywają więcej RAM niż monolityczne
- Sprawdź: `docker stats`
- Zwiększ pamięć Dockera w Docker Desktop settings

---

## 📁 Pliki Konfiguracyjne

```
monitoring/
├── k6/
│   └── load-test.js                    # Test scenariusze (bez zmian)
├── prometheus-microservices.yml        # ✨ NEW - config dla mikroserwisów
├── prometheus.yml                      # Stara config (dla monolitycznych)
├── grafana/
│   ├── provisioning/
│   │   ├── dashboards/
│   │   └── datasources/
│   └── dashboards/
└── QUICKSTART.md

backend/
├── api-gateway/src/main/resources/application.yml          # ✨ UPDATED
├── auth-service/src/main/resources/application.yml        # ✨ UPDATED
├── task-service/src/main/resources/application.yml        # ✨ UPDATED
└── report-service/src/main/resources/application.yml      # ✨ UPDATED

docker-compose.yml                 # Oryginalna (bez monitoring)
docker-compose-monitoring.yml      # ✨ NEW - pełny stack
```

---

## 🎯 Scenariusze Testowe (K6)

Test obejmuje:

1. **Authentication Flow**
   - Rejestracja nowego użytkownika
   - Login i uzyskanie JWT token

2. **Task Management**
   - GET /tasks
   - POST /tasks (create)
   - GET /daily-tasks
   - POST /daily-tasks (create)

3. **Reporting**
   - GET /daily-tasks/stats
   - GET /users/group

4. **Load Profile**
   - Ramp-up: 10 → 20 → 30 users
   - Duration: ~7 minut
   - Thresholds: p95 < 500ms, p99 < 1000ms, error rate < 5%

---

## 🔄 CI/CD Integration

Aby dodać testy obciążeniowe do pipeline'u:

```yaml
# .github/workflows/performance-test.yml
- name: Run K6 Load Tests
  run: |
    docker-compose -f docker-compose-monitoring.yml up -d
    sleep 30
    docker-compose -f docker-compose-monitoring.yml run --rm --profile testing k6 \
      run /scripts/load-test.js
```

---

## 📚 Przydatne Komendy

```bash
# Przeglądaj logi serwisów
docker-compose -f docker-compose-monitoring.yml logs -f api-gateway

# Wejdź do kontenera
docker-compose -f docker-compose-monitoring.yml exec api-gateway /bin/sh

# Zatrzymaj monitoring (zachowaj dane)
docker-compose -f docker-compose-monitoring.yml stop

# Usuń wszystko (łącznie z danymi)
docker-compose -f docker-compose-monitoring.yml down -v

# Przebuduj obrazy
docker-compose -f docker-compose-monitoring.yml build
```

---

## 📝 Notatki

- **API Gateway overhead**: +10-30ms na request (normalne dla routing layer)
- **Kafka latency**: Event processing ~100-500ms (asynchroniczny)
- **Network latency**: +5-10ms między serwisami (localhost)
- **Load test duration**: Total ~7 minut (ramp-up + steady state + ramp-down)

---

Created: 2026-01-27
Last Updated: 2026-01-27
