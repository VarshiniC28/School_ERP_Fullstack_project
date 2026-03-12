package com.example.schoolERP.project.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.schoolERP.project.model.Student;
import com.example.schoolERP.project.service.AttendanceService;
import com.example.schoolERP.project.service.CourseService;
import com.example.schoolERP.project.service.ExamScoreService;
import com.example.schoolERP.project.service.FeeService;
import com.example.schoolERP.project.service.StudentService;
import com.example.schoolERP.project.service.TimetableService;
import com.example.schoolERP.project.service.CustomUserDetails;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final AttendanceService attendanceService;
    private final ExamScoreService examScoreService;
    private final FeeService feeService;
    private final TimetableService timetableService;

    public StudentController(StudentService studentService,
                             CourseService courseService,
                             AttendanceService attendanceService,
                             ExamScoreService examScoreService,
                             FeeService feeService,
                             TimetableService timetableService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.attendanceService = attendanceService;
        this.examScoreService = examScoreService;
        this.feeService = feeService;
        this.timetableService = timetableService;
    }

    // Helper: get logged-in student's profile
    private Student getLoggedInStudent(CustomUserDetails userDetails) {
        return studentService.getStudentByUser(userDetails.getUser());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD — summary of everything
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String studentDashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Student student = getLoggedInStudent(userDetails);

        model.addAttribute("student", student);
        model.addAttribute("attendancePercentage",
                attendanceService.calculateAttendancePercentage(student));
        model.addAttribute("presentDays",
                attendanceService.countPresentDays(student));
        model.addAttribute("totalDays",
                attendanceService.countTotalDays(student));
        model.addAttribute("scores",
                examScoreService.getScoresByStudent(student));
        model.addAttribute("fee",
                feeService.getFeeByStudent(student));
        model.addAttribute("courses",
                courseService.getCoursesByClass(student.getClassNumber()));
        return "student/student_dashboard";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COURSES — courses for this student's class with faculty info
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/courses")
    public String viewCourses(@AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        Student student = getLoggedInStudent(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("courses",
                courseService.getCoursesByClass(student.getClassNumber()));
        return "student/view_courses";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ATTENDANCE — student's own attendance records + percentage
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/attendance")
    public String viewAttendance(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Student student = getLoggedInStudent(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("attendanceList",
                attendanceService.getAttendanceByStudent(student));
        model.addAttribute("attendancePercentage",
                attendanceService.calculateAttendancePercentage(student));
        model.addAttribute("presentDays",
                attendanceService.countPresentDays(student));
        model.addAttribute("totalDays",
                attendanceService.countTotalDays(student));
        return "student/view_attendance";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXAM SCORES — student's own grades
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/scores")
    public String viewScores(@AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        Student student = getLoggedInStudent(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("scores",
                examScoreService.getScoresByStudent(student));
        return "student/view_scores";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FEES — student's own fee clearance status
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/fees")
    public String viewFees(@AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        Student student = getLoggedInStudent(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("fee", feeService.getFeeByStudent(student));
        model.addAttribute("remainingBalance",
                feeService.getRemainingBalance(student));
        return "student/view_fees";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIMETABLE — class timetable for this student's class
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/timetable")
    public String viewTimetable(@AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        Student student = getLoggedInStudent(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("timetable",
                timetableService.getTimetableByClass(student.getClassNumber()));
        return "student/view_timetable";
    }
}