package com.cordillera.MS_login.controller;

import com.cordillera.MS_login.dto.LoginRequest;
import com.cordillera.MS_login.dto.LoginResponse;
import com.cordillera.MS_login.dto.RegistroRequest;
import com.cordillera.MS_login.dto.UsuarioResponse;
import com.cordillera.MS_login.dto.UsuarioUpdateRequest;
import com.cordillera.MS_login.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;

    @InjectMocks AuthController controller;

    @Test
    void login_devuelve200ConToken() {
        when(authService.login(any())).thenReturn(LoginResponse.builder().token("t").tipo("Bearer").build());

        var r = controller.login(new LoginRequest());

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().getToken()).isEqualTo("t");
    }

    @Test
    void registro_devuelve201() {
        when(authService.registro(any())).thenReturn(UsuarioResponse.builder().id(1L).email("a@b.cl").build());

        assertThat(controller.registro(new RegistroRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void listar_devuelve200() {
        when(authService.listarUsuarios()).thenReturn(List.of());

        assertThat(controller.listar().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void roles_devuelveLista() {
        when(authService.listarRoles()).thenReturn(List.of("ADMIN", "USUARIO"));

        assertThat(controller.roles().getBody()).containsExactly("ADMIN", "USUARIO");
    }

    @Test
    void obtener_devuelve200() {
        when(authService.obtenerUsuario(1L)).thenReturn(UsuarioResponse.builder().id(1L).build());

        assertThat(controller.obtener(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void crear_devuelve201() {
        when(authService.registro(any())).thenReturn(UsuarioResponse.builder().id(2L).build());

        assertThat(controller.crear(new RegistroRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void actualizar_devuelve200() {
        when(authService.actualizarUsuario(eq(1L), any())).thenReturn(UsuarioResponse.builder().id(1L).build());

        assertThat(controller.actualizar(1L, new UsuarioUpdateRequest()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminar_devuelve204() {
        var r = controller.eliminar(1L);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).eliminarUsuario(1L);
    }

    @Test
    void health_devuelveOk() {
        assertThat(controller.health().getBody()).contains("OK");
    }
}
