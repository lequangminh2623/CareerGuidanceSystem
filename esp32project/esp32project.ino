#include "certs.h"
#include <Adafruit_Fingerprint.h>
#include <ESPmDNS.h>
#include <LiquidCrystal_I2C.h>
#include <Preferences.h>
#include <PubSubClient.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <time.h>

// --- CẤU HÌNH MẠNG & MQTT ---
const char *ssid = "Home";
const char *password = "1334815home";
const char *mqtt_hostname = "server.local";
const int mqtt_port = 8883;

// --- ĐỐI TƯỢNG TOÀN CỤC ---
WiFiClientSecure espClientSecure;
PubSubClient client(espClientSecure);
Preferences preferences;
LiquidCrystal_I2C lcd(0x27, 16, 2);
HardwareSerial mySerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&mySerial);

// --- CẤU HÌNH CHÂN (PINS) ---
const int btnPower = 14;
const int btnDiscover = 12;
const int buzzer = 4;
const int fingerMOSFET = 5;

// --- TRẠNG THÁI HỆ THỐNG ---
bool systemActive = true;
bool isEnrolling = false;
int targetEnrollId = 0;
String targetEnrollName = "";
String chipId;

// --- MQTT TOPICS ---
const String TOPIC_DISCOVER_DEVICE = "device/discover";
const String TOPIC_ENROLL_FINGERPRINT = "fingerprint/enroll";
const String TOPIC_TAKE_ATTENDANCE = "attendance/take";
const String TOPIC_CHANGE_DEVICE_STATUS = "device/change-status";
const String TOPIC_ENROLL_CMD = "fingerprint/enroll/";
const String TOPIC_CHANGE_STATUS_CMD = "device/change-status/";
const String TOPIC_DELETE_CMD = "fingerprint/delete/";

// ======================= UI & HARDWARE HELPERS =======================
void showLCD(String l1, String l2) {
  if (!systemActive) return;
  lcd.clear();
  lcd.setCursor(0, 0); lcd.print(l1.substring(0, 16));
  lcd.setCursor(0, 1); lcd.print(l2.substring(0, 16));
}

void beep(int times) {
  for (int i = 0; i < times; i++) {
    digitalWrite(buzzer, HIGH); delay(100);
    digitalWrite(buzzer, LOW); delay(100);
  }
}

void showReady() {
  if (systemActive) showLCD("Ready", "ID: " + chipId.substring(6));
}

void toggleSystemPower(bool turnOn) {
  systemActive = turnOn;
  if (!systemActive) {
    beep(2);
    lcd.noBacklight();
    lcd.noDisplay();
    digitalWrite(fingerMOSFET, LOW);
  } else {
    lcd.display();
    lcd.backlight();
    digitalWrite(fingerMOSFET, HIGH);
    delay(200); 
    finger.begin(57600);
    showLCD("System Activated", "Ready...");
    beep(1);
    delay(1000);
    showReady();
  }
}

// ======================= MQTT HANDLERS (SRP) =======================
void handleEnrollCommand(String message) {
  if (!systemActive) return;

  if (message == "ADD" || message.startsWith("ADD:")) {
    isEnrolling = true;
    targetEnrollId = 0;
    targetEnrollName = "Starting...";
    if (message.startsWith("ADD:")) {
      targetEnrollName = message.substring(4);
    }
    showLCD("Enroll Mode", targetEnrollName);
    beep(1);
  } else if (message.startsWith("EDIT:")) {
    isEnrolling = true;
    int firstColon = message.indexOf(":");
    int secondColon = message.indexOf(":", firstColon + 1);

    if (secondColon != -1) {
      targetEnrollId = message.substring(firstColon + 1, secondColon).toInt();
      targetEnrollName = message.substring(secondColon + 1);
    } else {
      targetEnrollId = message.substring(firstColon + 1).toInt();
      // Lấy tên từ Preferences nếu Server không gửi kèm tên
      targetEnrollName = preferences.getString(("uid" + String(targetEnrollId)).c_str(), "ID: " + String(targetEnrollId));
    }
    showLCD("Edit Mode", targetEnrollName);
    beep(1);
  } else if (message == "CANCEL") {
    isEnrolling = false;
    showLCD("Enroll Cancelled", "By Server");
    beep(2);
    delay(1500);
    showReady();
  }
}

void handleStatusCommand(String message) {
  if (message == "ON" && !systemActive) {
    toggleSystemPower(true);
  } else if (message == "OFF" && systemActive) {
    toggleSystemPower(false);
  }
}

void handleDeleteCommand(String message) {
  if (!systemActive) return;

  int idxStart = message.indexOf(":");
  if (idxStart >= 0) {
    String valueStr = message.substring(idxStart + 1, message.lastIndexOf("}"));
    valueStr.replace("\"", "");
    valueStr.trim();

    if (valueStr == "ALL") {
      showLCD("Deleting All...", "Please wait");
      if (finger.emptyDatabase() == FINGERPRINT_OK) {
        preferences.clear();
        showLCD("Cleared All!", "DB & Cache Empty");
        beep(1);
      } else {
        showLCD("Clear Failed", "Storage Error");
        beep(3);
      }
      delay(2000);
      showReady();
      return;
    }

    int fingerprintIndex = valueStr.toInt();
    
    // Tìm tên trong bộ nhớ trước khi xóa để hiển thị
    String targetDeleteName = preferences.getString(("uid" + String(fingerprintIndex)).c_str(), "ID: " + String(fingerprintIndex));
    
    showLCD("Deleting FP...", targetDeleteName);

    if (finger.deleteModel(fingerprintIndex) == FINGERPRINT_OK) {
      preferences.remove(("uid" + String(fingerprintIndex)).c_str());
      showLCD("Deleted Success!", targetDeleteName);
      beep(1);
    } else {
      showLCD("Delete Failed", targetDeleteName);
      beep(2);
    }
    delay(2000);
    showReady();
  }
}

void mqttCallback(char *topic, byte *payload, unsigned int length) {
  String message = "";
  for (unsigned int i = 0; i < length; i++) message += (char)payload[i];
  String topicStr = String(topic);

  if (topicStr.startsWith(TOPIC_ENROLL_CMD)) {
    handleEnrollCommand(message);
  } else if (topicStr.startsWith(TOPIC_CHANGE_STATUS_CMD)) {
    handleStatusCommand(message);
  } else if (topicStr.startsWith(TOPIC_DELETE_CMD)) {
    handleDeleteCommand(message);
  }
}

void maintainMQTTConnection() {
  if (WiFi.status() != WL_CONNECTED) return;
  if (client.connected()) {
    client.loop();
    return;
  }

  client.setServer(mqtt_hostname, mqtt_port);
  if (systemActive) showLCD("MQTT Connect", "Securing mTLS...");

  if (client.connect(chipId.c_str())) {
    client.subscribe((TOPIC_ENROLL_CMD + chipId).c_str());
    client.subscribe((TOPIC_CHANGE_STATUS_CMD + chipId).c_str());
    client.subscribe((TOPIC_DELETE_CMD + chipId).c_str());

    String statusMsg = "{\"id\":\"" + chipId + "\",\"isActive\":" + String(systemActive ? "true" : "false") + "}";
    client.publish(TOPIC_CHANGE_DEVICE_STATUS.c_str(), statusMsg.c_str());

    if (systemActive) {
      showLCD("MQTT Online", "Secure mTLS");
      delay(1000);
      showReady();
    }
  } else {
    if (systemActive) showLCD("MQTT Failed", "Retrying...");
    delay(5000);
  }
}

// ======================= ATTENDANCE & ENROLL =======================
void publishEnrollError() {
  isEnrolling = false;
  String errRes = "{\"id\":\"" + chipId + "\",\"index\":0}";
  client.publish(TOPIC_ENROLL_FINGERPRINT.c_str(), errRes.c_str());
  showLCD("Enroll Failed", "Try again");
  beep(3);
  delay(2000);
  showReady();
}

void runEnrollment() {
  int id = (targetEnrollId > 0) ? targetEnrollId : (finger.getTemplateCount(), finger.templateCount + 1);

  String displayName = (targetEnrollName != "" && targetEnrollName != "Starting...") ? targetEnrollName : ("ID: " + String(id));

  // Bước 1: Quét lần 1
  showLCD("Scan Finger", displayName);
  while (finger.getImage() != FINGERPRINT_OK) {
    if (!systemActive || !isEnrolling) return;
    client.loop(); delay(100);
  }
  if (finger.image2Tz(1) != FINGERPRINT_OK) return publishEnrollError();
  beep(1);

  // Chờ nhấc tay ra
  showLCD("Remove Finger", "");
  while (finger.getImage() != FINGERPRINT_NOFINGER) {
    if (!systemActive || !isEnrolling) return;
    client.loop(); delay(50);
  }

  // Bước 2: Quét lần 2
  showLCD("Place Again", displayName);
  while (finger.getImage() != FINGERPRINT_OK) {
    if (!systemActive || !isEnrolling) return;
    client.loop(); delay(100);
  }
  if (finger.image2Tz(2) != FINGERPRINT_OK) return publishEnrollError();

  // Lưu vân tay
  if (finger.createModel() == FINGERPRINT_OK && finger.storeModel(id) == FINGERPRINT_OK) {
    String res = "{\"id\":\"" + chipId + "\",\"index\":" + String(id) + "}";
    client.publish(TOPIC_ENROLL_FINGERPRINT.c_str(), res.c_str());

    if (targetEnrollName != "" && targetEnrollName != "Starting..." && !targetEnrollName.startsWith("ID: ")) {
      preferences.putString(("uid" + String(id)).c_str(), targetEnrollName);
    }

    showLCD("Saved Success!", displayName);
    beep(2);
  } else {
    publishEnrollError();
    return;
  }

  isEnrolling = false;
  delay(2000);
  showReady();
}

void processAttendance() {
  if (finger.getImage() == FINGERPRINT_OK && finger.image2Tz() == FINGERPRINT_OK) {
    if (finger.fingerFastSearch() == FINGERPRINT_OK) {
      String logMsg = "{\"deviceId\":\"" + chipId + "\",\"fingerprintIndex\":" + String(finger.fingerID) + "}";
      client.publish(TOPIC_TAKE_ATTENDANCE.c_str(), logMsg.c_str());

      String studentName = preferences.getString(("uid" + String(finger.fingerID)).c_str(), "ID: " + String(finger.fingerID));
      showLCD("Checked In!", studentName);
      beep(1);
    } else {
      showLCD("Access Denied", "Unknown Finger");
      beep(3);
    }
    delay(2000);
    showReady();
  }
}

// ======================= BUTTON HANDLERS =======================
void handleButtons() {
  if (systemActive && digitalRead(btnDiscover) == LOW) {
    delay(50); 
    if (digitalRead(btnDiscover) == LOW) {
      String discoverMsg = "{\"chipId\":\"" + chipId + "\"}";
      client.publish(TOPIC_DISCOVER_DEVICE.c_str(), discoverMsg.c_str());
      showLCD("Discovering...", "Sent to server");
      beep(1);
      while (digitalRead(btnDiscover) == LOW) delay(50);
      delay(1500);
      showReady();
    }
  }

  if (digitalRead(btnPower) == LOW) {
    delay(50);
    if (digitalRead(btnPower) == LOW) {
      toggleSystemPower(!systemActive);
      String statusMsg = "{\"id\":\"" + chipId + "\",\"isActive\":" + String(systemActive ? "true" : "false") + "}";
      client.publish(TOPIC_CHANGE_DEVICE_STATUS.c_str(), statusMsg.c_str());
      while (digitalRead(btnPower) == LOW) delay(50);
      delay(500);
    }
  }
}

// ======================= INITIALIZATION =======================
void setupNetworkAndTime() {
  WiFi.begin(ssid, password);
  showLCD("Connecting WiFi", "SSID: Home");
  while (WiFi.status() != WL_CONNECTED) { delay(500); }
  showLCD("WiFi Connected", WiFi.localIP().toString());
  delay(1000);

  showLCD("Syncing Time", "Please wait...");
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");

  struct tm timeinfo;
  int retries = 0;
  while (!getLocalTime(&timeinfo) || timeinfo.tm_year + 1900 < 2024) {
    delay(500);
    if (++retries > 30) {
      showLCD("Time Sync Failed", "Skipping...");
      delay(1500);
      return;
    }
  }
  showLCD("Time Synced!", "Ready for mTLS");
  delay(1000);
}

void setup() {
  pinMode(btnPower, INPUT_PULLUP);
  pinMode(btnDiscover, INPUT_PULLUP);
  pinMode(buzzer, OUTPUT);
  pinMode(fingerMOSFET, OUTPUT);
  digitalWrite(fingerMOSFET, HIGH);

  lcd.init();
  lcd.backlight();
  showLCD("System Booting", "Initialize...");

  chipId = WiFi.macAddress();
  chipId.replace(":", "");

  preferences.begin("names", false);
  setupNetworkAndTime();
  MDNS.begin("esp32-attendance");

  espClientSecure.setCACert((const char *)ca_crt_hex);
  espClientSecure.setCertificate((const char *)client_crt_hex);
  espClientSecure.setPrivateKey((const char *)client_key_hex);
  client.setCallback(mqttCallback);

  finger.begin(57600);
  showReady();
}

// ======================= MAIN LOOP =======================
void loop() {
  maintainMQTTConnection();
  handleButtons();

  if (systemActive) {
    if (isEnrolling) {
      runEnrollment();
    } else {
      processAttendance();
    }
  }
  delay(50);
}
