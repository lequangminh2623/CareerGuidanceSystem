CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL CHECK (email ~ '^[A-Za-z0-9._%+-]+@ou\.edu\.vn$'),
    password VARCHAR(255) NOT NULL DEFAULT '$2a$10$71V52hYwqLtIpJJmfhGlS.nFxABwu.ovUo/siyNVCB2r4Sqqw4jZG',
    gender BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_STUDENT' CHECK (role IN ('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')),
    avatar TEXT NOT NULL DEFAULT 'https://res.cloudinary.com/dqw4mc8dg/image/upload/v1763449679/CareerGuidanceSystem/Avatar/i3pxjl44v4tucczr4roy.png',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student
CREATE TABLE students (
                          id UUID PRIMARY KEY,
                          code CHAR(10) UNIQUE NOT NULL CHECK (code ~ '^[0-9]{10}$'),
    CONSTRAINT fk_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

-- Insert Admin
INSERT INTO users (
    first_name, last_name, email, role
) VALUES (
             'Admin', 'System', 'admin@ou.edu.vn','ROLE_ADMIN'
         ) ON CONFLICT (email) DO NOTHING;