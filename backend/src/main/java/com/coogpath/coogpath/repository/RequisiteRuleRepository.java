package com.coogpath.coogpath.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequisiteRule;

public interface RequisiteRuleRepository extends JpaRepository<RequisiteRule, Long> 
{
    Optional<RequisiteRule> findByCourseCourseId(Long courseId);
}
