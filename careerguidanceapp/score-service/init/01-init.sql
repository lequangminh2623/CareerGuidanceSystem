CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE score_details (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    final_score DOUBLE PRECISION,
    midterm_score DOUBLE PRECISION,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    section_id UUID NOT NULL,
    student_id UUID NOT NULL,

    CONSTRAINT uq_student_id_section_id UNIQUE (student_id, section_id),
    CONSTRAINT chk_final_score CHECK (final_score >= 0 AND final_score <= 10),
    CONSTRAINT chk_midterm_score CHECK (midterm_score >= 0 AND midterm_score <= 10)
);

-- Tạo bảng ExtraScore
CREATE TABLE extra_scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    score DOUBLE PRECISION,
    score_index INTEGER NOT NULL,
    score_detail_id UUID NOT NULL,

    CONSTRAINT fk_score_details FOREIGN KEY (score_detail_id) REFERENCES score_details(id) ON DELETE CASCADE,
    CONSTRAINT uq_detail_index UNIQUE (score_detail_id, score_index),
    CONSTRAINT chk_score CHECK (score >= 0 AND score <= 10)
);