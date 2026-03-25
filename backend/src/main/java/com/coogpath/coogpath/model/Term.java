package com.coogpath.coogpath.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "term", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"year", "season"})
})
public class Term 
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Integer termId;

    @Column(nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season;

    public enum Season {
        FALL, SPRING, SUMMER
    }

    public Term()
    {}

    public Term(Integer termId, Integer year, Season season)
    {
        this.termId = termId;
        this.year = year;
        this.season = season;
    }

    public Integer getTermId()
    {
        return this.termId;
    }

    public void setTermId(Integer termId)
    {
        this.termId = termId;
    }

    public Integer getYear()
    {
        return this.year;
    }

    public void setYear(Integer year)
    {
        this.year = year;
    }

    public Season getSeason()
    {
        return this.season;

    }
    public void setSeason(Season season)
    {
        this.season = season;
    }
}
