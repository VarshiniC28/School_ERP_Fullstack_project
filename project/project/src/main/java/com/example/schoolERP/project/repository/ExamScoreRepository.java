package com.example.schoolERP.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.schoolERP.project.model.Course;
import com.example.schoolERP.project.model.ExamScore;
import com.example.schoolERP.project.model.Student;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {

    List<ExamScore> findByStudent(Student student);

    List<ExamScore> findByCourse(Course course);

    List<ExamScore> findByStudentAndCourse(Student student, Course course);
}