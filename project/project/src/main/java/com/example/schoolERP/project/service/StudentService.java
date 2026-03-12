package com.example.schoolERP.project.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.schoolERP.project.model.Student;
import com.example.schoolERP.project.model.User;
import com.example.schoolERP.project.repository.AttendanceRepository;
import com.example.schoolERP.project.repository.ExamScoreRepository;
import com.example.schoolERP.project.repository.FeeRepository;
import com.example.schoolERP.project.repository.StudentRepository;
import com.example.schoolERP.project.repository.UserRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FeeRepository feeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamScoreRepository examScoreRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          FeeRepository feeRepository,
                          AttendanceRepository attendanceRepository,
                          ExamScoreRepository examScoreRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.feeRepository = feeRepository;
        this.attendanceRepository = attendanceRepository;
        this.examScoreRepository = examScoreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Student addStudent(String name, String email, String phone,
                              Integer classNumber, String section, String rollNumber) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(email));
        user.setRole("ROLE_STUDENT");
        user = userRepository.save(user);

        Student student = new Student();
        student.setName(name);
        student.setUser(user);
        student.setPhone(phone != null ? phone : "");
        student.setClassNumber(classNumber);
        student.setSection(section != null ? section : "A");
        student.setRollNumber(rollNumber != null ? rollNumber : "");
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long studentId, String name, String phone,
                                 Integer classNumber, String section, String rollNumber) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        student.setName(name);
        if (student.getUser() != null) {
            student.getUser().setName(name);
            userRepository.save(student.getUser());
        }
        student.setPhone(phone != null ? phone : "");
        student.setClassNumber(classNumber);
        student.setSection(section != null ? section : "A");
        student.setRollNumber(rollNumber != null ? rollNumber : "");
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        // Must delete child records first to avoid FK constraint violations:
        // 1. ExamScores (references student_id)
        examScoreRepository.findByStudent(student)
                .forEach(examScoreRepository::delete);
        // 2. Attendance (references student_id)
        attendanceRepository.findByStudent(student)
                .forEach(attendanceRepository::delete);
        // 3. Fee (references student_id)
        com.example.schoolERP.project.model.Fee fee = feeRepository.findByStudent(student);
        if (fee != null) feeRepository.delete(fee);

        // 4. Now safe to delete Student
        Long userId = (student.getUser() != null) ? student.getUser().getId() : null;
        studentRepository.delete(student);
        studentRepository.flush();

        // 5. Delete the User account last
        if (userId != null) userRepository.deleteById(userId);
    }

    @Transactional
    public Student getStudentByUser(User user) {
        Student student = studentRepository.findByUser(user);
        if (student == null) {
            student = new Student();
            student.setName(user.getName() != null ? user.getName() : "");
            student.setUser(user);
            student.setPhone("");
            student.setClassNumber(1);
            student.setSection("A");
            student.setRollNumber("");
            student = studentRepository.save(student);
        }
        return student;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByClass(Integer classNumber) {
        return studentRepository.findByClassNumber(classNumber);
    }

    public List<Student> getStudentsByClassAndSection(Integer classNumber, String section) {
        return studentRepository.findByClassNumberAndSection(classNumber, section);
    }

    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
    }

    public long countStudents() {
        return studentRepository.count();
    }
}