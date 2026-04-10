#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CA_DIR="$SCRIPT_DIR/ca"
SERVER_DIR="$SCRIPT_DIR/server"
CLIENT_DIR="$SCRIPT_DIR/client"

mkdir -p "$CA_DIR" "$SERVER_DIR" "$CLIENT_DIR"

echo "=== 1. Tạo Certificate Authority (CA) ==="
openssl genrsa -out "$CA_DIR/ca.key" 2048
openssl req -new -x509 -days 3650 -key "$CA_DIR/ca.key" -out "$CA_DIR/ca.crt" \
  -subj "/C=VN/ST=HCM/L=HCM/O=CareerGuidance/CN=MyLocalCA"

echo "=== 2. Tạo Mosquitto Server Certificate (mDNS: server.local) ==="
openssl genrsa -out "$SERVER_DIR/mosquitto.key" 2048
openssl req -new -key "$SERVER_DIR/mosquitto.key" -out "$SERVER_DIR/mosquitto.csr" \
  -subj "/C=VN/ST=HCM/L=HCM/O=CareerGuidance/CN=server.local"

# Tạo SAN file chuẩn
cat > /tmp/san.cnf << EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = DNS:server,DNS:server.local,DNS:mosquitto,DNS:localhost,IP:127.0.0.1,IP:192.168.1.12
EOF

openssl x509 -req -in "$SERVER_DIR/mosquitto.csr" -CA "$CA_DIR/ca.crt" -CAkey "$CA_DIR/ca.key" \
  -CAcreateserial -out "$SERVER_DIR/mosquitto.crt" -days 3650 -extfile /tmp/san.cnf

echo "=== 3. Tạo Backend Client Certificate (Java) ==="
openssl genrsa -out "$CLIENT_DIR/backend.key" 2048
openssl req -new -key "$CLIENT_DIR/backend.key" -out "$CLIENT_DIR/backend.csr" \
  -subj "/C=VN/ST=HCM/L=HCM/O=CareerGuidance/CN=backend-client"
openssl x509 -req -in "$CLIENT_DIR/backend.csr" -CA "$CA_DIR/ca.crt" -CAkey "$CA_DIR/ca.key" \
  -CAcreateserial -out "$CLIENT_DIR/backend.crt" -days 3650

echo "=== 4. Tạo ESP32 Client Certificate (v3 & RSA Traditional) ==="

# 4.1 Tạo thẳng Private Key định dạng RSA Traditional
openssl genrsa -out "$CLIENT_DIR/esp32_rsa.key" 2048

# 4.2 Tạo Certificate Signing Request (CSR)
openssl req -new -key "$CLIENT_DIR/esp32_rsa.key" -out "$CLIENT_DIR/esp32.csr" \
  -subj "/C=VN/ST=HCM/L=HCM/O=CareerGuidance/CN=esp32-client"

# 4.3 Tạo file cấu hình Extension cho Client (Để ép lên v3 có đủ thuộc tính)
cat > /tmp/client_ext.cnf << EOF
basicConstraints = CA:FALSE
nsCertType = client, email
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
EOF

# 4.4 Ký xác nhận để ra file .crt Version 3 hoàn chỉnh
openssl x509 -req -in "$CLIENT_DIR/esp32.csr" -CA "$CA_DIR/ca.crt" -CAkey "$CA_DIR/ca.key" \
  -CAcreateserial -out "$CLIENT_DIR/esp32.crt" -days 3650 -extfile /tmp/client_ext.cnf

# Dọn dẹp file tạm
rm /tmp/client_ext.cnf

echo "=== 5. Tạo Java Keystores (Backend) ==="
# Export sang PKCS12 (Chuẩn hiện đại cho Java)
openssl pkcs12 -export -in "$CLIENT_DIR/backend.crt" -inkey "$CLIENT_DIR/backend.key" \
  -out "$CLIENT_DIR/backend-keystore.p12" -name backend-client -passout pass:mosquitto

# Tạo Truststore chứa CA để Java tin tưởng Broker
keytool -import -alias ca -file "$CA_DIR/ca.crt" -keystore "$CLIENT_DIR/backend-truststore.jks" \
  -storepass mosquitto -noprompt

# Dọn dẹp và bảo mật
rm -f /tmp/san.cnf "$CA_DIR"/*.srl "$SERVER_DIR"/*.csr "$CLIENT_DIR"/*.csr
chmod 600 "$CA_DIR"/*.key "$SERVER_DIR"/*.key "$CLIENT_DIR"/*.key

echo "✅ Đã tạo xong tất cả chứng chỉ!"
