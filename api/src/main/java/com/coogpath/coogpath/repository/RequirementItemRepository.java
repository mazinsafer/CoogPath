package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequirementItem;

public interface RequirementItemRepository extends JpaRepository<RequirementItem, Long>
{
    /** Every item of a program in one query, with its course and course set already joined. */
    @EntityGraph(attributePaths = {"requirementGroup", "course", "courseSet"})
    List<RequirementItem> findByRequirementGroupDegreeProgramProgramIdOrderByItemIdAsc(Long programId);
}
