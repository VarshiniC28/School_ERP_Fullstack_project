package com.example.schoolERP.project.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.schoolERP.project.model.Course;
import com.example.schoolERP.project.model.Faculty;
import com.example.schoolERP.project.model.Fee;
import com.example.schoolERP.project.model.Student;
import com.example.schoolERP.project.service.AttendanceService;
import com.example.schoolERP.project.service.CourseService;
import com.example.schoolERP.project.service.CustomUserDetails;
import com.example.schoolERP.project.service.ExamScoreService;
import com.example.schoolERP.project.service.FacultyService;
import com.example.schoolERP.project.service.FeeService;
import com.example.schoolERP.project.service.StudentService;
import com.example.schoolERP.project.service.TimetableService;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final AttendanceService attendanceService;
    private final ExamScoreService examScoreService;
    private final FeeService feeService;
    private final TimetableService timetableService;

    public FacultyController(FacultyService facultyService,
                             StudentService studentService,
                             CourseService courseService,
                             AttendanceService attendanceService,
                             ExamScoreService examScoreService,
                             FeeService feeService,
                             TimetableService timetableService) {
        this.facultyService = facultyService;
        this.studentService = studentService;
        this.courseService = courseService;
        this.attendanceService = attendanceService;
        this.examScoreService = examScoreService;
        this.feeService = feeService;
        this.timetableService = timetableService;
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String facultyDashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Faculty faculty = facultyService.getFacultyByUser(userDetails.getUser());
        model.addAttribute("faculty", faculty);
        model.addAttribute("myCourses", courseService.getCoursesByFaculty(faculty));
        model.addAttribute("myTimetable", timetableService.getTimetableByFaculty(faculty));
        return "faculty/faculty_dashboard";
    }

    // ── STUDENT MANAGEMENT ────────────────────────────────────────────────────

    @GetMapping("/students")
    public String viewStudents(@RequestParam(required = false) Integer classNumber,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Faculty faculty = facultyService.getFacultyByUser(userDetails.getUser());
        List<Student> students;
        if (classNumber != null) {
            students = studentService.getStudentsByClass(classNumber);
        } else {
            List<Course> myCourses = courseService.getCoursesByFaculty(faculty);
            students = myCourses.stream()
                    .map(c -> studentService.getStudentsByClass(c.getClassNumber()))
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
        }
        model.addAttribute("students", students);
        model.addAttribute("faculty", faculty);
        model.addAttribute("selectedClass", classNumber);
        model.addAttribute("myCourses", courseService.getCoursesByFaculty(faculty));
        return "faculty/manage_students";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam Integer classNumber,
                             @RequestParam String section,
                             @RequestParam String rollNumber,
                             RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.addStudent(name, email, phone, classNumber, section, rollNumber);
            feeService.createFeeRecord(student, 0.0, "2024-25");
            redirectAttributes.addFlashAttribute("success", "Student added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add student: " + e.getMessage());
        }
        return "redirect:/faculty/students";
    }

    @GetMapping("/students/edit/{id}")
    public String showEditStudent(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "faculty/edit_student";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam Integer classNumber,
                                @RequestParam String section,
                                @RequestParam String rollNumber,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(id, name, phone, classNumber, section, rollNumber);
            redirectAttributes.addFlashAttribute("success", "Student updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Update failed: " + e.getMessage());
        }
        return "redirect:/faculty/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/faculty/students";
    }

    // ── ATTENDANCE ────────────────────────────────────────────────────────────

    @GetMapping("/attendance")
    public String viewAttendance(@RequestParam(required = false) Integer classNumber,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Faculty faculty = facultyService.getFacultyByUser(userDetails.getUser());
        model.addAttribute("faculty", faculty);
        model.addAttribute("myCourses", courseService.getCoursesByFaculty(faculty));
        model.addAttribute("today", LocalDate.now());

        if (classNumber != null) {
            List<Student> students = studentService.getStudentsByClass(classNumber);
            model.addAttribute("students", students);
            model.addAttribute("selectedClass", classNumber);

            // BUG 7 FIX: use HashMap (allows null values) instead of Collectors.toMap
            Map<Long, Double> attendanceMap = new HashMap<>();
            for (Student s : students) {
                attendanceMap.put(s.getId(), attendanceService.calculateAttendancePercentage(s));
            }
            model.addAttribute("attendanceMap", attendanceMap);
        }
        return "faculty/manage_attendance";
    }

    @PostMapping("/attendance/mark")
    public String markAttendance(@RequestParam Long studentId,
                                 @RequestParam String date,
                                 @RequestParam boolean present,
                                 @RequestParam Integer classNumber,
                                 RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(studentId);
            attendanceService.markAttendance(student, LocalDate.parse(date), present);
            redirectAttributes.addFlashAttribute("success", "Attendance marked!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark attendance: " + e.getMessage());
        }
        return "redirect:/faculty/attendance?classNumber=" + classNumber;
    }

    // ── EXAM SCORES ───────────────────────────────────────────────────────────

    @GetMapping("/scores")
    public String viewScores(@RequestParam(required = false) Long courseId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        Faculty faculty = facultyService.getFacultyByUser(userDetails.getUser());
        List<Course> myCourses = courseService.getCoursesByFaculty(faculty);
        model.addAttribute("faculty", faculty);
        model.addAttribute("myCourses", myCourses);

        if (courseId != null) {
            Course selectedCourse = courseService.getCourseById(courseId);
            List<Student> students = studentService.getStudentsByClass(selectedCourse.getClassNumber());
            model.addAttribute("selectedCourse", selectedCourse);
            model.addAttribute("students", students);
            model.addAttribute("scores", examScoreService.getScoresByCourse(selectedCourse));
        }
        return "faculty/manage_scores";
    }

    @PostMapping("/scores/save")
    public String saveScore(@RequestParam Long studentId,
                            @RequestParam Long courseId,
                            @RequestParam Double marks,
                            @RequestParam Double maxMarks,
                            RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(studentId);
            Course course = courseService.getCourseById(courseId);
            examScoreService.saveScore(student, course, marks, maxMarks);
            redirectAttributes.addFlashAttribute("success", "Score saved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save score: " + e.getMessage());
        }
        return "redirect:/faculty/scores?courseId=" + courseId;
    }

    // ── FEES ──────────────────────────────────────────────────────────────────

    @GetMapping("/fees")
    public String viewFees(@RequestParam(required = false) Integer classNumber,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        Faculty faculty = facultyService.getFacultyByUser(userDetails.getUser());
        model.addAttribute("faculty", faculty);
        model.addAttribute("myCourses", courseService.getCoursesByFaculty(faculty));

        if (classNumber != null) {
            List<Student> students = studentService.getStudentsByClass(classNumber);
            model.addAttribute("students", students);
            model.addAttribute("selectedClass", classNumber);

            // BUG 6 FIX: use HashMap (allows null values) instead of Collectors.toMap
            Map<Long, Fee> feeMap = new HashMap<>();
            for (Student s : students) {
                feeMap.put(s.getId(), feeService.getFeeByStudent(s)); // null is OK in HashMap
            }
            model.addAttribute("feeMap", feeMap);
        }
        return "faculty/manage_fees";
    }

    @PostMapping("/fees/update/{studentId}")
    public String updateFee(@PathVariable Long studentId,
                            @RequestParam Double amountPaid,
                            @RequestParam Integer classNumber,
                            RedirectAttributes redirectAttributes) {
        try {
            feeService.recordPayment(studentId, amountPaid);
            redirectAttributes.addFlashAttribute("success", "Payment recorded!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update fee: " + e.getMessage());
        }
        return "redirect:/faculty/fees?classNumber=" + classNumber;
    }
}