package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.exception.RecursoNoEncontradoException;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.validation.UrlGoogleMapsValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private RateLimiterService rateLimiterService;

    // Validador compartido: defensa en profundidad además de @UrlGoogleMaps
    private static final UrlGoogleMapsValidator VALIDADOR_URL =
            new UrlGoogleMapsValidator();

    // ============================================
    // VALIDACIÓN DE URL DE GOOGLE MAPS
    // ============================================

    /**
     * Rechaza javascript:, data:, URLs relativas, dominios falsos
     * (p. ej. https://maps.google.com.evil.com) y cualquier cosa que
     * no sea https://maps.google.com/... o https://goo.gl/maps/...
     */
    private void validarLinkGoogleMaps(String url) {
        if (!VALIDADOR_URL.isValid(url, null)) {
            throw new IllegalArgumentException(
                    "El enlace debe ser una URL de Google Maps: "
                            + "https://maps.google.com/..., "
                            + "https://www.google.com/maps/..., "
                            + "https://maps.app.goo.gl/... o "
                            + "https://goo.gl/maps/..."
            );
        }
    }

    // ============================================
    // IDENTIDAD Y AUTORIZACIÓN (anti-IDOR)
    // ============================================

    private Authentication autenticacion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("No autenticado");
        }
        return auth;
    }

    private boolean esAdmin() {
        return autenticacion().getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Usuario usuarioActual() {
        String email = autenticacion().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
    }

    /**
     * Un repartidor solo puede acceder a los clientes que pertenecen
     * a UNA RUTA ASIGNADA A ÉL. Cualquier otro ID (adivinado o
     * secuencial) recibe 403. El admin puede acceder a todos.
     */
    private void verificarAcceso(Cliente cliente) {
        if (esAdmin()) return;

        Usuario actual = usuarioActual();
        boolean esSuRuta = cliente.getRuta() != null
                && cliente.getRuta().getRepartidor() != null
                && cliente.getRuta().getRepartidor().getId().equals(actual.getId());

        if (!esSuRuta) {
            throw new AccessDeniedException(
                    "No tienes permiso para acceder a este cliente"
            );
        }
    }

    // ============================================
    // HU-011 - REGISTRAR CLIENTE
    // ============================================

    @Transactional
    public Cliente crearCliente(Cliente cliente) {
        validarLinkGoogleMaps(cliente.getLinkGoogleMaps());

        // Ignorar cualquier id manipulado desde el cliente
        // (evita sobreescribir el registro de otra persona)
        cliente.setId(null);
        cliente.setActivo(true);

        Cliente guardado = clienteRepository.save(cliente);

        auditoriaService.registrar(
                usuarioActual(), "CREAR", "CLIENTE",
                guardado.getId(), "Cliente registrado: " + guardado.getNombre()
        );

        return guardado;
    }

    // ============================================
    // HU-011 - CATÁLOGO DE CLIENTES
    // ============================================

    /**
     * El admin ve todos los clientes; el repartidor solo los de su ruta.
     */
    public List<Cliente> obtenerClientes() {
        if (esAdmin()) {
            return clienteRepository.findAll();
        }

        Usuario actual = usuarioActual();
        return clienteRepository.findByRutaRepartidorId(actual.getId());
    }

    // Obtener por ID (con control de acceso)
    public Cliente obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        verificarAcceso(cliente);
        return cliente;
    }

    // ============================================
    // HU-011 - ACTUALIZAR CLIENTE
    // ============================================

    @Transactional
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        // obtenerClientePorId ya aplica verificarAcceso
        Cliente cliente = obtenerClientePorId(id);
        validarLinkGoogleMaps(clienteActualizado.getLinkGoogleMaps());

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setDomicilio(clienteActualizado.getDomicilio());
        cliente.setLinkGoogleMaps(clienteActualizado.getLinkGoogleMaps());
        cliente.setDiasReparto(clienteActualizado.getDiasReparto());
        cliente.setFrecuencia(clienteActualizado.getFrecuencia());
        cliente.setPrecioPorGarrafon(clienteActualizado.getPrecioPorGarrafon());
        cliente.setGarrafonPreferencia(clienteActualizado.getGarrafonPreferencia());
        cliente.setFotografiaDomicilio(clienteActualizado.getFotografiaDomicilio());
        // La ruta no se puede cambiar desde aquí (evita cambiar de dueño)

        Cliente guardado = clienteRepository.save(cliente);

        auditoriaService.registrar(
                usuarioActual(), "ACTUALIZAR", "CLIENTE",
                guardado.getId(), "Cliente actualizado: " + guardado.getNombre()
        );

        return guardado;
    }

    // ============================================
    // HU-011 - DAR DE BAJA / ALTA
    // ============================================

    @Transactional
    public Cliente darDeBaja(Long id) {
        Cliente cliente = obtenerClientePorId(id);
        cliente.setActivo(false);
        Cliente guardado = clienteRepository.save(cliente);

        auditoriaService.registrar(
                usuarioActual(), "BAJA", "CLIENTE",
                guardado.getId(), "Cliente dado de baja: " + guardado.getNombre()
        );

        return guardado;
    }

    @Transactional
    public Cliente darDeAlta(Long id) {
        Cliente cliente = obtenerClientePorId(id);
        cliente.setActivo(true);
        Cliente guardado = clienteRepository.save(cliente);

        auditoriaService.registrar(
                usuarioActual(), "ALTA", "CLIENTE",
                guardado.getId(), "Cliente dado de alta: " + guardado.getNombre()
        );

        return guardado;
    }

    // ============================================
    // HU-011 - ELIMINAR CLIENTE
    // ============================================

    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        verificarAcceso(cliente);

        Usuario actual = usuarioActual();

        // Rate limiting: máximo 3 eliminaciones por minuto por usuario
        // (protección contra automatización desde la consola)
        rateLimiterService.verificar(
                "eliminar-cliente", actual.getId(), 3, Duration.ofMinutes(1)
        );

        clienteRepository.deleteById(id);

        auditoriaService.registrar(
                actual, "ELIMINAR", "CLIENTE", id,
                "Cliente eliminado: " + cliente.getNombre()
        );
    }
}
