package com.fiap.parcelservice.adapter.web;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.application.service.ParcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller REST do Parcel Service — endpoints para receber, listar, consultar e marcar retirada de encomendas.
 */
@Tag(name = "Parcel", description = "Endpoints para registro, consulta e controle de retirada de encomendas")
@RestController
@RequestMapping("/api")
@Validated
public class ParcelController {

    private static final Logger log = LoggerFactory.getLogger(ParcelController.class);

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    /**
     * Recebe uma encomenda na portaria — persiste e publica evento para fila de processamento.
     * Retorna 201 Created com Location apontando para o recurso criado.
     */
    @Operation(summary = "Registrar nova encomenda na portaria")
    @PreAuthorize("hasRole('PORTEIRO')")
    @PostMapping("/parcels")
    public ResponseEntity<ParcelResponse> receiveParcel(@Valid @RequestBody ParcelRequest request, Authentication authentication) {
        log.info("Recebendo nova encomenda para destinatário={} apt={} por={}", request.getResidentName(), request.getApartment(), authentication != null ? authentication.getName() : "unknown");

        ParcelResponse created = parcelService.receiveParcel(request);

        if (created != null && created.getId() != null) {
            URI location = URI.create("/api/parcels/" + created.getId());
            return ResponseEntity.created(location).body(created);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Consulta uma encomenda por id.
     */
    @Operation(summary = "Consultar encomenda por ID")
    @GetMapping("/parcels/{id}")
    public ResponseEntity<ParcelResponse> getParcel(@PathVariable("id") Long id) {
        ParcelResponse resp = parcelService.getParcel(id);
        if (resp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Lista todas as encomendas.
     */
    @Operation(summary = "Listar todas as encomendas")
    @GetMapping("/parcels")
    public ResponseEntity<List<ParcelResponse>> listParcels() {
        List<ParcelResponse> list = parcelService.listParcels();
        return ResponseEntity.ok(list);
    }

    /**
     * Marca a encomenda como retirada (pickup) pelo porteiro.
     * O parâmetro Authentication é usado para identificar quem efetuou a baixa.
     */
    @Operation(summary = "Marcar encomenda como retirada (pickup)")
    @PreAuthorize("hasRole('PORTEIRO')")
    @PostMapping("/parcels/{id}/pickup")
    public ResponseEntity<ParcelResponse> markPickedUp(@PathVariable("id") Long id, Authentication authentication) {
        try {
            String porterUsername = authentication != null ? authentication.getName() : null;
            ParcelResponse resp = parcelService.markPickedUp(id, porterUsername);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao marcar pickup para id={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

}