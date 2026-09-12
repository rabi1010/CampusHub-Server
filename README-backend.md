# CampusHub Backend

Spring Boot REST API for the CampusHub college management system.

## Current Stack

- Java 17
- Spring Boot 3.5.16
- Spring Security 6.5.11 with JWT authentication
- Spring Data JPA and Hibernate
- PostgreSQL
- Spring Mail with Gmail SMTP
- Cloudinary image storage using Cloudinary HTTP client
- Maven Wrapper

## Run Locally

1. Create a PostgreSQL database named `campushub`.
2. Set the environment variables listed below.
3. Start the application with `./mvnw spring-boot:run` or `mvnw.cmd spring-boot:run` on Windows.
4. The API is available at `http://localhost:8080`.

Required environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/campushub
DB_USER=postgres
DB_PASSWORD=your-password
JWT_SECRET=your-long-random-secret
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

Optional mail variables should be supplied through a deployment-specific configuration rather than committed source files:

```text
SPRING_MAIL_USERNAME=your-email
SPRING_MAIL_PASSWORD=your-app-password
```

The current development configuration uses port `8080`, frontend origin `http://localhost:5173`, JWT access expiration of 24 hours, and multipart request limits of 10 MB. Image services additionally validate image type and size. Refresh-token generation and refresh expiration are not currently implemented.

## Response Format

Successful JSON responses use:

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

Paginated endpoints return Spring Page data in `data.content`, together with pagination metadata such as `totalElements`, `totalPages`, `size`, and `number`.

Unless marked **Public**, endpoints require a valid `Authorization: Bearer <jwt>` header. Access is enforced with method-level `@PreAuthorize` rules.

## Complete API Reference

There are **57 implemented controller endpoints**.

### Authentication: `/api/auth`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a pending student, teacher, or parent account. |
| POST | `/api/auth/login` | Public | Authenticate and return a JWT and user details. |
| POST | `/api/auth/logout` | Public | Complete client-side logout. JWT invalidation is handled by removing the token client-side. |
| GET | `/api/auth/me` | Authenticated | Return the current user. |
| PUT | `/api/auth/me` | Authenticated | Update the current user's profile. |
| PATCH | `/api/auth/me/password` | Authenticated | Change the current user's password. |
| PUT | `/api/auth/me/image` | Authenticated | Upload the current user's profile image. Multipart field: `image`. |
| GET | `/api/auth/me/image` | Authenticated | Return the current user's profile image URL. |
| GET | `/api/auth/users/pending` | ADMIN | List accounts waiting for approval. |
| PATCH | `/api/auth/users/{id}/approve` | ADMIN | Approve an account and create its role-specific record. |

Registration request fields: `fullName`, `email`, `phone`, `password`, and `role` (`STUDENT`, `TEACHER`, or `PARENT`). Approval fields depend on the selected role: students use `rollNo`, `departmentId`, and `batchId`; teachers use `employeeId`, `departmentId`, and `qualification`; parents use `studentIds` and `relationship`.

### Students: `/api/students`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/students?page=1&size=10&search=&departmentId=` | ADMIN, TEACHER | List students with pagination and optional filters. |
| GET | `/api/students/{id}` | ADMIN, TEACHER, STUDENT | Get one student. |
| GET | `/api/students/me` | STUDENT | Get the student record linked to the logged-in user. |
| POST | `/api/students` | ADMIN | Create a student and linked user. |
| PUT | `/api/students/{id}` | ADMIN | Update a student. |
| DELETE | `/api/students/{id}` | ADMIN | Delete a student and its linked user data. |
| POST | `/api/students/{id}/image` | ADMIN, STUDENT | Upload an image. Multipart field: `image`. |
| GET | `/api/students/{id}/image` | ADMIN, TEACHER, STUDENT | Return a student's image URL. |

### Teachers: `/api/teachers`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/teachers?page=1&size=10&search=&departmentId=` | ADMIN, TEACHER | List teachers with pagination and optional filters. |
| GET | `/api/teachers/me` | TEACHER | Get the logged-in teacher's profile. |
| GET | `/api/teachers/{id}` | ADMIN, TEACHER | Get one teacher. |
| POST | `/api/teachers` | ADMIN | Create a teacher and linked user. |
| PUT | `/api/teachers/{id}` | ADMIN | Update a teacher. |
| DELETE | `/api/teachers/{id}` | ADMIN | Delete a teacher and its linked user data. |
| POST | `/api/teachers/{id}/image` | ADMIN, TEACHER | Upload an image. Multipart field: `image`. |
| GET | `/api/teachers/{id}/image` | ADMIN, TEACHER | Return a teacher's image URL. |

### Parents: `/api/parents`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/parents?page=1&size=10&search=` | ADMIN | List parents with pagination and optional search. |
| GET | `/api/parents/{id}` | ADMIN, PARENT | Get one parent and linked children. |
| GET | `/api/parents/me` | PARENT | Get the logged-in parent's profile. |
| POST | `/api/parents` | ADMIN | Create a parent and link students. |
| PUT | `/api/parents/{id}` | ADMIN | Update a parent and student links. |
| DELETE | `/api/parents/{id}` | ADMIN | Delete a parent and its linked user data. |
| POST | `/api/parents/{id}/image` | ADMIN, PARENT | Upload an image. Multipart field: `image`. |
| GET | `/api/parents/{id}/image` | ADMIN, PARENT | Return a parent's image URL. |

### Courses: `/api/courses`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/courses?page=1&size=10&search=&departmentId=&semester=0` | Authenticated | List courses with optional filters. |
| GET | `/api/courses/{id}` | Authenticated | Get one course. |
| POST | `/api/courses` | ADMIN | Create a course. |
| PUT | `/api/courses/{id}` | ADMIN | Update a course. |
| DELETE | `/api/courses/{id}` | ADMIN | Delete a course. |

### Notices: `/api/notices`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/notices?page=1&size=10&search=` | Authenticated | List notices filtered for the logged-in user's role. |
| GET | `/api/notices/{id}` | Authenticated | Get one notice. |
| POST | `/api/notices` | ADMIN, TEACHER | Create a notice. `forRole` must be `ALL`, `STUDENT`, `TEACHER`, or `PARENT`. |
| DELETE | `/api/notices/{id}` | ADMIN, TEACHER | Delete a notice. Teachers may delete only their own notices. |

There is currently no `PUT` or `PATCH /api/notices/{id}` endpoint.

### Attendance: `/api/attendance`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/attendance` | ADMIN, TEACHER | Mark attendance for multiple students. |
| GET | `/api/attendance/course/{courseId}?page=1&size=20` | ADMIN, TEACHER | Get paginated attendance for a course. |
| GET | `/api/attendance/student/{studentId}?page=1&size=20` | ADMIN, TEACHER, STUDENT, PARENT | Get paginated attendance for a student. |
| GET | `/api/attendance/summary` | STUDENT | Get the logged-in student's attendance summary. |
| GET | `/api/attendance/student/{studentId}/summary` | ADMIN, TEACHER, STUDENT, PARENT | Get a student's attendance summary. |

Attendance statuses are `PRESENT`, `ABSENT`, and `LATE`.

### Marks: `/api/marks`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/marks` | ADMIN, TEACHER | Upload marks for multiple students. |
| GET | `/api/marks/student/{studentId}` | ADMIN, TEACHER, STUDENT, PARENT | Get all marks for a student. |
| GET | `/api/marks/gpa` | STUDENT | Get the logged-in student's GPA. |
| GET | `/api/marks/course/{courseId}` | ADMIN, TEACHER | Get marks for a course. |
| GET | `/api/marks/student/{studentId}/course/{courseId}` | ADMIN, TEACHER, STUDENT, PARENT | Get marks for one student in one course. |
| GET | `/api/marks/student/{studentId}/gpa` | ADMIN, TEACHER, STUDENT, PARENT | Get a student's GPA. |

Exam types are `INTERNAL`, `MIDTERM`, and `FINAL`.

### Academic Data: `/api`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/departments` | Authenticated | List departments. |
| GET | `/api/batches` | Authenticated | List batches. |

### Statistics: `/api/stats`

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/stats/admin` | ADMIN | Return admin dashboard statistics. |

## Security Notes

- CORS currently allows `http://localhost:5173` with credentials.
- CSRF is disabled because the API is stateless and uses JWT.
- Passwords use BCrypt with strength 12.
- Do not commit database passwords, JWT secrets, SMTP passwords, or Cloudinary secrets.
- `POST /api/auth/logout` does not revoke a server-side JWT; clients must remove their stored token.

## Verification

```text
mvnw.cmd clean test
```

The project currently includes the application context test. Dependency management is based on Spring Boot `3.5.16`, JJWT `0.13.0`, Apache HttpClient `4.5.14`, and Commons Lang `3.18.0`.
