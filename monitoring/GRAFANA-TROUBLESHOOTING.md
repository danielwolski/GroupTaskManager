# Rozwiązywanie problemów z Grafaną

## Problem: Dashboard nie pokazuje danych

### Krok 1: Sprawdź czy Prometheus zbiera metryki

```bash
# Sprawdź status targetów w Prometheus
curl http://localhost:9090/api/v1/targets

# Sprawdź czy serwisy eksportują metryki
curl http://localhost:8080/actuator/prometheus | head -20  # api-gateway
curl http://localhost:8081/actuator/prometheus | head -20  # auth-service
curl http://localhost:8082/actuator/prometheus | head -20  # task-service
curl http://localhost:8083/actuator/prometheus | head -20  # report-service
```

### Krok 2: Sprawdź czy Prometheus ma dane

Otwórz Prometheus UI: http://localhost:9090

Wykonaj zapytania:
```promql
# Sprawdź czy są metryki JVM
jvm_memory_used_bytes

# Sprawdź czy są metryki HTTP
http_server_requests_seconds_count

# Sprawdź czy są metryki Gateway
spring_cloud_gateway_requests_seconds_count
```

### Krok 3: Sprawdź konfigurację Grafany

1. Zaloguj się do Grafany: http://localhost:3000 (admin/admin)
2. Przejdź do: Configuration → Data Sources
3. Sprawdź czy "Prometheus" jest skonfigurowany i działa (przycisk "Save & Test")
4. URL powinien być: `http://prometheus:9090`

### Krok 4: Sprawdź czy dashboard jest załadowany

1. W Grafanie przejdź do: Dashboards → Browse
2. Szukaj: "GroupTaskManager - Spring Boot Metrics"
3. Jeśli nie ma, sprawdź logi Grafany:
   ```bash
   docker-compose -f docker-compose-monitoring.yml logs grafana | grep -i dashboard
   ```

### Krok 5: Sprawdź logi

```bash
# Logi Grafany
docker-compose -f docker-compose-monitoring.yml logs grafana

# Logi Prometheusa
docker-compose -f docker-compose-monitoring.yml logs prometheus

# Logi serwisów
docker-compose -f docker-compose-monitoring.yml logs api-gateway | grep -i actuator
```

## Częste problemy

### Problem: "No data" w panelach

**Przyczyna:** Metryki nie są jeszcze dostępne lub mają inne nazwy

**Rozwiązanie:**
1. Poczekaj 1-2 minuty (Prometheus scrapuje co 15s)
2. Sprawdź w Prometheus, jakie metryki są dostępne
3. Zaktualizuj zapytania w dashboardzie, jeśli nazwy się różnią

### Problem: Dashboard nie widzi Prometheusa

**Przyczyna:** Błędny URL datasource lub problem z siecią Docker

**Rozwiązanie:**
1. Sprawdź czy Prometheus działa: `curl http://localhost:9090/-/healthy`
2. W Grafanie: Configuration → Data Sources → Prometheus
3. URL powinien być: `http://prometheus:9090` (nie localhost!)
4. Kliknij "Save & Test"

### Problem: Metryki nie są eksportowane

**Przyczyna:** Brak zależności `micrometer-registry-prometheus`

**Rozwiązanie:**
1. Sprawdź `build.gradle` - powinno być:
   ```gradle
   implementation 'io.micrometer:micrometer-registry-prometheus'
   ```
2. Przebuduj serwisy:
   ```bash
   docker-compose -f docker-compose-monitoring.yml build
   docker-compose -f docker-compose-monitoring.yml up -d
   ```

### Problem: Dashboard nie ładuje się automatycznie

**Przyczyna:** Problem z provisioning

**Rozwiązanie:**
1. Sprawdź czy plik dashboardu jest w: `monitoring/grafana/dashboards/`
2. Sprawdź konfigurację: `monitoring/grafana/provisioning/dashboards/dashboard.yml`
3. Zrestartuj Grafanę:
   ```bash
   docker-compose -f docker-compose-monitoring.yml restart grafana
   ```

## Testowanie metryk

### Sprawdź dostępne metryki w Prometheus

```bash
# Lista wszystkich metryk
curl http://localhost:9090/api/v1/label/__name__/values | jq

# Sprawdź konkretną metrykę
curl 'http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes' | jq
```

### Sprawdź metryki bezpośrednio z serwisów

```bash
# API Gateway
curl http://localhost:8080/actuator/prometheus | grep -E "(jvm_|http_|spring_cloud_gateway_)" | head -10

# Auth Service
curl http://localhost:8081/actuator/prometheus | grep -E "(jvm_|http_)" | head -10

# Task Service
curl http://localhost:8082/actuator/prometheus | grep -E "(jvm_|http_)" | head -10

# Report Service
curl http://localhost:8083/actuator/prometheus | grep -E "(jvm_|http_)" | head -10
```

## Przydatne zapytania PromQL

```promql
# Request rate dla wszystkich serwisów
sum(rate(http_server_requests_seconds_count[5m])) by (application)

# Request rate dla Gateway
sum(rate(spring_cloud_gateway_requests_seconds_count[5m]))

# P95 latency
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, application))

# Memory usage
jvm_memory_used_bytes{area="heap"}

# CPU usage
process_cpu_usage

# Thread count
jvm_threads_live_threads
```

## Restart całego stacku monitoringu

```bash
# Zatrzymaj
docker-compose -f docker-compose-monitoring.yml stop prometheus grafana

# Usuń kontenery (zachowaj dane)
docker-compose -f docker-compose-monitoring.yml rm -f prometheus grafana

# Uruchom ponownie
docker-compose -f docker-compose-monitoring.yml up -d prometheus grafana

# Sprawdź logi
docker-compose -f docker-compose-monitoring.yml logs -f grafana prometheus
```

