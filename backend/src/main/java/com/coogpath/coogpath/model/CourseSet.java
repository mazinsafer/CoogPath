package com.coogpath.coogpath.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_set")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSet 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "course_set_id")
    private Long courseSetId;

    @Column(nullable=false, length=100 )
    private String name;
}
