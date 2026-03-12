package com.example.schoolERP.project.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.schoolERP.project.model.Faculty;
import com.example.schoolERP.project.model.User;
import com.example.schoolERP.project.repository.FacultyRepository;
import com.example.schoolERP.project.repository.UserRepository;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyService(FacultyRepository facultyRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Faculty addFaculty(String name, String email, String phone, String specialization) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(email));
        user.setRole("ROLE_FACULTY");
        user = userRepository.save(user);

        Faculty faculty = new Faculty();
        faculty.setName(name);
        faculty.setUser(user);
        faculty.setPhone(phone != null ? phone : "");
        faculty.setSpecialization(specialization != null ? specialization : "");
        return facultyRepository.save(faculty);
    }

    @Transactional
    public Faculty updateFaculty(Long facultyId, String name, String phone, String specialization) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found: " + facultyId));

        faculty.setName(name);
        if (faculty.getUser() != null) {
            faculty.getUser().setName(name);
            userRepository.save(faculty.getUser());
        }
        faculty.setPhone(phone != null ? phone : "");
        faculty.setSpecialization(specialization != null ? specialization : "");
        return facultyRepository.save(faculty);
    }

    @Transactional
    public void deleteFaculty(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found: " + facultyId));
        // BUG 4 FIX: capture userId BEFORE deleting faculty
        Long userId = (faculty.getUser() != null) ? faculty.getUser().getId() : null;
        facultyRepository.delete(faculty);
        facultyRepository.flush(); // ensure delete is flushed before deleting user
        if (userId != null) {
            userRepository.deleteById(userId);
        }
    }

    @Transactional
    public Faculty getFacultyByUser(User user) {
        Faculty faculty = facultyRepository.findByUser(user);
        if (faculty == null) {
            faculty = new Faculty();
            faculty.setName(user.getName() != null ? user.getName() : "");
            faculty.setUser(user);
            faculty.setPhone("");
            faculty.setSpecialization("");
            faculty = facultyRepository.save(faculty);
        }
        return faculty;
    }

    public List<Faculty> getAllFaculty() {
        return facultyRepository.findAll();
    }

    public Faculty getFacultyById(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found: " + facultyId));
    }

    public long countFaculty() {
        return facultyRepository.count();
    }
}