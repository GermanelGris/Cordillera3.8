package com.cordillera.MS_reportes.client;

import com.cordillera.MS_reportes.dto.KpiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "ms-kpi",
        url = "${feign.client.kpi.url}",
        fallback = KpiClientFallback.class
)
public interface KpiClient {

    @GetMapping("/kpi")
    List<KpiDto> listarTodos();

    @GetMapping("/kpi/periodo/{periodo}")
    List<KpiDto> listarPorPeriodo(@PathVariable("periodo") String periodo);
}
