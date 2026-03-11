CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Bảng Year
CREATE TABLE years (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name CHAR(9) UNIQUE NOT NULL -- "2024-2025"
);

-- Bảng Semester với ENUM SemesterType
CREATE TABLE semesters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name CHAR(10) NOT NULL CHECK (name IN ('SEMESTER_1', 'SEMESTER_2')),
    year_id UUID NOT NULL,

    CONSTRAINT fk_years FOREIGN KEY (year_id) REFERENCES years(id) ON DELETE CASCADE,
    CONSTRAINT uq_year_id_name UNIQUE(year_id, name)
);
CREATE INDEX idx_semesters_year_id ON semesters(year_id);

-- Bảng Grade với Enum GradeType
CREATE TABLE grades (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name CHAR(8) NOT NULL CHECK (name IN ('GRADE_10', 'GRADE_11', 'GRADE_12')),
    year_id UUID NOT NULL,

    CONSTRAINT fk_years FOREIGN KEY (year_id) REFERENCES years(id) ON DELETE CASCADE,
    CONSTRAINT uq_year_id_name UNIQUE(year_id, name)
);
CREATE INDEX idx_grades_year_id ON grades(year_id);

-- Bảng course
CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Bảng Classroom
CREATE TABLE classrooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    grade_id UUID NOT NULL,

    CONSTRAINT fk_grades FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE RESTRICT,
    CONSTRAINT uq_grade_id_name UNIQUE(grade_id, name)
);
CREATE INDEX idx_classrooms_grade_id ON classrooms(grade_id);

-- Bảng trung gian Classroom User (Student)
CREATE TABLE students_classrooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL,
    classroom_id UUID NOT NULL,

    CONSTRAINT fk_classrooms FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE RESTRICT,
    CONSTRAINT uq_classroom_id_student_id UNIQUE (classroom_id, student_id)
);
CREATE INDEX idx_students_classrooms_student_id ON students_classrooms(student_id);

-- Bảng Curriculums (Chương trình học)
CREATE TABLE curriculums (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    grade_id UUID NOT NULL,
    semester_id UUID NOT NULL,
    subject_id UUID NOT NULL,

    CONSTRAINT fk_grades FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE CASCADE,
    CONSTRAINT fk_semesters FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE CASCADE,
    CONSTRAINT fk_subjects FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,

    CONSTRAINT uq_grade_semester_subject UNIQUE(grade_id, semester_id, subject_id)
);
CREATE INDEX idx_curriculums_grade_id ON curriculums(grade_id);
CREATE INDEX idx_curriculums_semester_id ON curriculums(semester_id);
CREATE INDEX idx_curriculums_subject_id ON curriculums(subject_id);

-- Bảng Sections (Lớp học phần / Nhóm môn học)
CREATE TABLE sections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    teacher_id UUID,
    score_status VARCHAR(50) NOT NULL,
    classroom_id UUID NOT NULL,
    curriculum_id UUID NOT NULL,

    CONSTRAINT fk_classrooms FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_curriculums FOREIGN KEY (curriculum_id) REFERENCES curriculums(id) ON DELETE CASCADE,
    CONSTRAINT uq_classroom_curriculum UNIQUE(classroom_id, curriculum_id)
);
CREATE INDEX idx_sections_classroom_id ON sections(classroom_id);
CREATE INDEX idx_sections_curriculum_id ON sections(curriculum_id);
CREATE INDEX idx_sections_teacher_id ON sections(teacher_id);
