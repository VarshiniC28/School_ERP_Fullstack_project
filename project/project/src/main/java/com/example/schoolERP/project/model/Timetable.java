package com.example.schoolERP.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "timetable",
    uniqueConstraints = {
        // A faculty can only teach one course per day per time slot per class
        @UniqueConstraint(columnNames = {"faculty_id", "class_number", "day_of_week", "time_slot"})
    }
)
@Data
@NoArgsConstructor
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1-10
    private Integer classNumber;

    // "Monday", "Tuesday", etc.
    private String dayOfWeek;

    // e.g. "8:00-9:00", "9:00-10:00"
    private String timeSlot;

    // Which period number it is (1st, 2nd, 3rd...)
    private Integer periodNumber;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}