-- COURSE

CREATE TABLE course (
    course_id   INT AUTO_INCREMENT PRIMARY KEY,
    subject     VARCHAR(15)  NOT NULL COMMENT 'e.g. COSC, MATH, ENGL',
    number      VARCHAR(15)  NOT NULL COMMENT 'e.g. 1436, 2436',
    title       VARCHAR(100) NOT NULL,
    credits     INT          NOT NULL,
    UNIQUE (subject, number)
);


-- TERM

CREATE TABLE term (
    term_id INT AUTO_INCREMENT PRIMARY KEY,
    year    INT                            NOT NULL,
    season  ENUM('FALL','SPRING','SUMMER') NOT NULL,
    UNIQUE (year, season)
);


-- REQUISITE NODE
-- defined before requisite_rule because requisite_rule references it

CREATE TABLE requisite_node (
    node_id        INT AUTO_INCREMENT PRIMARY KEY,
    operator       ENUM('AND','OR','COURSE','CONDITION') NOT NULL,
    course_id      INT         COMMENT 'only set when operator = COURSE',
    condition_code VARCHAR(50) COMMENT 'e.g. JUNIOR_STANDING',
    parent_node_id INT         COMMENT 'null = root node',
    sort_order     INT         NOT NULL DEFAULT 0,
    rule_id        INT         COMMENT 'set after requisite_rule is created',
    FOREIGN KEY (course_id)      REFERENCES course(course_id),
    FOREIGN KEY (parent_node_id) REFERENCES requisite_node(node_id)
);


-- REQUISITE RULE

CREATE TABLE requisite_rule (
    rule_id      INT AUTO_INCREMENT PRIMARY KEY,
    course_id    INT                    NOT NULL COMMENT 'the course that HAS this prereq',
    type         ENUM('PREREQ','COREQ') NOT NULL,
    root_node_id INT                    NOT NULL,
    FOREIGN KEY (course_id)    REFERENCES course(course_id),
    FOREIGN KEY (root_node_id) REFERENCES requisite_node(node_id)
);

ALTER TABLE requisite_node
    ADD CONSTRAINT fk_reqnode_rule
    FOREIGN KEY (rule_id) REFERENCES requisite_rule(rule_id);


-- DEGREE PROGRAM

CREATE TABLE degree_program (
    program_id             INT AUTO_INCREMENT PRIMARY KEY,
    name                   VARCHAR(100) NOT NULL,
    college                VARCHAR(100),
    catalog_year_start     INT NOT NULL,
    catalog_year_end       INT COMMENT 'null = still active',
    total_credits_required INT NOT NULL
);


-- REQUIREMENT GROUP

CREATE TABLE requirement_group (
    group_id        INT AUTO_INCREMENT PRIMARY KEY,
    program_id      INT          NOT NULL,
    name            VARCHAR(100) NOT NULL,
    rule_type       ENUM('ALL_OF','CHOOSE_N','MIN_CREDITS','AT_LEAST_FROM_LIST') NOT NULL,
    min_credits     INT COMMENT 'used for MIN_CREDITS and AT_LEAST_FROM_LIST',
    min_courses     INT COMMENT 'used for CHOOSE_N',
    parent_group_id INT COMMENT 'for nested requirement groups',
    FOREIGN KEY (program_id)      REFERENCES degree_program(program_id),
    FOREIGN KEY (parent_group_id) REFERENCES requirement_group(group_id)
);


-- COURSE SET

CREATE TABLE course_set (
    course_set_id INT          AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL
);


-- COURSE SET COURSE

CREATE TABLE course_set_course (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    course_set_id INT NOT NULL,
    course_id     INT NOT NULL,
    FOREIGN KEY (course_set_id) REFERENCES course_set(course_set_id),
    FOREIGN KEY (course_id)     REFERENCES course(course_id),
    UNIQUE (course_set_id, course_id)
);


-- REQUIREMENT ITEM

CREATE TABLE requirement_item (
    item_id       INT AUTO_INCREMENT PRIMARY KEY,
    group_id      INT        NOT NULL,
    course_id     INT        COMMENT 'set if a specific course is required',
    course_set_id INT        COMMENT 'set if choosing from a list',
    required      BOOLEAN    NOT NULL DEFAULT TRUE,
    min_grade     VARCHAR(2) COMMENT 'e.g. C, B',
    FOREIGN KEY (group_id)      REFERENCES requirement_group(group_id),
    FOREIGN KEY (course_id)     REFERENCES course(course_id),
    FOREIGN KEY (course_set_id) REFERENCES course_set(course_set_id)
);


-- STUDENT

CREATE TABLE student (
    student_id         INT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    email              VARCHAR(100) NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    program_id         INT,
    catalog_year       INT,
    max_credits_fall   INT     NOT NULL DEFAULT 15,
    max_credits_spring INT     NOT NULL DEFAULT 15,
    max_credits_summer INT     NOT NULL DEFAULT 6,
    include_summer     BOOLEAN NOT NULL DEFAULT FALSE,
    capstone_choice    VARCHAR(20) DEFAULT 'SENIOR_SE',
    free_elective_credits INT NOT NULL DEFAULT 0,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES degree_program(program_id)
);


-- STUDENT COURSE

CREATE TABLE student_course (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT                                              NOT NULL,
    course_id  INT                                              NOT NULL,
    term_id    INT                                              COMMENT 'nullable for transfer/planned',
    status     ENUM('TAKEN','IN_PROGRESS','PLANNED','TRANSFER') NOT NULL,
    grade      VARCHAR(2) COMMENT 'A, B, C, D, F, W',
    attempt_no INT        NOT NULL DEFAULT 1,
    created_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(student_id),
    FOREIGN KEY (course_id)  REFERENCES course(course_id),
    FOREIGN KEY (term_id)    REFERENCES term(term_id)
);


-- ROADMAP SNAPSHOT

CREATE TABLE roadmap_snapshot (
    snapshot_id       INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT         NOT NULL,
    target_graduation VARCHAR(20) COMMENT 'e.g. Spring 2027',
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE roadmap_semester (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    snapshot_id    INT         NOT NULL,
    term_label     VARCHAR(50) NOT NULL COMMENT 'e.g. Fall 2025',
    semester_order INT         NOT NULL,
    FOREIGN KEY (snapshot_id) REFERENCES roadmap_snapshot(snapshot_id)
);

CREATE TABLE roadmap_semester_course (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    semester_id          INT  NOT NULL,
    course_id            INT  NOT NULL,
    requirement_group_id INT  COMMENT 'which group this course satisfies',
    reason               TEXT COMMENT 'AI or algorithm explanation',
    FOREIGN KEY (semester_id)          REFERENCES roadmap_semester(id),
    FOREIGN KEY (course_id)            REFERENCES course(course_id),
    FOREIGN KEY (requirement_group_id) REFERENCES requirement_group(group_id)
);
