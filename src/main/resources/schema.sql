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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, date)
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
('Nguyễn Văn An', '2020-03-10', 1, '789 Peace Avenue'),
('Trần Thị Bích', '2020-07-22', 1, '123 Harmony Lane'),
('Lê Minh Hoàng', '2020-11-05', 1, '456 Family Street');

-- Insert sample parents
INSERT INTO parents (user_id, name, student_id, relationship, phone, email) VALUES
(3, 'Minh Trần', 1, 'Father', '555-6543', 'minhtran@email.com'),
(4, 'Lan Nguyễn', 2, 'Mother', '555-7890', 'lannguyen@email.com'),
(5, 'Hoa Lê', 3, 'Mother', '555-4321', 'hoale@email.com');

-- Insert sample posts
INSERT INTO posts (title, content, author_id, class_id) VALUES
('Chào mừng đến lớp Hoa Cúc!', 'Chúng tôi rất vui mừng bắt đầu năm học mới với nhiều hoạt động thú vị.', 2, 1);

-- Insert sample attendance
INSERT INTO attendance (student_id, date, status, check_in_time) VALUES
(1, CURRENT_DATE, 'PRESENT', '08:30:00'),
(2, CURRENT_DATE, 'LATE', '08:45:00'),
(3, CURRENT_DATE, 'ABSENT', NULL);

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
