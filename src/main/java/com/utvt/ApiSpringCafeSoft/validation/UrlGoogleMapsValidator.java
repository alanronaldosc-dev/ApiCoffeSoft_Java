package com.utvt.ApiSpringCafeSoft.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;

/**
 * Implementación de {@link UrlGoogleMaps}.
 *
 * Usa java.net.URI en lugar de startsWith para que no se deje engañar
 * por trucos como "https://maps.google.com.evil.com" o "javascript:...".
 *
 * Acepta las formas oficiales de Google Maps:
 *   - https://maps.google.com/...            (y variantes de país)
 *   - https://www.google.com/maps/...        (y variantes de país)
 *   - https://maps.app.goo.gl/...            (enlace corto oficial)
 *   - https://goo.gl/maps/...                (enlace corto legacy)
 */
public class UrlGoogleMapsValidator
        implements ConstraintValidator<UrlGoogleMaps, String> {

    // www.google.com / www.google.com.mx / www.google.es / ...
    private static final String GOOGLE_WEB =
            "www\\.google\\.[a-z]{2,3}(\\.[a-z]{2,3})?";

    // maps.google.com / maps.google.com.mx / maps.google.es / ...
    private static final String GOOGLE_MAPS =
            "maps\\.google\\.[a-z]{2,3}(\\.[a-z]{2,3})?";

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        // Campo opcional: null o vacío es válido
        if (valor == null || valor.isBlank()) {
            return true;
        }

        try {
            URI uri = URI.create(valor.trim());

            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();

            if (scheme == null || host == null) {
                return false; // p. ej. "javascript:alert(document.cookie)"
            }

            if (!"https".equalsIgnoreCase(scheme)) {
                return false; // solo se permite HTTPS
            }

            host = host.toLowerCase();

            // 1. maps.google.com (cualquier ruta) y variantes de país
            if ("maps.google.com".equals(host)
                    || host.matches(GOOGLE_MAPS)) {
                return true;
            }

            // 2. Enlace corto oficial de Google Maps
            if ("maps.app.goo.gl".equals(host)) {
                return true;
            }

            // 3. goo.gl/maps/...
            if ("goo.gl".equals(host)
                    && path != null && path.startsWith("/maps")) {
                return true;
            }

            // 4. www.google.com/maps/... y variantes de país
            if (path != null && path.startsWith("/maps")
                    && host.matches(GOOGLE_WEB)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false; // URL malformada
        }
    }
}
