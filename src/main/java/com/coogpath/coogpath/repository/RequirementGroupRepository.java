package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequirementGroup;

public interface  RequirementGroupRepository extends JpaRepository<RequirementGroup, Long>
{
    List<RequirementGroup> findByDegreeProgramProgramId(Long programId);
    // Fetches all the requirement buckets (CS Core, Math, etc.) for a specific degree
}
