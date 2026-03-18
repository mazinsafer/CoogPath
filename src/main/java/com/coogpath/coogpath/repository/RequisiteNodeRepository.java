package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RequisiteNode;

public interface RequisiteNodeRepository extends JpaRepository<RequisiteNode, Long>
{
    List<RequisiteNode> findByRuleRuleId(Long ruleId);
}
