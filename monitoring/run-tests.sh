#!/bin/bash

# Skrypt pomocniczy do uruchamiania testów obciążeniowych

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

# Docker Compose file dla monitoring setup
DOCKER_COMPOSE_FILE="docker-compose-monitoring.yml"

echo "=========================================="
echo "GroupTaskManager - Testy Obciążeniowe"
echo "=========================================="
echo "📦 Używanie: $DOCKER_COMPOSE_FILE"
echo ""

# Sprawdź, czy docker compose jest dostępny
if ! command -v docker compose &> /dev/null; then
    echo "❌ docker compose nie jest zainstalowany!"
    exit 1
fi

# Funkcja pomocnicza do wyświetlania menu
show_menu() {
    echo "Wybierz typ testu:"
    echo "1) Smoke Test (test podstawowy)"
    echo "2) Load Test (test obciążeniowy)"
    echo "3) Stress Test (test wytrzymałościowy)"
    echo "4) Uruchom wszystkie serwisy"
    echo "5) Zatrzymaj wszystkie serwisy"
    echo "6) Wyświetl logi"
    echo "0) Wyjście"
    echo ""
    read -p "Twój wybór: " choice
}

# Funkcja do uruchamiania testu
run_test() {
    local test_file=$1
    local test_name=$2
    
    echo ""
    echo "🚀 Uruchamianie $test_name..."
    echo ""
    
    # # Upewnij się że serwisy są uruchomione i sieć istnieje
    # echo "⚙️  Sprawdzanie stanu serwisów..."
    # docker compose -f "$DOCKER_COMPOSE_FILE" up -d api-gateway auth-service task-service report-service
    
    # echo "⏳ Czekanie na uruchomienie serwisów (30 sekund)..."
    # sleep 30
    
    # # Sprawdź czy API Gateway jest dostępny
    # echo "🔍 Sprawdzanie dostępności API Gateway..."
    # for i in {1..10}; do
    #     if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    #         echo "✅ API Gateway jest dostępny"
    #         break
    #     fi
    #     echo "⏳ Próba $i/10..."
    #     sleep 5
    # done
    
    # Uruchom test linux
#    docker run --rm \
#      --network=grouptaskmanager_gtm-network \
#      -e BASE_URL=http://api-gateway:8080 \
#      -v "$PROJECT_DIR/monitoring/k6:/scripts" \
#      grafana/k6:latest run /scripts/$test_file

    export MSYS_NO_PATHCONV=1
    export MSYS2_ARG_CONV_EXCL="*"

    # Windows
    docker run --rm \
      --network=grouptaskmanager_gtm-network \
      -e BASE_URL=http://api-gateway:8080 \
      -v "$(cygpath -w "$PROJECT_DIR/monitoring/k6"):/scripts" \
      grafana/k6:latest run /scripts/$test_file

    
    echo ""
    echo "✅ $test_name zakończony!"
    echo ""
}

# Funkcja do uruchamiania serwisów
start_services() {
    echo "🔧 Uruchamianie serwisów (Mikroserwisy + Monitoring)..."
    docker compose -f "$DOCKER_COMPOSE_FILE" up -d
    
    echo ""
    echo "⏳ Oczekiwanie na uruchomienie serwisów (60 sekund)..."
    sleep 60
    
    echo ""
    echo "✅ Serwisy uruchomione!"
    echo ""
    echo "Dostępne serwisy:"
    echo "  🔐 Auth Service:    http://localhost:8081"
    echo "  📋 Task Service:    http://localhost:8082"
    echo "  📊 Report Service:  http://localhost:8083"
    echo "  🚪 API Gateway:     http://localhost:8080"
    echo "  🌐 Frontend:        http://localhost:4200"
    echo ""
    echo "📈 Monitoring:"
    echo "  📉 Prometheus:      http://localhost:9090"
    echo "  📊 Grafana:         http://localhost:3000 (admin/admin)"
    echo ""
}

# Funkcja do zatrzymywania serwisów
stop_services() {
    echo "🛑 Zatrzymywanie serwisów..."
    docker compose -f "$DOCKER_COMPOSE_FILE" down
    echo "✅ Serwisy zatrzymane!"
}

# Funkcja do wyświetlania logów
show_logs() {
    echo "Wybierz serwis do wyświetlenia logów:"
    echo "1) API Gateway"
    echo "2) Auth Service"
    echo "3) Task Service"
    echo "4) Report Service"
    echo "5) Prometheus"
    echo "6) Grafana"
    echo "7) Wszystkie"
    echo ""
    read -p "Twój wybór: " log_choice
    
    case $log_choice in
        1) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f api-gateway ;;
        2) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f auth-service ;;
        3) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f task-service ;;
        4) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f report-service ;;
        5) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f prometheus ;;
        6) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f grafana ;;
        7) docker compose -f "$DOCKER_COMPOSE_FILE" logs -f ;;
        *) echo "Nieprawidłowy wybór" ;;
    esac
}

# Główna pętla
while true; do
    show_menu
    
    case $choice in
        1)
            run_test "smoke-test.js" "Smoke Test"
            ;;
        2)
            run_test "load-test.js" "Load Test"
            ;;
        3)
            run_test "stress-test.js" "Stress Test"
            ;;
        4)
            start_services
            ;;
        5)
            stop_services
            ;;
        6)
            show_logs
            ;;
        0)
            echo "Do widzenia!"
            exit 0
            ;;
        *)
            echo "Nieprawidłowy wybór. Spróbuj ponownie."
            ;;
    esac
    
    echo ""
    read -p "Naciśnij Enter, aby kontynuować..."
    echo ""
done

