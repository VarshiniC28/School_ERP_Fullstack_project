package com.example.schoolERP.project.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.schoolERP.project.model.Attendance;
import com.example.schoolERP.project.model.Student;
import com.example.schoolERP.project.repository.AttendanceRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // ── Faculty: Mark or update attendance for one student on a given date ─────
    // Uses save-or-update logic: if a record already exists for that
    // student+date, it updates it instead of throwing a duplicate error.
    @Transactional
    public Attendance markAttendance(Student student, LocalDate date, boolean present) {
        Attendance existing = attendanceRepository.findByStudentAndDate(student, date);

        if (existing != null) {
            // Update existing record
            existing.setPresent(present);
            return attendanceRepository.save(existing);
        }

        // Create new record
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(date);
        attendance.setPresent(present);
        return attendanceRepository.save(attendance);
    }

    // ── Faculty / Student: Get all attendance records for a student ────────────
    public List<Attendance> getAttendanceByStudent(Student student) {
        return attendanceRepository.findByStudent(student);
    }

    // ── Student dashboard: Calculate attendance percentage ────────────────────
    // Returns a value like 85.5 (meaning 85.5%)
    public double calculateAttendancePercentage(Student student) {
        List<Attendance> records = attendanceRepository.findByStudent(student);

        if (records.isEmpty()) {
            return 0.0;
        }

        long presentCount = records.stream()
                .filter(Attendance::isPresent)
                .count();

        return ((double) presentCount / records.size()) * 100;
    }

    // ── Faculty: Get attendance for a specific student on a specific date ──────
    public Attendance getAttendanceByStudentAndDate(Student student, LocalDate date) {
        return attendanceRepository.findByStudentAndDate(student, date);
    }

    // ── Faculty: Count how many days a student was present ────────────────────
    public long countPresentDays(Student student) {
        return attendanceRepository.findByStudent(student).stream()
                .filter(Attendance::isPresent)
                .count();
    }

    // ── Faculty: Count total working days recorded for a student ──────────────
    public long countTotalDays(Student student) {
        return attendanceRepository.findByStudent(student).size();
    }
}