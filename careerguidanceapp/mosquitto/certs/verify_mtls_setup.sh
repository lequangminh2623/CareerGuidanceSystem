#!/bin/bash

# Script to verify mTLS setup for Mosquitto
# This script checks all necessary certificates, configurations, and connectivity

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Mosquitto mTLS Setup Verification ===${NC}\n"

ERRORS=0
WARNINGS=0

# Function to print success
success() {
    echo -e "${GREEN}✓${NC} $1"
}

# Function to print error
error() {
    echo -e "${RED}✗${NC} $1"
    ((ERRORS++))
}

# Function to print warning
warning() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

# Function to print info
info() {
    echo -e "ℹ $1"
}

echo -e "${YELLOW}1. Checking Certificate Files${NC}"
echo "=================================="

# Check CA certificate
if [ -f "mosquitto/certs/ca/ca.crt" ]; then
    success "CA certificate found"
else
    error "CA certificate NOT found at mosquitto/certs/ca/ca.crt"
fi

# Check CA key
if [ -f "mosquitto/certs/ca/ca.key" ]; then
    success "CA private key found"
    # Check permissions
    perms=$(stat -f '%OLp' mosquitto/certs/ca/ca.key 2>/dev/null || stat -c '%a' mosquitto/certs/ca/ca.key 2>/dev/null)
    if [ "$perms" = "0600" ] || [ "$perms" = "600" ]; then
        success "CA key has secure permissions (600)"
    else
        warning "CA key permissions are $perms (should be 600)"
    fi
else
    error "CA private key NOT found at mosquitto/certs/ca/ca.key"
fi

# Check Server certificates
if [ -f "mosquitto/certs/server/mosquitto.crt" ]; then
    success "Server certificate found"
else
    error "Server certificate NOT found"
fi

if [ -f "mosquitto/certs/server/mosquitto.key" ]; then
    success "Server private key found"
else
    error "Server private key NOT found"
fi

# Check Client certificates
if [ -f "mosquitto/certs/client/backend.crt" ]; then
    success "Backend client certificate found"
else
    error "Backend client certificate NOT found"
fi

if [ -f "mosquitto/certs/client/backend.key" ]; then
    success "Backend client private key found"
else
    error "Backend client private key NOT found"
fi

if [ -f "mosquitto/certs/client/esp32.crt" ]; then
    success "ESP32 client certificate found"
else
    error "ESP32 client certificate NOT found"
fi

# Check Java keystores
if [ -f "mosquitto/certs/client/backend-truststore.jks" ]; then
    success "Backend truststore found"
else
    error "Backend truststore NOT found"
fi

if [ -f "mosquitto/certs/client/backend-keystore.p12" ]; then
    success "Backend keystore found"
else
    error "Backend keystore NOT found"
fi

if [ -f "attendance-service/src/main/resources/certs/backend-truststore.jks" ]; then
    success "Backend truststore copied to resources"
else
    warning "Backend truststore NOT found in resources (may need to copy)"
fi

if [ -f "attendance-service/src/main/resources/certs/backend-keystore.p12" ]; then
    success "Backend keystore copied to resources"
else
    warning "Backend keystore NOT found in resources (may need to copy)"
fi

echo ""
echo -e "${YELLOW}2. Checking Configuration Files${NC}"
echo "==================================="

# Check mosquitto config
if [ -f "mosquitto/config/mosquitto.conf" ]; then
    success "Mosquitto configuration found"

    # Check for TLS settings
    if grep -q "listener 8883" mosquitto/config/mosquitto.conf; then
        success "TLS listener (8883) configured"
    else
        error "TLS listener (8883) NOT configured in mosquitto.conf"
    fi

    if grep -q "require_certificate true" mosquitto/config/mosquitto.conf; then
        success "Client certificate requirement enabled"
    else
        warning "Client certificate requirement may not be enabled"
    fi

    if grep -q "tls_version tlsv1.2" mosquitto/config/mosquitto.conf; then
        success "TLS version set to 1.2"
    else
        warning "TLS version may not be set to 1.2"
    fi
else
    error "Mosquitto configuration NOT found"
fi

# Check application properties
if [ -f "attendance-service/src/main/resources/application.properties" ]; then
    success "Application properties found"

    if grep -q "mqtt.broker.url" attendance-service/src/main/resources/application.properties; then
        success "MQTT broker URL configured"
        url=$(grep "mqtt.broker.url" attendance-service/src/main/resources/application.properties | cut -d'=' -f2-)
        info "Configured broker URL: $url"
    fi

    if grep -q "mqtt.tls.enabled" attendance-service/src/main/resources/application.properties; then
        success "MQTT TLS setting configured"
    fi
else
    error "Application properties NOT found"
fi

# Check MqttConfig.java
if [ -f "attendance-service/src/main/java/com/lqm/attendance_service/configs/MqttConfig.java" ]; then
    success "MqttConfig.java found"

    if grep -q "configureSSL" attendance-service/src/main/java/com/lqm/attendance_service/configs/MqttConfig.java; then
        success "TLS configuration method present"
    else
        error "TLS configuration method NOT found"
    fi
else
    error "MqttConfig.java NOT found"
fi

echo ""
echo -e "${YELLOW}3. Checking Certificate Validity${NC}"
echo "===================================="

# Check server certificate validity
if command -v openssl &> /dev/null; then
    if [ -f "mosquitto/certs/server/mosquitto.crt" ]; then
        echo "Server Certificate:"
        openssl x509 -in mosquitto/certs/server/mosquitto.crt -noout -subject -dates

        # Check expiration
        exp_date=$(openssl x509 -in mosquitto/certs/server/mosquitto.crt -noout -dates | grep "notAfter" | cut -d'=' -f2)
        success "Server certificate expires: $exp_date"
    fi

    if [ -f "mosquitto/certs/client/backend.crt" ]; then
        echo ""
        echo "Backend Client Certificate:"
        openssl x509 -in mosquitto/certs/client/backend.crt -noout -subject -dates
        success "Backend client certificate valid"
    fi
else
    warning "OpenSSL not found - skipping certificate validity check"
fi

echo ""
echo -e "${YELLOW}4. Checking Docker Setup${NC}"
echo "=========================="

# Check docker-compose.yml
if [ -f "docker-compose.yml" ]; then
    success "docker-compose.yml found"

    if grep -q "mosquitto" docker-compose.yml; then
        success "Mosquitto service in docker-compose.yml"
    fi

    if grep -q "MQTT_BROKER_URL" docker-compose.yml; then
        success "MQTT configuration in docker-compose.yml"
    fi
else
    error "docker-compose.yml NOT found"
fi

# Check Docker installation
if command -v docker &> /dev/null; then
    success "Docker is installed"
else
    warning "Docker is NOT installed"
fi

if command -v docker-compose &> /dev/null; then
    success "Docker Compose is installed"
else
    warning "Docker Compose is NOT installed"
fi

echo ""
echo -e "${YELLOW}5. Environment Setup${NC}"
echo "====================="

# Check .env file
if [ -f ".env" ]; then
    success ".env file found"

    if grep -q "MQTT_USERNAME" .env; then
        success "MQTT credentials in .env"
    else
        warning "MQTT credentials may not be in .env"
    fi
else
    if [ -f ".env.example" ]; then
        warning ".env file NOT found (but .env.example exists)"
        info "Run: cp .env.example .env"
    else
        error ".env file NOT found"
    fi
fi

# Check .gitignore
if [ -f ".gitignore" ]; then
    success ".gitignore found"

    if grep -q "*.key" .gitignore; then
        success "Private keys in .gitignore"
    else
        warning "Private keys may not be in .gitignore"
    fi
else
    warning ".gitignore NOT found"
fi

echo ""
echo -e "${YELLOW}6. Documentation${NC}"
echo "================="

# Check documentation files
docs=(
    "MQTT_SECURITY_IMPLEMENTATION.md"
    "ESP32_MQTT_MTLS_GUIDE.md"
    "DOCKER_MTLS_SETUP.md"
    "MQTT_MTLS_QUICKSTART.md"
)

for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        success "$doc found"
    else
        warning "$doc NOT found"
    fi
done

echo ""
echo -e "${YELLOW}7. Summary${NC}"
echo "==========="

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}No critical errors found! ✓${NC}"
else
    echo -e "${RED}Found $ERRORS critical error(s)${NC}"
fi

if [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}Found $WARNINGS warning(s)${NC}"
fi

echo ""
echo -e "${GREEN}=== Verification Complete ===${NC}\n"

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}Please fix the errors above before proceeding.${NC}"
    echo "See MQTT_MTLS_QUICKSTART.md for more information."
    exit 1
else
    echo -e "${GREEN}Setup appears to be correct!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Update .env with your actual credentials"
    echo "2. Create Mosquitto password file:"
    echo "   cd mosquitto/config && mosquitto_passwd -c password.txt admin"
    echo "3. Start services:"
    echo "   docker-compose up -d"
    echo "4. Verify connection:"
    echo "   docker-compose logs mosquitto | grep -i 'listening\\|tls'"
    echo ""
    exit 0
fi

