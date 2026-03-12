# School ERP — Full Stack Web Application

A School Management System built with Spring Boot 3, Spring Security, JPA/Hibernate, MySQL, and Thymeleaf.
Manages the complete operations of a school with three role-based dashboards.

---

## Roles

| Role | Access |
|---|---|
| **Admin** | Manage students, faculty, courses, timetable, fees, view reports |
| **Faculty** | Mark attendance, enter exam scores, view student fee status |
| **Student** | View own attendance, scores, fees, courses, timetable |

---

## Features

- Role-based login with BCrypt password hashing and Spring Security
- Student & Faculty management with full CRUD operations
- Daily attendance tracking with auto percentage calculation
- Exam score entry with automatic grade calculation (A+, A, B+, B, C, D, F)
- Fee management — set total, record payments, auto-clear on full payment, track defaulters
- Timetable scheduling with duplicate slot prevention
- Admin reports — fee collection summary, attendance overview, top 10 scorers, fee defaulters

---

## Tech Stack

- **Backend** — Java 21, Spring Boot 3.4.5, Rest API
- **Security** — Spring Security 6, BCrypt
- **Database** — MySQL 8, Spring Data JPA, Hibernate
- **Frontend** — Thymeleaf, Bootstrap 5, JS
- **Build** — Maven, Lombok

---

## Author

**Your Name** — [GitHub](https://github.com/YOUR_USERNAME) · [LinkedIn](https://linkedin.com/in/YOUR_PROFILE)
