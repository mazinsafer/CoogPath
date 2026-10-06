package com.coogpath.coogpath.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Restricts a handler to the signed-in student's own data. The annotated method
 * must take the target student as a parameter named {@code studentId}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("#studentId != null && #studentId.toString() == authentication.name")
public @interface OwnStudentOnly {
}
