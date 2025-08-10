## Project Scope

Focus on core features only:
- User authentication (Principal, Teacher, Parent)
- Basic student information management
- Simple messaging system
- Academic posts/portfolio
- Basic attendance tracking

## Folder Structure

```
src/
├── main/
│   ├── java/
│   │   ├── model/         # Data models (User, Student, etc.)
│   │   ├── dao/           # Database access objects
│   │   ├── service/       # Business logic
│   │   ├── ui/            # Swing UI components
│   │   │   ├── auth/      # Login screens
│   │   │   ├── admin/     # Principal screens
│   │   │   ├── teacher/   # Teacher screens
│   │   │   └── parent/    # Parent screens
│   │   ├── util/          # Utilities (DB connection, etc.)
│   │   └── Main.java      # Application entry point
│   └── resources/
│       └── config.properties
```

### **Key Technologies:**
- **UI:** Java Swing with GroupLayout/BorderLayout
- **Database:** PostgreSQL with JDBC
- **Build:** Maven or Gradle (optional for dependency management)
- **Libraries:** 
  - PostgreSQL JDBC driver
  - BCrypt for password hashing (optional)



