package com.utvt.ApiSpringCafeSoft.security;

import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CafeUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CafeUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(cuenta -> !Boolean.FALSE.equals(cuenta.getActivo()))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));

        User.UserBuilder user = User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .disabled(Boolean.FALSE.equals(usuario.getActivo()));

        if (Integer.valueOf(0).equals(usuario.getUserTipo())) {
            user.roles("ADMIN");
        } else if (Integer.valueOf(2).equals(usuario.getUserTipo())) {
            user.roles("CLIENT");
        } else if (Integer.valueOf(4).equals(usuario.getUserTipo())) {
            user.roles("DELIVERY");
        } else {
            user.roles("USER");
        }

        return user.build();
    }
}
