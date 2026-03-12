package com.example.schoolERP.project.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.schoolERP.project.model.Fee;
import com.example.schoolERP.project.model.Student;
import com.example.schoolERP.project.repository.FeeRepository;
import com.example.schoolERP.project.repository.StudentRepository;

@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeeService(FeeRepository feeRepository, StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Fee createFeeRecord(Student student, Double totalAmount, String academicYear) {
        // Don't create a duplicate if one already exists
        Fee existing = feeRepository.findByStudent(student);
        if (existing != null) return existing;

        Fee fee = new Fee();
        fee.setStudent(student);
        fee.setTotalAmount(totalAmount != null ? totalAmount : 0.0);
        fee.setPaidAmount(0.0);
        fee.setCleared(false);
        fee.setAcademicYear(academicYear != null ? academicYear : "2024-25");
        return feeRepository.save(fee);
    }

    @Transactional
    public Fee recordPayment(Long studentId, Double amountPaid) {
        Fee fee = feeRepository.findByStudentId(studentId);

        // BUG 5 FIX: auto-create fee record if it doesn't exist, don't throw
        if (fee == null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
            fee = new Fee();
            fee.setStudent(student);
            fee.setTotalAmount(0.0);
            fee.setPaidAmount(0.0);
            fee.setCleared(false);
            fee.setAcademicYear("2024-25");
            fee = feeRepository.save(fee);
        }

        double newPaidAmount = fee.getPaidAmount() + amountPaid;
        fee.setPaidAmount(newPaidAmount);
        fee.setLastPaymentDate(LocalDate.now());
        if (fee.getTotalAmount() > 0 && newPaidAmount >= fee.getTotalAmount()) {
            fee.setCleared(true);
        }
        return feeRepository.save(fee);
    }

    @Transactional
    public Fee setFeeCleared(Long studentId, boolean cleared) {
        Fee fee = feeRepository.findByStudentId(studentId);
        if (fee == null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
            fee = new Fee();
            fee.setStudent(student);
            fee.setTotalAmount(0.0);
            fee.setPaidAmount(0.0);
            fee.setAcademicYear("2024-25");
            fee = feeRepository.save(fee);
        }
        fee.setCleared(cleared);
        if (cleared) {
            fee.setPaidAmount(fee.getTotalAmount());
            fee.setLastPaymentDate(LocalDate.now());
        }
        return feeRepository.save(fee);
    }

    public Fee getFeeByStudent(Student student) {
        return feeRepository.findByStudent(student);
    }

    public Fee getFeeByStudentId(Long studentId) {
        return feeRepository.findByStudentId(studentId);
    }

    public List<Fee> getAllFees() {
        return feeRepository.findAll();
    }

    public double getRemainingBalance(Student student) {
        Fee fee = feeRepository.findByStudent(student);
        if (fee == null) return 0.0;
        return Math.max(0.0, fee.getTotalAmount() - fee.getPaidAmount());
    }
}