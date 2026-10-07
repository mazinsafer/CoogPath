package com.coogpath.coogpath.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.dto.PlannedTerm;
import com.coogpath.coogpath.dto.RequirementGroupProgress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

class AdvisorServiceTest {
    @TempDir Path temporaryDirectory;

    @Test
    void readsAdvisorKeyAddedToLocalEnvAfterStartup() throws Exception {
        Path envFile = temporaryDirectory.resolve(".env");
        assertEquals("", AdvisorService.readLocalApiKey(envFile));
        Files.writeString(envFile, "OPENAI_API_KEY= test-key\n");
        assertEquals("test-key", AdvisorService.readLocalApiKey(envFile));
        Files.writeString(envFile, "OPENAI_API_KEY=replacement-key\n");
        assertEquals("replacement-key", AdvisorService.readLocalApiKey(envFile));
    }

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
        assertEquals("Return JSON only.\nQuestion", AdvisorService.modelInput("Question", true));
        assertEquals("SELECT COUNT(*) FROM roadmap_terms", AdvisorService.normalizeSql("SELECT COUNT(*) FROM roadmap_terms;"));
        assertTrue(AdvisorService.safeSqlOrOverview("DROP TABLE roadmap_terms").contains("FROM roadmap_terms"));
        assertThrows(ResponseStatusException.class, () -> AdvisorService.validateSql("DROP TABLE roadmap_courses"));
        assertThrows(ResponseStatusException.class, () -> AdvisorService.validateSql("SELECT * FROM roadmap_courses; DELETE FROM roadmap_courses"));
        assertThrows(ResponseStatusException.class, () -> AdvisorService.validateSql("SELECT * FROM read_csv('/etc/passwd')"));
    }
}
