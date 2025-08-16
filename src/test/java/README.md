# Authorization System Tests

## Overview

This directory contains comprehensive unit tests for the Authorization System of the Kindergarten Management Application.

## Test Structure

### `AuthorizationServiceTest.java`
Main test class that validates the authorization system functionality:

#### Test Categories:

1. **Principal (Hiệu trưởng) Permissions Test**
   - Validates that principals have full access to all system functions
   - Tests: User creation, student management, post creation, school management, etc.

2. **Teacher (Giáo viên) Permissions Test**
   - Validates teacher permissions are limited to their assigned classes
   - Tests: Post creation, student updates, attendance management
   - Validates restrictions: Cannot create users, cannot view all posts

3. **Parent (Phụ huynh) Permissions Test**
   - Validates parent permissions are limited to viewing and commenting
   - Tests: Comment/like posts, view class posts, send messages
   - Validates restrictions: Cannot create posts, cannot manage students

4. **Login/Logout Functionality Test**
   - Tests successful and failed login attempts
   - Validates session management

5. **Access Control Without Login Test**
   - Ensures all permissions are denied when not logged in

6. **School and Class Access Control Tests**
   - Validates users can only access their assigned schools/classes

7. **Role-Based Dashboard Access Test**
   - Tests role identification methods (isPrincipal, isTeacher, isParent)

8. **Error Messages Test**
   - Validates appropriate Vietnamese error messages

9. **Invalid Role Handling Test**
   - Tests system robustness with invalid inputs

### `AuthorizationTestRunner.java`
Programmatic test runner that can execute the tests and provide summary results.

## Running Tests

### Option 1: Using Maven (Recommended)
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthorizationServiceTest

# Run tests with verbose output
mvn test -Dtest=AuthorizationServiceTest -Dmaven.test.failure.ignore=true
```

### Option 2: Using VS Code
1. Open the test file `AuthorizationServiceTest.java`
2. Click on "Run Test" or "Debug Test" next to individual test methods
3. Or run the entire test class using the play button next to the class name

### Option 3: Using Test Runner
Run the `AuthorizationTestRunner` class as a Java application:
```bash
java -cp target/test-classes:target/classes:lib/* service.AuthorizationTestRunner
```

## Test Data Requirements

The tests rely on the sample data from `schema.sql`:
- User: `admin` / `admin123` (PRINCIPAL)
- User: `teacher1` / `teacher123` (TEACHER)  
- User: `parent1` / `parent123` (PARENT)

Make sure the database is initialized with sample data before running tests.

## Expected Test Results

All tests should pass if:
1. Database is properly initialized
2. Authorization system is correctly implemented
3. Sample users exist in the database

## Test Output Example

```
=== Testing PRINCIPAL User ===
✅ Logged in as: admin (PRINCIPAL)

Permissions:
  ✅ Tạo tài khoản (CREATE_USERS)
  ✅ Tạo học sinh (CREATE_STUDENTS)
  ✅ Tạo bài đăng (CREATE_POSTS)
  ✅ Bình luận (COMMENT_POSTS)
  ✅ Cập nhật học sinh (UPDATE_STUDENTS)
  ✅ Xem tất cả bài đăng (VIEW_ALL_POSTS)
  ✅ Xem bài đăng lớp (VIEW_CLASS_POSTS)
  ✅ Quản lý trường (MANAGE_SCHOOL)

Class Access:
  Can access Class 1: ✅

School Access:
  Can access School 1: ✅

✅ Logged out
```

## Troubleshooting

### Common Issues:

1. **Database Connection Error**
   - Ensure PostgreSQL is running
   - Check database configuration in `config.properties`
   - Run `DatabaseInitializer` to set up test data

2. **Test Failures**
   - Check that sample users exist in the database
   - Verify authorization system is properly initialized
   - Check for any changes in permission constants

3. **Missing Dependencies**
   - Run `mvn clean install` to download dependencies
   - Ensure JUnit 5 and JUnit Platform Launcher are in classpath

## Adding New Tests

To add new tests:

1. Add new test methods to `AuthorizationServiceTest.java`
2. Use `@Test` annotation
3. Follow naming convention: `test[FeatureName]()`
4. Use descriptive assertions with meaningful error messages
5. Clean up after tests (logout, reset state)

Example:
```java
@Test
@DisplayName("Test New Feature")
void testNewFeature() {
    // Arrange
    authService.login("admin", "admin123");
    
    // Act
    boolean result = authorizationService.hasPermission("NEW_PERMISSION");
    
    // Assert
    assertTrue(result, "Admin should have new permission");
}
```
