package com.cordillera.MS_login.service;

import com.cordillera.MS_login.dto.LoginRequest;
import com.cordillera.MS_login.dto.LoginResponse;
import com.cordillera.MS_login.dto.RegistroRequest;
import com.cordillera.MS_login.dto.UsuarioResponse;
import com.cordillera.MS_login.dto.UsuarioUpdateRequest;
import com.cordillera.MS_login.entity.RolEntity;
import com.cordillera.MS_login.entity.Usuario;
import com.cordillera.MS_login.kafka.AuthEventPublisher;
import com.cordillera.MS_login.repository.RolRepository;
import com.cordillera.MS_login.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthEventPublisher eventPublisher;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getEmail()));

        if (!usuario.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String rolNombre = usuario.getRol().getNombre();
        Map<String, Object> claims = Map.of(
                "rol",    rolNombre,
                "nombre", usuario.getNombre()
        );

        String token = jwtService.generarToken(usuario.getEmail(), claims);
        eventPublisher.publicarLogin(usuario.getEmail());

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .nombre(usuario.getNombre())
                .rol(rolNombre)
                .expiresIn(86400000L)
                .build();
    }

    public UsuarioResponse registro(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        String rolNombre = (request.getRol() != null && !request.getRol().isBlank())
                ? request.getRol().toUpperCase()
                : "USUARIO";

        RolEntity rolEntity = rolRepository.findByNombre(rolNombre)
                .orElseGet(() -> rolRepository.findByNombre("USUARIO")
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre)));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rolEntity)
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse obtenerUsuario(Long id) {
        return toResponse(buscarUsuario(id));
    }

    public UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarUsuario(id);

        // Email único: solo validar si cambió
        if (!usuario.getEmail().equalsIgnoreCase(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está en uso");
        }

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        if (request.getRol() != null && !request.getRol().isBlank()) {
            String rolNombre = request.getRol().toUpperCase();
            RolEntity rolEntity = rolRepository.findByNombre(rolNombre)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rol no encontrado: " + rolNombre));
            usuario.setRol(rolEntity);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = buscarUsuario(id);
        usuarioRepository.delete(usuario);
    }

    public List<String> listarRoles() {
        return rolRepository.findAll().stream().map(RolEntity::getNombre).toList();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario " + id + " no encontrado"));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol().getNombre())
                .activo(u.getActivo())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
