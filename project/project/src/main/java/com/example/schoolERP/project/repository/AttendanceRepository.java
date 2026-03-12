package com.example.schoolERP.project.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.schoolERP.project.model.Attendance;
import com.example.schoolERP.project.model.Faculty;
import com.example.schoolERP.project.model.Student;
import com.example.schoolERP.project.model.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent(Student student);

    Attendance findByStudentAndDate(Student student, LocalDate date);
}
