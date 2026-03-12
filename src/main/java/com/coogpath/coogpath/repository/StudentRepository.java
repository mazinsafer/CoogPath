package com.coogpath.coogpath.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long>
{
    Optional<Student> findByEmail(String email); // // Used for user login/authentication
}
