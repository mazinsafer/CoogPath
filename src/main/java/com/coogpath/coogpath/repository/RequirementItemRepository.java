package com.coogpath.coogpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequirementItem;

public interface RequirementItemRepository extends JpaRepository<RequirementItem, Long>
{
    
}
