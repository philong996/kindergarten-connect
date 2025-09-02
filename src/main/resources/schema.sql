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
-- Enhanced to support both Class Activities and School Announcements
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author_id INTEGER NOT NULL REFERENCES users(id), -- Teacher who created
    class_id INTEGER REFERENCES classes(id), -- Class this post belongs to (NULL for school announcements)
    post_type VARCHAR(50) NOT NULL DEFAULT 'CLASS_ACTIVITY' CHECK (post_type IN ('CLASS_ACTIVITY', 'SCHOOL_ANNOUNCEMENT')),
    category VARCHAR(50) CHECK (category IN ('GENERAL', 'EVENT', 'HOLIDAY', 'SCHEDULE')), -- For announcements
    photo_attachment BYTEA, -- Binary data for photo uploads
    photo_filename VARCHAR(255), -- Original filename for the photo
    scheduled_date DATE, -- For scheduling posts in advance (NULL for immediate)
    event_date DATE, -- For announcements: when the event will happen
    visibility VARCHAR(20) NOT NULL DEFAULT 'ALL' CHECK (visibility IN ('ALL', 'PARENTS_ONLY', 'TEACHERS_ONLY')),
    is_published BOOLEAN NOT NULL DEFAULT true,
    is_pinned BOOLEAN NOT NULL DEFAULT false, -- For important announcements
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comments Table (Parent comments on teacher posts)
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    post_id INTEGER NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id INTEGER NOT NULL REFERENCES users(id), -- Parent who commented
    content TEXT NOT NULL,
    is_approved BOOLEAN DEFAULT false, -- For comment moderation
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Conversations Table (Chat conversations between users)
CREATE TABLE conversations (
    id SERIAL PRIMARY KEY,
    participant1_id INTEGER NOT NULL REFERENCES users(id),
    participant2_id INTEGER NOT NULL REFERENCES users(id),
    last_message TEXT,
    last_message_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_participants UNIQUE(participant1_id, participant2_id),
    CONSTRAINT different_participants CHECK(participant1_id != participant2_id),
    CONSTRAINT ordered_participants CHECK(participant1_id < participant2_id)
);

-- Chat Messages Table (Individual messages in conversations)
CREATE TABLE chat_messages (
    id SERIAL PRIMARY KEY,
    conversation_id INTEGER NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id INTEGER NOT NULL REFERENCES users(id),
    content TEXT NOT NULL DEFAULT '',
    attachment BYTEA,
    attachment_filename VARCHAR(255),
    attachment_mime_type VARCHAR(100),
    is_read BOOLEAN NOT NULL DEFAULT false,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
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
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_scheduled_date ON posts(scheduled_date);
CREATE INDEX idx_posts_post_type ON posts(post_type);
CREATE INDEX idx_posts_category ON posts(category);
CREATE INDEX idx_posts_event_date ON posts(event_date);
CREATE INDEX idx_posts_is_pinned ON posts(is_pinned);
CREATE INDEX idx_posts_type_pinned ON posts(post_type, is_pinned, created_at); -- Composite index for sorting
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_attendance_student_date ON attendance(student_id, date);
CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_read_status ON messages(receiver_id, is_read);
CREATE INDEX idx_messages_sent_date ON messages(sent_at);
CREATE INDEX idx_messages_conversation ON messages(sender_id, receiver_id, sent_at);
CREATE INDEX idx_physical_records_student_date ON physical_development_records(student_id, measurement_date);

-- Chat system indexes
CREATE INDEX idx_conversations_participant1 ON conversations(participant1_id);
CREATE INDEX idx_conversations_participant2 ON conversations(participant2_id);
CREATE INDEX idx_conversations_last_message ON conversations(last_message_at DESC);
CREATE INDEX idx_chat_messages_conversation ON chat_messages(conversation_id, sent_at);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_unread ON chat_messages(conversation_id, is_read, sender_id);

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

-- Insert sample posts (both class activities and school announcements)
INSERT INTO posts (title, content, author_id, class_id, post_type, visibility, is_published) VALUES
('Chào mừng đến lớp Hoa Cúc!', 'Chúng tôi rất vui mừng bắt đầu năm học mới với nhiều hoạt động thú vị.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Thông báo lịch học tuần tới', 'Tuần tới chúng ta sẽ có các hoạt động vui chơi ngoài trời. Các bé cần mang theo nón và nước uống.', 2, 1, 'CLASS_ACTIVITY', 'PARENTS_ONLY', true),
('Kế hoạch dự án khoa học nhỏ', 'Các bé sẽ tham gia dự án trồng cây và quan sát sự phát triển của cây trong 2 tuần tới.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Hoạt động vẽ tranh chủ đề mùa thu', 'Hôm nay các bé đã vẽ những bức tranh tuyệt đẹp về mùa thu với lá vàng và hoa cúc. Cô rất tự hào về sự sáng tạo của các em!', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Bài học về các loại động vật', 'Tuần này chúng ta đã học về các loài động vật nuôi và động vật hoang dã. Các bé rất hứng thú khi nghe câu chuyện về voi, sư tử và thỏ.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Thực hành kỹ năng sống: Gấp quần áo', 'Các bé đã học cách gấp quần áo gọn gàng và sắp xếp đồ dùng cá nhân. Đây là kỹ năng rất quan trọng cho sự độc lập của trẻ.', 2, 1, 'CLASS_ACTIVITY', 'PARENTS_ONLY', true),
('Tiết học âm nhạc: Hát về gia đình', 'Hôm nay các bé đã học hát bài "Gia đình nhỏ của em" rất hay. Mời các phụ huynh về nhà thực hành cùng con để bé nhớ lâu hơn.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Hoạt động nấu ăn: Làm bánh quy', 'Các bé đã cùng nhau làm bánh quy hình ngôi sao và trái tim. Mọi em đều rất vui và tự hào về thành phẩm của mình!', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Giờ đọc sách: Tìm hiểu về nghề nghiệp', 'Tuần này chúng ta đọc về các nghề nghiệp khác nhau như bác sĩ, giáo viên, lính cứu hỏa. Các bé rất hào hứng chia sẻ ước mơ tương lai của mình.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true),
('Thông báo cần chuẩn bị đồ dùng cho tuần tới', 'Tuần tới lớp sẽ có hoạt động làm đồ handmade. Nhờ phụ huynh chuẩn bị: giấy màu, keo dán, kéo nhỏ (để cô giữ), và vài cây bút màu.', 2, 1, 'CLASS_ACTIVITY', 'PARENTS_ONLY', true);

-- Insert sample school announcements
INSERT INTO posts (title, content, author_id, class_id, post_type, category, event_date, visibility, is_published, is_pinned) VALUES
('Thông báo nghỉ lễ Quốc Khánh', 'Trường sẽ nghỉ lễ Quốc Khánh ngày 2/9/2025. Các lớp học sẽ tiếp tục vào ngày 3/9/2025.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'HOLIDAY', '2025-09-02', 'ALL', true, true),
('Họp phụ huynh đầu năm học', 'Cuộc họp phụ huynh đầu năm học sẽ được tổ chức vào thứ Bảy tuần tới. Mong các phụ huynh sắp xếp thời gian tham dự.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'EVENT', '2025-09-07', 'PARENTS_ONLY', true, true),
('Thay đổi lịch đón trẻ', 'Do sửa chữa cổng chính, việc đón trẻ sẽ chuyển sang cổng phụ từ ngày 5/9 đến hết ngày 10/9.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'SCHEDULE', '2025-09-05', 'ALL', true, false),
('Lễ hội mùa thu 2025', 'Trường sẽ tổ chức lễ hội mùa thu với nhiều hoạt động thú vị như trò chơi dân gian, múa lân, và các gian hàng ẩm thực. Các phụ huynh có thể đăng ký tham gia làm tình nguyện viên.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'EVENT', '2025-09-15', 'ALL', true, true),
('Khám sức khỏe định kỳ cho trẻ', 'Bác sĩ sẽ đến trường khám sức khỏe định kỳ cho tất cả các bé từ ngày 10/9 đến 12/9. Phụ huynh không cần chuẩn bị gì thêm.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'GENERAL', '2025-09-10', 'PARENTS_ONLY', true, false),
('Thông báo nghỉ lễ Trung Thu', 'Trường nghỉ lễ Trung Thu từ ngày 16/9 đến 17/9. Chúc các gia đình một mùa Trung Thu ấm áp và hạnh phúc!', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'HOLIDAY', '2025-09-16', 'ALL', true, true),
('Thay đổi thực đơn tuần tới', 'Do nhà cung cấp thay đổi, thực đơn tuần tới sẽ có một số điều chỉnh. Chi tiết thực đơn mới đã được gửi qua email cho phụ huynh.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'SCHEDULE', '2025-09-09', 'PARENTS_ONLY', true, false),
('Workshop nuôi dạy con cho phụ huynh', 'Chúng tôi sẽ tổ chức workshop "Kỹ năng nuôi dạy con ở độ tuổi mầm non" vào Chủ nhật tuần sau. Đăng ký tại văn phòng nhà trường.', 1, NULL, 'SCHOOL_ANNOUNCEMENT', 'EVENT', '2025-09-14', 'PARENTS_ONLY', true, false);

-- Insert more diverse class activities with different visibility and scheduling
INSERT INTO posts (title, content, author_id, class_id, post_type, visibility, is_published, scheduled_date) VALUES
('Dự án "Tôi yêu gia đình"', 'Tuần tới các bé sẽ tham gia dự án về gia đình. Mỗi bé sẽ mang một bức ảnh gia đình và kể về những thành viên trong nhà. Đây là cơ hội tuyệt vời để các bé học cách chia sẻ và lắng nghe.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true, '2025-09-09'),
('Thông báo riêng: Bé An cần quan tâm đặc biệt', 'Bé An hôm nay có vẻ mệt mỏi và ít vui vẻ hơn bình thường. Phụ huynh bé An vui lòng liên hệ cô để trao đổi thêm về tình trạng của bé ở nhà.', 2, 1, 'CLASS_ACTIVITY', 'PARENTS_ONLY', true, NULL),
('Kế hoạch tham quan vườn bách thảo', 'Lớp đang lên kế hoạch tham quan vườn bách thảo vào cuối tháng. Chuyến đi sẽ giúp các bé tìm hiểu về thiên nhiên và các loài thực vật. Chi tiết sẽ được thông báo sau.', 2, 1, 'CLASS_ACTIVITY', 'ALL', false, '2025-09-25'),
('Bài tập về nhà: Quan sát mây trên bầu trời', 'Cuối tuần này, các bé sẽ có nhiệm vụ quan sát hình dạng của những đám mây và vẽ lại những gì các em nhìn thấy. Phụ huynh hãy hỗ trợ con em hoàn thành bài tập này nhé!', 2, 1, 'CLASS_ACTIVITY', 'PARENTS_ONLY', true, NULL),
('Luyện tập múa hát cho buổi biểu diễn', 'Các bé đang luyện tập tiết mục múa hát "Bé đi học" để chuẩn bị cho buổi biểu diễn cuối tháng. Mọi em đều rất nhiệt tình và tiến bộ rõ rệt!', 2, 1, 'CLASS_ACTIVITY', 'ALL', true, NULL),
('Học làm bánh trung thu mini', 'Nhân dịp Trung Thu sắp đến, các bé sẽ học cách làm bánh trung thu mini từ bột nặn an toàn. Đây sẽ là hoạt động rất thú vị để các em hiểu thêm về truyền thống văn hóa dân tộc.', 2, 1, 'CLASS_ACTIVITY', 'ALL', true, '2025-09-12'),
('Thông báo cho giáo viên: Cập nhật chương trình', 'Chương trình giảng dạy tuần tới sẽ có một số điều chỉnh theo hướng dẫn mới từ Sở Giáo dục. Các đồng nghiệp vui lòng tham khảo tài liệu đính kèm.', 2, 1, 'CLASS_ACTIVITY', 'TEACHERS_ONLY', true, NULL),
('Sinh nhật tháng 9 - Chúc mừng các bé!', 'Hôm nay lớp chúng ta đã tổ chức tiệc sinh nhật cho các bé sinh tháng 9. Tất cả đều rất vui vẻ và hạnh phúc. Chúc các con luôn khỏe mạnh và học giỏi!', 2, 1, 'CLASS_ACTIVITY', 'ALL', true, NULL);

-- Insert sample comments  
INSERT INTO comments (post_id, author_id, content, is_approved) VALUES
(1, 3, 'Cảm ơn cô giáo! Chúng tôi rất mong chờ năm học mới.', true),
(1, 4, 'Rất vui được tham gia lớp học của cô.', true),
(2, 3, 'Em sẽ chuẩn bị đầy đủ đồ dùng cho bé.', false),
(3, 5, 'Dự án này nghe rất thú vị, con em sẽ rất thích.', true);

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

-- View for class activities
CREATE VIEW class_activities AS
SELECT 
    p.id,
    p.title,
    p.content,
    p.author_id,
    u.username AS author_name,
    p.class_id,
    c.name AS class_name,
    p.photo_attachment,
    p.photo_filename,
    p.scheduled_date,
    p.visibility,
    p.is_published,
    p.created_at,
    p.updated_at,
    COUNT(cm.id) AS comment_count
FROM posts p
JOIN users u ON p.author_id = u.id
LEFT JOIN classes c ON p.class_id = c.id
LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
WHERE p.post_type = 'CLASS_ACTIVITY'
GROUP BY p.id, u.username, c.name
ORDER BY p.created_at DESC;

-- View for school announcements
CREATE VIEW school_announcements AS
SELECT 
    p.id,
    p.title,
    p.content,
    p.author_id,
    u.username AS author_name,
    p.category,
    p.event_date,
    p.photo_attachment,
    p.photo_filename,
    p.scheduled_date,
    p.visibility,
    p.is_published,
    p.is_pinned,
    p.created_at,
    p.updated_at,
    COUNT(cm.id) AS comment_count
FROM posts p
JOIN users u ON p.author_id = u.id
LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
WHERE p.post_type = 'SCHOOL_ANNOUNCEMENT'
GROUP BY p.id, u.username
ORDER BY p.is_pinned DESC, p.created_at DESC;

-- View for all posts with type information
CREATE VIEW posts_with_details AS
SELECT 
    p.id,
    p.title,
    p.content,
    p.author_id,
    u.username AS author_name,
    p.class_id,
    c.name AS class_name,
    p.post_type,
    p.category,
    p.event_date,
    p.photo_attachment,
    p.photo_filename,
    p.scheduled_date,
    p.visibility,
    p.is_published,
    p.is_pinned,
    p.created_at,
    p.updated_at,
    COUNT(cm.id) AS comment_count,
    -- Helper columns for display
    CASE 
        WHEN p.post_type = 'SCHOOL_ANNOUNCEMENT' THEN 'School Announcement'
        ELSE 'Class Activity'
    END AS post_type_display,
    CASE 
        WHEN p.category = 'EVENT' THEN 'Event'
        WHEN p.category = 'HOLIDAY' THEN 'Holiday'
        WHEN p.category = 'SCHEDULE' THEN 'Schedule Change'
        WHEN p.category = 'GENERAL' THEN 'General'
        ELSE ''
    END AS category_display,
    CASE 
        WHEN p.event_date IS NOT NULL AND p.event_date > CURRENT_DATE THEN true
        ELSE false
    END AS is_upcoming_event
FROM posts p
JOIN users u ON p.author_id = u.id
LEFT JOIN classes c ON p.class_id = c.id
LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
GROUP BY p.id, u.username, c.name
ORDER BY 
    CASE WHEN p.post_type = 'SCHOOL_ANNOUNCEMENT' THEN p.is_pinned ELSE false END DESC,
    p.created_at DESC;

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
COMMENT ON TABLE posts IS 'Enhanced posts supporting both Class Activities and School Announcements with photo support, scheduling, categorization, and pinning';
COMMENT ON TABLE comments IS 'Parent comments on teacher posts with moderation support';
COMMENT ON TABLE messages IS 'Simple messaging between teachers and parents';
COMMENT ON TABLE attendance IS 'Daily attendance tracking';

-- Column comments for the enhanced posts table
COMMENT ON COLUMN posts.post_type IS 'Type of post: CLASS_ACTIVITY for class-specific posts, SCHOOL_ANNOUNCEMENT for school-wide announcements';
COMMENT ON COLUMN posts.category IS 'Category for school announcements: GENERAL, EVENT, HOLIDAY, SCHEDULE';
COMMENT ON COLUMN posts.event_date IS 'Date when an announced event will take place (for announcements only)';
COMMENT ON COLUMN posts.is_pinned IS 'Whether this post should be pinned to the top (typically for important announcements)';
COMMENT ON COLUMN posts.class_id IS 'Class this post belongs to (NULL for school-wide announcements)';

