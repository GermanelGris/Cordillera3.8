package com.cordillera.MS_kpi.client;

import com.cordillera.MS_kpi.dto.DatoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "ms-data",
        url = "${feign.client.data.url}",
        fallback = DatoClientFallback.class
)
public interface DatoClient {

    @GetMapping("/datos")
    List<DatoDto> listarTodos();

    @GetMapping("/datos/periodo/{periodo}")
    List<DatoDto> listarPorPeriodo(@PathVariable("periodo") String periodo);

    @GetMapping("/datos/tipo/{tipo}")
    List<DatoDto> listarPorTipo(@PathVariable("tipo") String tipo);
}
