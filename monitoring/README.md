# Testy Obciążeniowe - GroupTaskManager

Ten katalog zawiera konfigurację do przeprowadzania testów obciążeniowych systemu GroupTaskManager przy użyciu narzędzi: **Prometheus**, **Grafana** i **k6**.

## Architektura Monitoringu

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Spring    │────▶│  Prometheus  │────▶│   Grafana   │
│   Boot App  │     │  (Metrics)   │     │ (Dashboard) │
└─────────────┘     └──────────────┘     └─────────────┘
       ▲
       │
┌──────┴──────┐
│     k6      │
│ (Load Test) │
└─────────────┘
```

## Wymagania

- Docker i Docker Compose
- Zainstalowane zależności projektu (Gradle dla backendu)

## Instalacja i Konfiguracja

### 1. Przygotowanie środowiska

Upewnij się, że wszystkie serwisy są gotowe:

```bash
# Z głównego katalogu projektu
cd /home/daniel/Desktop/gitrepos/PracaMagisterskaMonolit/GroupTaskManager
```

### 2. Uruchomienie całego stacku

Uruchom wszystkie serwisy (backend, PostgreSQL, Prometheus, Grafana):

```bash
docker-compose up -d
```

To uruchomi:
- **PostgreSQL** na porcie `5432`
- **Backend** (Spring Boot) na porcie `8080`
- **Node Exporter** na porcie `9100` (metryki hosta)
- **Prometheus** na porcie `9090`
- **Grafana** na porcie `3000`

### 3. Weryfikacja działania

Sprawdź, czy wszystkie serwisy działają:

```bash
# Sprawdź status kontenerów
docker-compose ps

# Sprawdź endpointy metrics
curl http://localhost:8080/actuator/prometheus

# Sprawdź Prometheus
curl http://localhost:9090/-/healthy

# Sprawdź Grafana
curl http://localhost:3000/api/health
```

## Dostęp do interfejsów

- **Grafana**: http://localhost:3000
  - Login: `admin`
  - Hasło: `admin`
  - Przy pierwszym logowaniu zostaniesz poproszony o zmianę hasła

- **Prometheus**: http://localhost:9090
  - Interfejs webowy do przeglądania metryk i zapytań PromQL

- **Backend API**: http://localhost:8080
  - Endpoint metrics: http://localhost:8080/actuator/prometheus
  - Endpoint health: http://localhost:8080/actuator/health

## Przeprowadzanie Testów Obciążeniowych

### Typy testów

W katalogu `monitoring/k6/` znajdują się trzy typy testów:

1. **Smoke Test** (`smoke-test.js`) - Podstawowy test weryfikujący, czy system działa
2. **Load Test** (`load-test.js`) - Test obciążeniowy z stopniowym zwiększaniem obciążenia
3. **Stress Test** (`stress-test.js`) - Test wytrzymałościowy do znalezienia punktu załamania

### 1. Smoke Test (Test Dymny)

Najprostszy test do weryfikacji podstawowej funkcjonalności:

```bash
docker-compose run --rm k6 run /scripts/smoke-test.js
```

**Parametry:**
- 1 użytkownik wirtualny
- 30 sekund trwania
- Weryfikuje podstawowe endpointy

### 2. Load Test (Test Obciążeniowy)

Główny test obciążeniowy z stopniowym zwiększaniem obciążenia:

```bash
docker-compose run --rm k6 run /scripts/load-test.js
```

**Parametry testu:**
- Ramp-up: 10 użytkowników → 20 → 30
- Każdy poziom trwa 1 minutę
- Ramp-down: 30 sekund
- **Progi (thresholds):**
  - 95% żądań < 500ms
  - 99% żądań < 1000ms
  - Współczynnik błędów < 5%

**Co testuje:**
- Rejestracja i logowanie użytkowników
- Pobieranie listy zadań
- Tworzenie zadań
- Pobieranie listy zadań dziennych
- Tworzenie zadań dziennych
- Pobieranie użytkowników w grupie
- Pobieranie statystyk zadań dziennych

### 3. Stress Test (Test Wytrzymałościowy)

Test do znalezienia maksymalnego obciążenia systemu:

```bash
docker-compose run --rm k6 run /scripts/stress-test.js
```

**Parametry testu:**
- Stopniowe zwiększanie: 10 → 20 → 30 → 40 → 50 użytkowników
- Każdy poziom trwa 2 minuty
- Bardziej tolerancyjne progi błędów (do 10%)

### Uruchamianie testów z własnymi parametrami

Możesz uruchomić testy z własnymi parametrami:

```bash
# Zmiana URL backendu
docker-compose run --rm -e BASE_URL=http://localhost:8080 k6 run /scripts/load-test.js

# Uruchomienie testu lokalnie (jeśli masz zainstalowany k6)
k6 run monitoring/k6/load-test.js
```

## Monitorowanie w czasie rzeczywistym

### 1. Grafana Dashboard

Po uruchomieniu testów, otwórz Grafana (http://localhost:3000) i przejdź do dashboardu **"GroupTaskManager - Spring Boot Metrics"**.

Dashboard zawiera następujące panele:
- **HTTP Request Rate** - Liczba żądań na sekundę
- **HTTP Request Duration (p95)** - Czas odpowiedzi (percentyl 95)
- **JVM Memory Usage** - Użycie pamięci JVM
- **JVM Threads** - Liczba wątków JVM
- **HTTP Error Rate** - Współczynnik błędów HTTP
- **Database Connection Pool** - Pula połączeń do bazy danych

### 2. Prometheus Queries

W Prometheus (http://localhost:9090) możesz wykonywać zapytania PromQL:

```promql
# Liczba żądań HTTP na sekundę
rate(http_server_requests_seconds_count[5m])

# Czas odpowiedzi (p95)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Użycie pamięci heap
jvm_memory_used_bytes{area="heap"}

# Liczba aktywnych wątków
jvm_threads_live_threads

# Współczynnik błędów 5xx
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

## Analiza wyników

### Kluczowe metryki do monitorowania

1. **Czas odpowiedzi (Response Time)**
   - P95 < 500ms (dla load test)
   - P99 < 1000ms
   - Średni czas odpowiedzi

2. **Współczynnik błędów (Error Rate)**
   - Powinien być < 5% dla load test
   - < 10% dla stress test

3. **Przepustowość (Throughput)**
   - Liczba żądań na sekundę
   - Porównanie przed i po optymalizacji

4. **Zasoby systemowe**
   - Użycie CPU
   - Użycie pamięci (heap i non-heap)
   - Liczba wątków
   - Pula połączeń do bazy danych

### Interpretacja wyników k6

Po zakończeniu testu k6 wyświetli podsumowanie:

```
✓ get tasks status is 200
✓ create task status is 202
✓ get daily tasks status is 200
...

checks.........................: 95.00% ✓ 1900      ✗ 100
data_received..................: 2.5 MB 42 kB/s
data_sent......................: 1.2 MB 20 kB/s
http_req_duration..............: avg=245ms min=120ms med=220ms max=1200ms p(95)=480ms p(99)=950ms
http_req_failed................: 2.50%  ✓ 50       ✗ 1950
http_reqs......................: 2000   33.33/s
```

**Ważne wskaźniki:**
- `http_req_duration` - Czas odpowiedzi (szczególnie p95 i p99)
- `http_req_failed` - Współczynnik błędów
- `checks` - Procent udanych weryfikacji

## Rozwiązywanie problemów

### Problem: Backend nie eksportuje metryk

**Rozwiązanie:**
1. Sprawdź, czy zależności Prometheus są dodane w `build.gradle`
2. Sprawdź konfigurację w `application.properties`
3. Sprawdź logi: `docker-compose logs backend`

### Problem: Prometheus nie zbiera metryk

**Rozwiązanie:**
1. Sprawdź konfigurację w `monitoring/prometheus.yml`
2. Sprawdź, czy backend jest dostępny: `curl http://backend:8080/actuator/prometheus`
3. Sprawdź logi: `docker-compose logs prometheus`

### Problem: Grafana nie wyświetla danych

**Rozwiązanie:**
1. Sprawdź, czy datasource Prometheus jest skonfigurowany
2. Sprawdź URL datasource (powinien być `http://prometheus:9090`)
3. Sprawdź logi: `docker-compose logs grafana`

### Problem: Testy k6 kończą się błędami

**Rozwiązanie:**
1. Sprawdź, czy backend jest uruchomiony i dostępny
2. Sprawdź logi backendu pod kątem błędów
3. Zwiększ progi błędów w konfiguracji testu
4. Sprawdź, czy baza danych jest dostępna

## Zatrzymywanie serwisów

```bash
# Zatrzymaj wszystkie serwisy
docker-compose down

# Zatrzymaj i usuń wolumeny (uwaga: usuwa dane!)
docker-compose down -v
```

## Zaawansowane użycie

### Eksport wyników k6 do JSON

```bash
docker-compose run --rm k6 run --out json=results.json /scripts/load-test.js
```

### Uruchomienie testu z własnym scenariuszem

Możesz zmodyfikować pliki testowe w `monitoring/k6/` lub utworzyć własne:

```bash
# Utwórz własny test
cat > monitoring/k6/custom-test.js << 'EOF'
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 10,
  duration: '1m',
};

export default function () {
  const res = http.get('http://backend:8080/api/tasks');
  check(res, { 'status is 200': (r) => r.status === 200 });
}
EOF

# Uruchom test
docker-compose run --rm k6 run /scripts/custom-test.js
```

### Integracja z CI/CD

Możesz dodać testy do pipeline CI/CD:

```yaml
# Przykład dla GitHub Actions
- name: Run Load Tests
  run: |
    docker-compose up -d
    sleep 30  # Czekaj na uruchomienie serwisów
    docker-compose run --rm k6 run /scripts/smoke-test.js
```

## Przydatne linki

- [Dokumentacja k6](https://k6.io/docs/)
- [Dokumentacja Prometheus](https://prometheus.io/docs/)
- [Dokumentacja Grafana](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Prometheus](https://micrometer.io/docs/registry/prometheus)

## Wsparcie

W przypadku problemów sprawdź:
1. Logi kontenerów: `docker-compose logs [service-name]`
2. Status kontenerów: `docker-compose ps`
3. Metryki w Prometheus: http://localhost:9090
4. Dashboard w Grafana: http://localhost:3000

