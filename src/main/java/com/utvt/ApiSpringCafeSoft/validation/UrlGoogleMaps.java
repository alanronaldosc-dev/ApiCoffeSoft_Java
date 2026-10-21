package com.utvt.ApiSpringCafeSoft.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que un campo de texto sea una URL legítima de Google Maps.
 *
 * Solo se aceptan:
 *   - https://maps.google.com/...
 *   - https://goo.gl/maps/...
 *
 * Rechaza javascript:, data:, URLs relativas y dominios falsos
 * (p. ej. https://maps.google.com.evil.com), evitando Open Redirect / XSS.
 */
@Documented
@Constraint(validatedBy = UrlGoogleMapsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlGoogleMaps {

    String message() default "Debe ser una URL de Google Maps: https://maps.google.com/..., https://www.google.com/maps/..., https://maps.app.goo.gl/... o https://goo.gl/maps/...";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
