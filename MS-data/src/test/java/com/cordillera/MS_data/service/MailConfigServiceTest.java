package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.MailConfigDto;
import com.cordillera.MS_data.dto.MailConfigResponse;
import com.cordillera.MS_data.entity.MailConfig;
import com.cordillera.MS_data.repository.MailConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class MailConfigServiceTest {

    @Mock MailConfigRepository repository;

    @InjectMocks MailConfigService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "defHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(service, "defPort", 587);
        ReflectionTestUtils.setField(service, "defUsername", "user@cordillera.cl");
        ReflectionTestUtils.setField(service, "defPassword", "secret");
        ReflectionTestUtils.setField(service, "defFrom", "");
    }

    private MailConfig guardada() {
        return MailConfig.builder().id(1L).host("smtp.test").port(2525)
                .username("guardado@cordillera.cl").password("pass").fromAddress("from@cordillera.cl")
                .auth(true).starttls(true).build();
    }

    @Test
    void obtenerEfectiva_desdeBD() {
        when(repository.findById(1L)).thenReturn(Optional.of(guardada()));

        assertThat(service.obtenerEfectiva().getHost()).isEqualTo("smtp.test");
    }

    @Test
    void obtenerEfectiva_porDefecto_cuandoNoHayEnBD() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MailConfig cfg = service.obtenerEfectiva();

        assertThat(cfg.getHost()).isEqualTo("smtp.gmail.com");
        assertThat(cfg.getFromAddress()).isEqualTo("user@cordillera.cl"); // defFrom vacío → username
    }

    @Test
    void guardar_nuevaConfig_conPassword() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(MailConfig.class))).thenAnswer(i -> i.getArgument(0));

        MailConfigDto dto = new MailConfigDto();
        dto.setHost("smtp.nuevo");
        dto.setPort(465);
        dto.setUsername("nuevo@cordillera.cl");
        dto.setPassword("nuevaPass");
        dto.setFromAddress("");
        dto.setAuth(null);
        dto.setStarttls(null);

        MailConfigResponse r = service.guardar(dto);

        assertThat(r.getHost()).isEqualTo("smtp.nuevo");
        assertThat(r.getFromAddress()).isEqualTo("nuevo@cordillera.cl"); // fromAddress vacío → username
        assertThat(r.getAuth()).isTrue();
        assertThat(r.isPasswordConfigurada()).isTrue();
    }

    @Test
    void guardar_passwordEnBlanco_conservaExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(guardada()));
        when(repository.save(any(MailConfig.class))).thenAnswer(i -> i.getArgument(0));

        MailConfigDto dto = new MailConfigDto();
        dto.setHost("smtp.edit");
        dto.setPort(587);
        dto.setUsername("edit@cordillera.cl");
        dto.setPassword("   "); // en blanco → no cambia
        dto.setFromAddress("from2@cordillera.cl");
        dto.setAuth(false);
        dto.setStarttls(false);

        MailConfigResponse r = service.guardar(dto);

        assertThat(r.getHost()).isEqualTo("smtp.edit");
        assertThat(r.getAuth()).isFalse();
        assertThat(r.isPasswordConfigurada()).isTrue(); // conservó "pass"
    }

    @Test
    void crearMailSender_configuraHostYPuerto() {
        when(repository.findById(1L)).thenReturn(Optional.of(guardada()));

        JavaMailSenderImpl sender = service.crearMailSender();

        assertThat(sender.getHost()).isEqualTo("smtp.test");
        assertThat(sender.getPort()).isEqualTo(2525);
    }

    @Test
    void obtenerRemitente_usaFromAddress() {
        when(repository.findById(1L)).thenReturn(Optional.of(guardada()));
        assertThat(service.obtenerRemitente()).isEqualTo("from@cordillera.cl");
    }

    @Test
    void obtenerResponse_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(guardada()));
        assertThat(service.obtenerResponse().getUsername()).isEqualTo("guardado@cordillera.cl");
    }
}
