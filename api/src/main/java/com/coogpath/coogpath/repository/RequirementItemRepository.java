package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequirementItem;

public interface RequirementItemRepository extends JpaRepository<RequirementItem, Long>
{
    List<RequirementItem> findByRequirementGroupGroupId(Long groupId);
}
