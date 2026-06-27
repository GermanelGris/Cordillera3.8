package com.cordillera.MS_login.service;

import com.cordillera.MS_login.dto.RegistroRequest;
import com.cordillera.MS_login.dto.UsuarioResponse;
import com.cordillera.MS_login.dto.UsuarioUpdateRequest;
import com.cordillera.MS_login.entity.RolEntity;
import com.cordillera.MS_login.entity.Usuario;
import com.cordillera.MS_login.kafka.AuthEventPublisher;
import com.cordillera.MS_login.repository.RolRepository;
import com.cordillera.MS_login.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthServiceCrudTest {

    @Mock UsuarioRepository  usuarioRepository;
    @Mock RolRepository      rolRepository;
    @Mock JwtService         jwtService;
    @Mock PasswordEncoder    passwordEncoder;
    @Mock AuthEventPublisher eventPublisher;

    @InjectMocks AuthService authService;

    private RolEntity rolUsuario;
    private RolEntity rolAdmin;
    private Usuario   usuario;

    @BeforeEach
    void setUp() {
        rolUsuario = RolEntity.builder().id(3).nombre("USUARIO").activo(true).createdAt(LocalDateTime.now()).build();
        rolAdmin   = RolEntity.builder().id(1).nombre("ADMIN").activo(true).createdAt(LocalDateTime.now()).build();
        usuario    = Usuario.builder().id(1L).nombre("Juan").email("juan@cordillera.cl")
                .passwordHash("hash").rol(rolUsuario).activo(true).createdAt(LocalDateTime.now()).build();
    }

    // ── obtenerUsuario ───────────────────────────────────────────────────────
    @Test
    void obtenerUsuario_existente_ok() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse r = authService.obtenerUsuario(1L);

        assertThat(r.getEmail()).isEqualTo("juan@cordillera.cl");
        assertThat(r.getRol()).isEqualTo("USUARIO");
    }

    @Test
    void obtenerUsuario_noExiste_lanza404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.obtenerUsuario(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no encontrado");
    }

    // ── actualizarUsuario ────────────────────────────────────────────────────
    @Test
    void actualizarUsuario_completo_actualizaTodo() {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setNombre("Juan Pérez");
        req.setEmail("juanp@cordillera.cl");
        req.setActivo(false);
        req.setRol("admin");
        req.setPassword("nuevaPass");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("juanp@cordillera.cl")).thenReturn(false);
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin));
        when(passwordEncoder.encode("nuevaPass")).thenReturn("hashNuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = authService.actualizarUsuario(1L, req);

        assertThat(r.getNombre()).isEqualTo("Juan Pérez");
        assertThat(r.getRol()).isEqualTo("ADMIN");
        assertThat(r.getActivo()).isFalse();
    }

    @Test
    void actualizarUsuario_sinCambiosOpcionales_ok() {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setNombre("Juan");
        req.setEmail("juan@cordillera.cl"); // mismo email → no valida duplicado

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = authService.actualizarUsuario(1L, req);

        assertThat(r.getEmail()).isEqualTo("juan@cordillera.cl");
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }

    @Test
    void actualizarUsuario_emailDuplicado_lanza409() {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setNombre("Juan");
        req.setEmail("otro@cordillera.cl");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("otro@cordillera.cl")).thenReturn(true);

        assertThatThrownBy(() -> authService.actualizarUsuario(1L, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ya está en uso");
    }

    @Test
    void actualizarUsuario_rolNoExiste_lanza400() {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setNombre("Juan");
        req.setEmail("juan@cordillera.cl");
        req.setRol("SUPERADMIN");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findByNombre("SUPERADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.actualizarUsuario(1L, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    // ── eliminar / roles / registro custom ───────────────────────────────────
    @Test
    void eliminarUsuario_ok() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        authService.eliminarUsuario(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void listarRoles_ok() {
        when(rolRepository.findAll()).thenReturn(List.of(rolAdmin, rolUsuario));

        assertThat(authService.listarRoles()).containsExactly("ADMIN", "USUARIO");
    }

    @Test
    void registro_conRolCustom_usaRolIndicado() {
        RegistroRequest req = new RegistroRequest();
        req.setNombre("Vendedor");
        req.setEmail("vend@cordillera.cl");
        req.setPassword("Pass1234");
        req.setRol("admin");

        when(usuarioRepository.existsByEmail("vend@cordillera.cl")).thenReturn(false);
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin));
        when(passwordEncoder.encode("Pass1234")).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(5L);
            return u;
        });

        UsuarioResponse r = authService.registro(req);

        assertThat(r.getRol()).isEqualTo("ADMIN");
        assertThat(r.getId()).isEqualTo(5L);
    }
}
