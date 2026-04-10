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
                                    status VARCHAR(20) NOT NULL,

                                    CONSTRAINT unique_daily_attendance UNIQUE (student_id, attendance_date)
);

CREATE INDEX idx_search_attendance ON attendances(attendance_date, classroom_id);