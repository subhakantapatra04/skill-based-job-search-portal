CREATE DATABASE IF NOT EXISTS skill_job_search
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE skill_job_search;
-- JPA creates and updates the entity tables at local startup (ddl-auto=update).
-- This script intentionally only provisions the database; use Flyway/Liquibase
-- versioned migrations for a production deployment.
