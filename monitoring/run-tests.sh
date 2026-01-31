#!/bin/bash

# Skrypt pomocniczy do uruchamiania testów obciążeniowych

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

# Docker Compose file
DOCKER_COMPOSE_FILE="docker-compose.yml"

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
    
    # Uruchom test używając docker compose run (automatycznie używa sieci z docker-compose)
    docker compose run --rm k6 run /scripts/$test_file
    
    echo ""
    echo "✅ $test_name zakończony!"
    echo ""
}

# Funkcja do uruchamiania serwisów
start_services() {
    echo "🔧 Uruchamianie serwisów (Backend + Monitoring)..."
    docker compose up -d
    
    echo ""
    echo "⏳ Oczekiwanie na uruchomienie serwisów (60 sekund)..."
    sleep 60
    
    echo ""
    echo "✅ Serwisy uruchomione!"
    echo ""
    echo "Dostępne serwisy:"
    echo "  🚀 Backend:         http://localhost:8080"
    echo ""
    echo "📈 Monitoring:"
    echo "  📉 Node Exporter:   http://localhost:9100"
    echo "  📉 Prometheus:      http://localhost:9090"
    echo "  📊 Grafana:         http://localhost:3000 (admin/admin)"
    echo ""
}

# Funkcja do zatrzymywania serwisów
stop_services() {
    echo "🛑 Zatrzymywanie serwisów..."
    docker compose down
    echo "✅ Serwisy zatrzymane!"
}

# Funkcja do wyświetlania logów
show_logs() {
    echo "Wybierz serwis do wyświetlenia logów:"
    echo "1) Backend"
    echo "2) Prometheus"
    echo "3) Grafana"
    echo "4) Node Exporter"
    echo "5) PostgreSQL"
    echo "6) Wszystkie"
    echo ""
    read -p "Twój wybór: " log_choice
    
    case $log_choice in
        1) docker compose logs -f backend ;;
        2) docker compose logs -f prometheus ;;
        3) docker compose logs -f grafana ;;
        4) docker compose logs -f node-exporter ;;
        5) docker compose logs -f postgres ;;
        6) docker compose logs -f ;;
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
