CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Bảng thiết bị
CREATE TABLE devices (
                         id VARCHAR(50) PRIMARY KEY, -- Chip ID đọc từ code ESP32
                         classroom_id UUID,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         CONSTRAINT unique_classroom_device UNIQUE (classroom_id)
);

-- Bảng ánh xạ Vân tay
CREATE TABLE fingerprints (
                              fingerprint_index INT NOT NULL,
                              classroom_id UUID NOT NULL,
                              student_id UUID NOT NULL,
                              PRIMARY KEY (fingerprint_index, classroom_id)
);

-- Bảng bản ghi điểm danh
CREATE TABLE attendances (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    student_id UUID NOT NULL,
                                    classroom_id UUID NOT NULL,
                                    attendance_date DATE NOT NULL,
                                    check_in_time TIME,
                                    status VARCHAR(255) NOT NULL,
                                    session VARCHAR(255) CHECK (session IN ('MORNING', 'AFTERNOON')),

                                    CONSTRAINT unique_daily_attendance UNIQUE (student_id, attendance_date, session)
);

CREATE INDEX idx_search_attendance ON attendances(attendance_date, classroom_id);

-- Bảng cấu hình điểm danh (singleton, id=1)
CREATE TABLE attendance_config (
                                    id BIGINT PRIMARY KEY DEFAULT 1,
                                    sessions_per_day INT NOT NULL DEFAULT 1,
                                    morning_start_time TIME NOT NULL DEFAULT '07:00',
                                    morning_end_time TIME NOT NULL DEFAULT '11:30',
                                    afternoon_start_time TIME NOT NULL DEFAULT '13:00',
                                    afternoon_end_time TIME NOT NULL DEFAULT '17:00'
);