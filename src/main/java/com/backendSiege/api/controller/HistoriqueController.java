package com.backendSiege.api.controller;

import com.backendSiege.api.dto.HistoriqueResponseDto;
import com.backendSiege.domain.model.Historique;
import com.backendSiege.domain.model.TypeHistoriqueEnum;
import com.backendSiege.domain.service.HistoriqueService;
import com.backendSiege.infrastructure.mapper.HistoriqueMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HistoriqueController {

    private final HistoriqueService historiqueService;
    private final HistoriqueMapper historiqueMapper;

    public HistoriqueController(HistoriqueService historiqueService, HistoriqueMapper historiqueMapper) {
        this.historiqueService = historiqueService;
        this.historiqueMapper = historiqueMapper;
    }

    @GetMapping("/historique/{idLot}")
    public ResponseEntity<List<HistoriqueResponseDto>> getAllHistoriqueForLot(@PathVariable UUID idLot, @RequestParam(value = "type", required = false) String type) {
        TypeHistoriqueEnum enumType = null;

        if (type != null && !type.trim().isEmpty()) {
            String normalizedType = type.trim().toUpperCase();

            if ("ALERT".equals(normalizedType)) {
                normalizedType = "ALERTE";
            }
            try {
                enumType = TypeHistoriqueEnum.valueOf(normalizedType);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }
        List<Historique> historiques = historiqueService.findAllHistoriqueForLot(idLot, enumType);

        return ResponseEntity.ok(historiques.stream()
                .map(historiqueMapper::toHistoriqueResponseDto)
                .toList());
    }
}
