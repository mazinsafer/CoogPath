package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequisiteRule;

public interface RequisiteRuleRepository extends JpaRepository<RequisiteRule, Long> 
{
    List<RequisiteRule> findByCourseCourseId(Long courseId);
    // Fetches all the requirement buckets (CS Core, Math, etc.) for a specific degree
}
