# Student Management App Development Plan
*Java Swing + PostgreSQL*

## 🔴 Epic 1: Core Foundation for database and authentication/authorization and base classes

### **Feature 1: Database Infrastructure**
**Deliverable:** Working database with all required tables and connections
- [x] Install PostgreSQL and set up database
- [x] Create database schema (users, schools, classes, students, parents, posts, messages, attendance tables)
- [x] Set up JDBC connection
- [x] Test database connectivity and basic CRUD operations

---

### **Feature 2: Authentication System**
**Deliverable:** Secure login system with role-based access
- [x] Implement login screen UI in `LoginWindow.java`  
- [x] Create user authentication logic in `AuthService.java`  
- [x] Implement password validation in `AuthUtil.java`  
- [x] Create user session management in `AuthorizationService.java`  
- [x] Build role-based access control (Principal, Teacher, Parent) in `AuthorizationService.java`  
- [x] Implement logout functionality in `BaseAuthenticatedPage.java`  

---

### **Feature 3: Base Classes**
**Deliverable:** Core base classes for UI and data models  
- [x] Implement `BaseAuthenticatedPage.java` as the base class for authenticated UI pages  
- [x] Implement `User.java` as the data model for users  
- [x] Implement `Student.java` as the data model for students  
- [x] Implement `Parent.java` as the data model for parents  
- [x] Implement `Attendance.java` as the data model for attendance records 
- [x] Create basic data access layer (DAO classes)  
  - [x] Implement `UserDAO.java` for user-related database operations  
  - [x] Implement `StudentDAO.java` for student-related database operations  
  - [x] Implement `AuthorizationDAO.java` for authorization-related database operations  

---

## 🔴 Epic 2: Principal Page

### **Feature 1: User Management Panel**
**Deliverable:** Tabbed interface for creating/editing teachers and parents with user list table, search functionality, and role assignment  
- [ ] Implement `UserManagementPanel.java` for user management UI
- [ ] Add CRUD operations with role validation in `UserService.java`
- [ ] Create `TeacherDAO.java` for teacher-specific data and class assignments
- [ ] Create `ParentDAO.java` for parent-student relationships and contact info

---

### **Feature 2: Class Management Panel**
**Deliverable:** Create and manage school classes with teacher assignment, student enrollment, and class statistics  
- [ ] Implement `ClassManagementPanel.java` for class management UI
- [ ] Add business logic for class operations and capacity management in `ClassService.java`
- [ ] Create `ClassDAO.java` for database operations related to class CRUD and teacher assignments
- [ ] Create `Class.java` as the data model for class entity with capacity and grade level

---

### **Feature 3: Reports Dashboard Panel**
**Deliverable:** Statistical dashboard with charts for enrollment, attendance, and activity reports with export functionality  
- [ ] Implement `ReportsPanel.java` for reports dashboard UI
- [ ] Add report generation and data aggregation logic in `ReportService.java`
- [ ] Create `ReportDAO.java` for complex queries related to attendance rates, enrollment stats, and activity metrics

---

### **Feature 4: School Settings Panel**
**Deliverable:** School configuration including basic info, academic calendar, system preferences, and database management  
- [ ] Implement `SettingsPanel.java` for school settings UI
- [ ] Add logic for managing application configuration and school information in `SettingsService.java`
- [ ] Create a `settings` table to store application configuration and school-specific preferences.
- [ ] Create `SettingsDAO.java` for database operations related to settings storage and retrieval
- [ ] Create `School.java` as the data model for school information and configuration

---

## 🔴 Epic 3: Teacher Page

### **Feature 1: My Classes Overview Panel**
**Deliverable:** Display teacher's assigned classes with quick stats and class selection for context switching  
- [ ] Implement `MyClassesPanel.java` for teacher's class overview UI
- [ ] Add business logic for teacher-specific operations and class access in `TeacherService.java`
- [ ] Update `ClassDAO.java` to include methods for teacher's assigned classes and class statistics

---

### **Feature 2: Class Roster Management Panel**
**Deliverable:** Student list for selected class with profiles, teacher notes, and quick actions  
- [ ] Implement `ClassRosterPanel.java` for class roster management UI
- [ ] Add methods for class-specific student queries and teacher notes in `StudentService.java`
- [ ] Create a `teacher_notes` table to store teacher observations about students.
- [ ] Create `TeacherNote.java` as the data model for teacher observations about students
- [ ] Create `TeacherNoteDAO.java` for database operations related to teacher notes CRUD

---

### **Feature 3: Daily Attendance Panel**
**Deliverable:** Mark daily attendance with date selection, bulk actions, and attendance history  
- [ ] Implement `AttendancePanel.java` for attendance management UI
- [ ] Add business logic for attendance marking and validation in `AttendanceService.java`
- [ ] Create `AttendanceDAO.java` for database operations related to attendance records and history queries
- [ ] Update `Attendance.java` to include late arrival time and excuse reason fields

---

### **Feature 4: Class Posts Management Panel**
**Deliverable:** Create and manage posts for parents with photo uploads, scheduling, and comment moderation  
- [ ] Implement `PostsPanel.java` for class posts management UI
- [ ] Add business logic for post creation, scheduling, and visibility rules in `PostService.java`
- [ ] Create `PostDAO.java` for database operations related to posts, comments, and photo attachments
- [ ] Create `Post.java` as the data model for posts with scheduling and target audience
- [ ] Create a `comments` table to store parent comments on posts.
- [ ] Create `Comment.java` as the data model for parent comments on posts

---

### **Feature 5: Teacher Communication Panel**
**Deliverable:** Message inbox for parent communication with templates, attachments, and conversation threads  
- [ ] Implement `TeacherMessagesPanel.java` for teacher communication UI
- [ ] Add business logic for messaging between teachers and parents in `MessageService.java`
- [ ] Create `MessageDAO.java` for database operations related to messages, threads, and attachments
- [ ] Create `Message.java` as the data model for messages with sender, recipient, and thread info

---

## 🔴 Epic 4: Parent Page

### **Feature 1: My Children Overview Panel**
**Deliverable:** Display enrolled children with quick stats and child selection for context switching  
- [ ] Implement `MyChildrenPanel.java` for parent dashboard UI
- [ ] Add business logic for parent-specific operations and child access in `ParentService.java`
- [ ] Update `ParentDAO.java` to include methods for parent's children and quick statistics

---

### **Feature 2: Class Activities Feed Panel**
**Deliverable:** View teacher posts and school announcements with filtering, comments, and interactions  
- [ ] Implement `ClassActivitiesPanel.java` for class activities feed UI
- [ ] Add methods for parent post viewing and comment functionality in `PostService.java`
- [ ] Create a `post_interactions` table to store likes, shares, and other interactions on posts.
- [ ] Create `PostInteraction.java` as the data model for likes, shares, and parent interactions

---

### **Feature 3: Child Attendance Calendar Panel**
**Deliverable:** Monthly calendar view of child's attendance with statistics and excuse submission  
- [ ] Implement `AttendanceCalendarPanel.java` for attendance calendar UI
- [ ] Add methods for parent attendance viewing and excuse submission in `AttendanceService.java`
- [ ] Create an `attendance_excuses` table to store parent-submitted excuses for absences.
- [ ] Create `AttendanceExcuse.java` as the data model for parent excuse submissions

---

### **Feature 4: Parent Messages Center Panel**
**Deliverable:** Message inbox for teacher communication with compose functionality and attachments  
- [ ] Implement `ParentMessagesPanel.java` for parent messages center UI
- [ ] Add methods for parent message operations and teacher selection in `MessageService.java`

---

### **Feature 5: Child Progress Portfolio Panel**
**Deliverable:** View child's developmental progress, photos, milestones, and teacher observations  
- [ ] Implement `ChildProgressPanel.java` for child progress portfolio UI
- [ ] Add business logic for child development tracking in `ProgressService.java`
- [ ] Create a `progress` table to store child development milestones and teacher observations.
- [ ] Create `Progress.java` as the data model for child development milestones and observations
- [ ] Create `ProgressDAO.java` for database operations related to milestones and development records

---

### **Feature 6: Parent Profile Settings Panel**
**Deliverable:** Edit personal information, emergency contacts, notification preferences, and password  
- [ ] Implement `ParentProfilePanel.java` for parent profile settings UI
- [ ] Add methods for profile updates and notification preferences in `UserService.java`
- [ ] Create a `notification_preferences` table to store parent notification settings.
- [ ] Create `EmergencyContact.java` as the data model for emergency contact