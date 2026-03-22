package com.coogpath.coogpath.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course", uniqueConstraints = {@UniqueConstraint(columnNames={"subject", "number"})} )
public class Course 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "course_id")
    private Long courseId;

    @Column(nullable = false, length = 15)
    private String subject;

    @Column(nullable= false, length = 15)
    private String number;
    
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer credits;

    public Course()
    {

    }

    public Course(Long courseId, String subject, String number, String title, Integer credits)
    {
        this.courseId = courseId;
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.credits = credits;
    }

    public Long getCourseId()
    {
        return this.courseId;
    }

    public void setCourseId(Long courseId)
    {
        this.courseId = courseId;
    }

    public String getSubject()
    {
        return this.subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getNumber()
    {
        return this.number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getCredits()
    {
        return this.credits;
    }

    public void setCredits(Integer credits)
    {
        this.credits = credits;
    }
}
