-- =====================================================
-- SIMPLE KINDERGARTEN MANAGEMENT SYSTEM - CORE FEATURES ONLY
-- Based on 2-week development plan requirements
-- =====================================================

-- Drop tables if they exist (for clean reinstall)
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS parents CASCADE;
DROP TABLE IF EXISTS classes CASCADE;
DROP TABLE IF EXISTS schools CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================
-- CORE TABLES - WEEK 1 FOUNDATION
-- =====================================================

-- Schools Table
CREATE TABLE schools (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users Table (Principal, Teacher, Parent)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PRINCIPAL', 'TEACHER', 'PARENT')),
    school_id INTEGER REFERENCES schools(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Classes Table
CREATE TABLE classes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    school_id INTEGER NOT NULL REFERENCES schools(id),
    teacher_id INTEGER REFERENCES users(id),
    grade_level VARCHAR(20) NOT NULL DEFAULT 'Lớp Lá (5-6 tuổi)', -- e.g., 'Lớp Mầm (3-4 tuổi)', 'Lớp Chồi (4-5 tuổi)', 'Lớp Lá (5-6 tuổi)'
    capacity INTEGER NOT NULL DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- STUDENT MANAGEMENT - WEEK 2 CORE FEATURES
-- =====================================================

-- Students Table (Simple student information)
CREATE TABLE students (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    class_id INTEGER REFERENCES classes(id),
    address TEXT,
    profile_image BYTEA, -- Binary data for profile image
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Parents Table (Basic parent information)
CREATE TABLE parents (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    student_id INTEGER REFERENCES students(id),
    relationship VARCHAR(30) NOT NULL, -- 'Mother', 'Father', 'Guardian'
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- COMMUNICATION FEATURES - WEEK 2
-- =====================================================

-- Posts Table (Academic posts/portfolio - Teachers create, Parents view)
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author_id INTEGER NOT NULL REFERENCES users(id), -- Teacher who created
    class_id INTEGER REFERENCES classes(id), -- Class this post belongs to
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages Table (Simple messaging between teacher-parent)
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    sender_id INTEGER NOT NULL REFERENCES users(id),
    receiver_id INTEGER NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- ATTENDANCE TRACKING - WEEK 2
-- =====================================================

-- Attendance Table (Basic attendance marking)
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id),
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
    check_in_time TIME,
    late_arrival_time TIME, -- Time when late student actually arrived
    excuse_reason TEXT, -- Reason for absence or lateness
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, date)
);

-- =====================================================
-- PHYSICAL DEVELOPMENT TRACKING
-- =====================================================

-- Physical Development Records (Height, Weight tracking)
CREATE TABLE physical_development_records (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id),
    height_cm DECIMAL(5,2) NOT NULL, -- Height in centimeters (e.g., 105.50)
    weight_kg DECIMAL(5,2) NOT NULL, -- Weight in kilograms (e.g., 18.75)
    bmi DECIMAL(5,2) GENERATED ALWAYS AS (weight_kg / POWER(height_cm / 100, 2)) STORED,
    measurement_date DATE NOT NULL,
    recorded_by INTEGER NOT NULL REFERENCES users(id), -- Teacher who recorded
    notes TEXT, -- Optional notes from teacher
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- BASIC INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_students_class_id ON students(class_id);
CREATE INDEX idx_parents_student_id ON parents(student_id);
CREATE INDEX idx_posts_class_id ON posts(class_id);
CREATE INDEX idx_attendance_student_date ON attendance(student_id, date);
CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_physical_records_student_date ON physical_development_records(student_id, measurement_date);

-- =====================================================
-- SAMPLE DATA FOR TESTING
-- =====================================================

-- Insert default school
INSERT INTO schools (name, address, phone) 
VALUES 
('Hoa Mai Kindergarten', '456 Flower Street', '555-5678');

-- Insert users (Principal, Teacher, Parents)
INSERT INTO users (username, password, role, school_id) VALUES
('hieu_admin', 'hieu123', 'PRINCIPAL', 1),
('ngoc_teacher', 'ngoc123', 'TEACHER', 1),
('minh_parent', 'minh123', 'PARENT', 1),
('lan_parent', 'lan123', 'PARENT', 1),
('hoa_parent', 'hoa123', 'PARENT', 1);

-- Insert sample class
INSERT INTO classes (name, school_id, teacher_id) VALUES
('Lớp Hoa Cúc', 1, 2);

-- Insert sample students
INSERT INTO students (name, dob, class_id, address) VALUES
('Trần Văn An', '2020-03-10', 1, '789 Peace Avenue'),
('Trần Thị Bích', '2020-07-22', 1, '123 Harmony Lane'),
('Lê Minh Hoàng', '2020-11-05', 1, '456 Family Street'),
('Trần Minh Hoàng', '2020-11-08', 1, '456 Family Street');

-- Insert sample parents
INSERT INTO parents (user_id, name, student_id, relationship, phone, email) VALUES
(3, 'Minh Trần', 1, 'Father', '555-6543', 'minhtran@email.com'),
(4, 'Lan Nguyễn', 2, 'Mother', '555-7890', 'lannguyen@email.com'),
(5, 'Hoa Lê', 3, 'Mother', '555-4321', 'hoale@email.com'),
(3, 'Minh Trần', 4, 'Father', '555-6543', 'minhtran@email.com');

-- Insert sample posts
INSERT INTO posts (title, content, author_id, class_id) VALUES
('Chào mừng đến lớp Hoa Cúc!', 'Chúng tôi rất vui mừng bắt đầu năm học mới với nhiều hoạt động thú vị.', 2, 1);

-- Insert sample attendance
INSERT INTO attendance (student_id, date, status, check_in_time) VALUES
(1, CURRENT_DATE, 'PRESENT', '08:30:00'),
(2, CURRENT_DATE, 'LATE', '08:45:00'),
(3, CURRENT_DATE, 'ABSENT', NULL);

-- Insert sample physical development records
INSERT INTO physical_development_records (student_id, height_cm, weight_kg, measurement_date, recorded_by, notes) VALUES
(1, 105.50, 18.75, '2024-01-15', 2, 'Regular growth check'),
(1, 107.20, 19.30, '2024-04-15', 2, 'Good growth progress'),
(1, 109.00, 19.80, '2024-08-15', 2, 'Healthy development'),
(2, 103.80, 17.90, '2024-01-15', 2, 'Initial measurement'),
(2, 105.40, 18.45, '2024-04-15', 2, 'Normal growth'),
(2, 107.10, 18.95, '2024-08-15', 2, 'Steady progress'),
(3, 108.20, 20.15, '2024-01-15', 2, 'Above average height'),
(3, 110.50, 20.80, '2024-04-15', 2, 'Continued good growth'),
(3, 112.30, 21.40, '2024-08-15', 2, 'Excellent development');

-- =====================================================
-- SIMPLE VIEWS FOR COMMON QUERIES
-- =====================================================

-- View for student information with class and parent
CREATE VIEW student_summary AS
SELECT 
    s.id,
    s.name AS student_name,
    s.dob,
    c.name AS class_name,
    p.name AS parent_name,
    p.phone AS parent_phone,
    p.relationship
FROM students s
LEFT JOIN classes c ON s.class_id = c.id
LEFT JOIN parents p ON s.id = p.student_id;

-- View for attendance summary
CREATE VIEW attendance_summary AS
SELECT 
    DATE(a.date) as attendance_date,
    s.name AS student_name,
    c.name AS class_name,
    a.status,
    a.check_in_time
FROM attendance a
JOIN students s ON a.student_id = s.id
JOIN classes c ON s.class_id = c.id
ORDER BY a.date DESC;

-- View for physical development summary
CREATE VIEW physical_development_summary AS
SELECT 
    pdr.id,
    s.id AS student_id,
    s.name AS student_name,
    s.dob,
    pdr.height_cm,
    pdr.weight_kg,
    pdr.bmi,
    pdr.measurement_date,
    pdr.recorded_by,
    u.username AS recorded_by_teacher,
    pdr.notes,
    pdr.created_at,
    -- Calculate age at measurement
    EXTRACT(YEAR FROM AGE(pdr.measurement_date, s.dob)) AS age_years,
    EXTRACT(MONTH FROM AGE(pdr.measurement_date, s.dob)) % 12 AS age_months,
    -- Previous measurement for comparison
    LAG(pdr.height_cm) OVER (PARTITION BY s.id ORDER BY pdr.measurement_date) AS prev_height,
    LAG(pdr.weight_kg) OVER (PARTITION BY s.id ORDER BY pdr.measurement_date) AS prev_weight,
    LAG(pdr.bmi) OVER (PARTITION BY s.id ORDER BY pdr.measurement_date) AS prev_bmi
FROM physical_development_records pdr
JOIN students s ON pdr.student_id = s.id
JOIN users u ON pdr.recorded_by = u.id
ORDER BY s.id, pdr.measurement_date DESC;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE users IS 'System users: Principal, Teachers, and Parents';
COMMENT ON TABLE students IS 'Basic student information';
COMMENT ON TABLE parents IS 'Parent contact information linked to students';
COMMENT ON TABLE classes IS 'Kindergarten classes with assigned teachers';
COMMENT ON TABLE posts IS 'Academic posts and announcements by teachers';
COMMENT ON TABLE messages IS 'Simple messaging between teachers and parents';
COMMENT ON TABLE attendance IS 'Daily attendance tracking';
