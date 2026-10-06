---
name: flyway-migration-safety
description: Mandatory rules for any change to the database schema or seed data in CoogPath (api/src/main/resources/db/migration). Use before creating, editing, or running SQL, changing Flyway or JPA settings, or doing anything against the production database.
---

# Flyway migration safety

Production student accounts live in the same MySQL database as the degree data.
In April 2026, `db/init-prod.sql` dropped every table so Flyway could rerun V1.
Every student who had registered before then was deleted. These rules exist so
that never happens again.

## Hard rules

1. **Never edit, rename, reorder, or delete a migration that has been merged to
   `main`.** Flyway checksums applied files; a changed file fails startup in prod.
   Fix mistakes with a new migration.
2. **Never drop or truncate tables, and never `DELETE FROM student` or
   `student_course`.** No reset or init scripts. If a migration must remove
   degree data, delete specific rows by ID with a comment saying why (see V10).
3. **Never run `flyway clean`, `flyway repair`, or manual DDL against
   production.** `spring.flyway.clean-disabled=true` is set in the prod profile;
   keep it.
4. **Keep `spring.jpa.hibernate.ddl-auto=none`.** Hibernate must not create or
   alter tables.
5. **Take a backup before any migration that touches existing rows** (Railway
   dashboard → MySQL → Backups, or `mysqldump` with the prod env vars). Mention
   it in the PR description.
6. **Don't put credentials in properties files or migrations.**

## Naming and placement

- Path: `api/src/main/resources/db/migration/`
- Name: `V<next number>__<snake_case_description>.sql`, with two underscores.
  Files that don't match the pattern are silently ignored, so draft SQL left in
  that folder is dead code; don't leave any there.
- Check the highest existing version with `ls api/src/main/resources/db/migration`
  **and** on the branch you will merge into. Two branches each adding `V11`
  will conflict at deploy time.

## Writing a migration

- Use explicit primary keys for degree data (`course_id`, `group_id`,
  `node_id`, `rule_id`, `course_set_id`) so other migrations can reference
  them. Start from the highest ID in the existing migrations, plus one. The
  `adding-a-degree-plan` skill lists the current maximums.
- `requirement_item` and `course_set_course` rows use auto-increment IDs; don't
  set them.
- Wrap inserts that reference rows in the same file with
  `SET FOREIGN_KEY_CHECKS = 0;` … `SET FOREIGN_KEY_CHECKS = 1;` (existing
  migrations do this because `requisite_node` and `requisite_rule` reference
  each other).
- Start the file with a header comment that says what changes and which ID
  ranges it uses.
- Column additions: `ALTER TABLE ... ADD COLUMN ... DEFAULT ...` so existing
  rows stay valid. Add the field to the JPA entity in the same PR.

## Verifying

1. Run it against a **fresh** database so it works from V1:
   ```bash
   mysql -u root -e "CREATE DATABASE coogpath_check"
   DB_URL=jdbc:mysql://localhost:3306/coogpath_check ./mvnw -q test
   mysql -u root -e "DROP DATABASE coogpath_check"
   ```
   CI does the same against an empty MySQL service.
2. Run it against your normal local database (the incremental path prod takes).
3. Compare plan and requirements output for affected students before and after.

## Local drift

A local database can contain rows that no migration created (hand-run draft
scripts). For example, one dev database has a `course_set_id = 3` that does not
exist in any migration. Don't assume local data matches production; when
choosing new IDs, check the migration files and, if possible,
`SELECT MAX(...)` on production.
