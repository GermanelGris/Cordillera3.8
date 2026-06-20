package com.cordillera.MS_kpi.controller;

import com.cordillera.MS_kpi.dto.KpiDto;
import com.cordillera.MS_kpi.dto.KpiResponse;
import com.cordillera.MS_kpi.service.KpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kpi")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    @PostMapping("/calcular")
    public ResponseEntity<KpiResponse> calcular(@Valid @RequestBody KpiDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kpiService.calcular(dto));
    }

    @PostMapping("/calcular-desde-datos")
    public ResponseEntity<KpiResponse> calcularDesdeDatos(
            @RequestParam String tipoCalculo,
            @RequestParam String tipoDato,
            @RequestParam String periodo,
            @RequestParam String nombre) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kpiService.calcularDesdeDatos(tipoCalculo, tipoDato, periodo, nombre));
    }

    @GetMapping
    public ResponseEntity<List<KpiResponse>> listar() {
        return ResponseEntity.ok(kpiService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KpiResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(kpiService.buscarPorId(id));
    }

    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<List<KpiResponse>> listarPorPeriodo(@PathVariable String periodo) {
        return ResponseEntity.ok(kpiService.listarPorPeriodo(periodo));
    }

    @GetMapping("/tipo/{tipoCalculo}")
    public ResponseEntity<List<KpiResponse>> listarPorTipo(@PathVariable String tipoCalculo) {
        return ResponseEntity.ok(kpiService.listarPorTipo(tipoCalculo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KpiResponse> actualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(kpiService.actualizar(id, body.get("nombre"), body.get("descripcion")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        kpiService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MS-KPI OK");
    }
}
