# Szybki Start - Testy Obciążeniowe

## Krok 1: Uruchomienie serwisów

```bash
cd /home/daniel/Desktop/gitrepos/PracaMagisterskaMonolit/GroupTaskManager
docker-compose up -d
```

Poczekaj około 30-60 sekund na uruchomienie wszystkich serwisów.

## Krok 2: Weryfikacja działania

Sprawdź, czy serwisy działają:

```bash
# Sprawdź status
docker-compose ps

# Sprawdź endpoint metrics
curl http://localhost:8080/actuator/prometheus | head -20
```

## Krok 3: Uruchomienie testu obciążeniowego

### Opcja A: Użyj skryptu pomocniczego

```bash
./monitoring/run-tests.sh
```

Wybierz opcję 2 (Load Test) z menu.

### Opcja B: Uruchom bezpośrednio

```bash
docker-compose run --rm k6 run /scripts/load-test.js
```

## Krok 4: Monitorowanie wyników

### Grafana Dashboard

1. Otwórz http://localhost:3000
2. Zaloguj się (admin/admin)
3. Przejdź do dashboardu "GroupTaskManager - Spring Boot Metrics"

### Prometheus

1. Otwórz http://localhost:9090
2. Wykonaj zapytania PromQL, np.:
   ```
   rate(http_server_requests_seconds_count[5m])
   ```

## Krok 5: Zatrzymanie serwisów

```bash
docker-compose down
```

## Typy testów

- **Smoke Test**: `docker-compose run --rm k6 run /scripts/smoke-test.js`
- **Load Test**: `docker-compose run --rm k6 run /scripts/load-test.js`
- **Stress Test**: `docker-compose run --rm k6 run /scripts/stress-test.js`

## Rozwiązywanie problemów

### Backend nie odpowiada

```bash
# Sprawdź logi
docker-compose logs backend

# Sprawdź, czy baza danych działa
docker-compose logs postgres
```

### Prometheus nie zbiera metryk

```bash
# Sprawdź konfigurację
cat monitoring/prometheus.yml

# Sprawdź logi
docker-compose logs prometheus
```

### Grafana nie wyświetla danych

1. Sprawdź datasource w Grafana (Configuration → Data Sources)
2. URL powinien być: `http://prometheus:9090`
3. Kliknij "Save & Test"

## Więcej informacji

Zobacz pełną dokumentację w [README.md](README.md)

