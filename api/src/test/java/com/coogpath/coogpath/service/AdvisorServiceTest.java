package com.coogpath.coogpath.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.dto.PlannedTerm;
import com.coogpath.coogpath.dto.RequirementGroupProgress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AdvisorServiceTest {
    @Test
    void studentPlanAndRequirementsCanBeQueriedTogether() throws Exception {
        PlanResult plan = new PlanResult(
                List.of(new PlannedTerm("FALL 2026", "FALL", 2026, 3,
                        List.of(new PlannedTerm.PlannedCourseDTO("COSC 1336", "Programming Fundamentals I", 3, "", "Required")))),
                List.of(), List.of());
        List<RequirementGroupProgress> requirements = List.of(new RequirementGroupProgress("Core", 6, 3,
                List.of(new RequirementGroupProgress.RequirementCourse("COSC 1336", "Programming Fundamentals I", 3, false))));

        try (Connection db = DriverManager.getConnection("jdbc:duckdb:")) {
            AdvisorService.populate(db, plan, requirements);
            try (Statement settings = db.createStatement()) {
                settings.execute("SET enable_external_access = false");
                settings.execute("SET autoinstall_known_extensions = false");
                settings.execute("SET autoload_known_extensions = false");
                settings.execute("SET allow_community_extensions = false");
                settings.execute("SET lock_configuration = true");
            }
            assertEquals("FALL 2026", AdvisorService.query(db,
                    "SELECT term_label FROM roadmap_courses WHERE course_code = 'COSC 1336'").getFirst().get("term_label"));
            assertEquals("3", AdvisorService.query(db,
                    "SELECT SUM(total_credits - completed_credits) AS remaining FROM degree_requirements")
                    .getFirst().get("remaining"));
        }
    }

    @Test
    void rejectsUnsafeStatements() {
        assertThrows(ResponseStatusException.class, () -> AdvisorService.validateSql("DROP TABLE roadmap_courses"));
        assertThrows(ResponseStatusException.class, () -> AdvisorService.validateSql("SELECT * FROM roadmap_courses; DELETE FROM roadmap_courses"));
        assertThrows(ResponseStatusException.class, () -> AdvisorService.validateSql("SELECT * FROM read_csv('/etc/passwd')"));
    }
}
