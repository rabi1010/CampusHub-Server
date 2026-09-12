# CampusHub Postman Testing

Import these two files into Postman:

- `CampusHub.postman_collection.json`
- `CampusHub.postman_environment.json`

Select the `CampusHub Local` environment before sending requests.

## Before Starting

1. Start PostgreSQL and the Spring Boot backend.
2. Confirm the backend is available at `http://localhost:8080`.
3. Confirm `.env` contains valid `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, Cloudinary variables, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `FRONTEND_URL`.
4. Set `imagePath` to an existing local `.jpg`, `.jpeg`, `.png`, or `.webp` file. Postman cannot resolve a file variable unless the file is selected or the working directory is configured.

## Required Test Order

The collection is grouped in a safe order, but IDs must be copied into environment variables.

1. Run **List departments** and copy a department object's `id` to `departmentId`.
2. Run **List batches** and copy a batch object's `id` to `batchId`.
3. Run **Login admin**. The collection test script automatically stores `data.token` as `token`.
4. Register one or more users, then run **Get pending users**.
5. Copy the pending user's `id` to `pendingUserId`, then approve it with the role-specific body. The student approval body is prefilled; teacher approval requires `employeeId`, `departmentId`, and `qualification`; parent approval requires `studentIds` and `relationship`.
6. Run **List students**, **List teachers**, and **List parents**. Copy returned entity IDs into `studentId`, `teacherId`, and `parentId`.
7. Run **Create course**, then copy its returned `data.id` into `courseId`.
8. Run attendance and marks requests after `studentId` and `courseId` are populated.
9. For role-specific endpoints, log in using the appropriate approved user's email and password, then replace `token` with that login response token. For example, `/students/me` requires a STUDENT token and `/attendance/summary` requires a STUDENT token.
10. Run delete requests last because they remove test records used by other requests.

## Request Rules

- JSON requests use `Content-Type: application/json`.
- Authenticated requests use `Authorization: Bearer {{token}}`.
- Image requests use `multipart/form-data` with exactly one field named `image`.
- Attendance statuses: `PRESENT`, `ABSENT`, `LATE`.
- Mark exam types: `INTERNAL`, `MIDTERM`, `FINAL`.
- Notice roles: `ALL`, `STUDENT`, `TEACHER`, `PARENT`.
- Page numbers in this backend are one-based. Use `page=1`.
- Successful API data is inside `data`; paginated records are inside `data.content`.

## Complete Coverage

The collection contains all 57 implemented endpoints: 10 authentication, 2 academic data, 8 student, 8 teacher, 8 parent, 5 course, 4 notice, 5 attendance, 6 marks, and 1 statistics endpoint.

There is no backend `PUT` or `PATCH /api/notices/{id}` endpoint. The frontend contact service's `/api/contact` call is also not included because no such backend controller exists.

## Expected Common Errors

- `401`: missing, invalid, or expired Bearer token.
- `403`: valid token but insufficient role.
- `404`: supplied entity ID does not exist.
- `409`: duplicate email, roll number, employee ID, or course code.
- `400`: invalid enum, missing required field, invalid image, or invalid pagination/body data.
