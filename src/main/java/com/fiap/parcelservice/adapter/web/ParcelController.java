package com.fiap.parcelservice.adapter.web;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.application.service.ParcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * Controller REST do Parcel Service — endpoints para receber, listar, consultar e marcar retirada de encomendas.
 */
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
    @PostMapping("/parcels")
    public ResponseEntity<ParcelResponse> receiveParcel(@Valid @RequestBody ParcelRequest request) {
        log.info("Recebendo nova encomenda para destinatário={} apt={}", request.getRecipientName(), request.getApartment());

        ParcelResponse created = parcelService.receiveParcel(request);

        HttpHeaders headers = new HttpHeaders();
        if (created != null && created.getId() != null) {
            headers.setLocation(URI.create("/api/parcels/" + created.getId()));
            return new ResponseEntity<>(created, headers, HttpStatus.CREATED);
        } else {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Consulta uma encomenda por id.
     */
    @GetMapping("/parcels/{id}")
    public ResponseEntity<ParcelResponse> getParcel(@PathVariable("id") Long id) {
        ParcelResponse resp = parcelService.getParcel(id);
        if (resp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Lista todas as encomendas.
     */
    @GetMapping("/parcels")
    public ResponseEntity<List<ParcelResponse>> listParcels() {
        List<ParcelResponse> list = parcelService.listParcels();
        return ResponseEntity.ok(list);
    }

    /**
     * Marca a encomenda como retirada (pickup) pelo porteiro.
     * Query param 'pickedBy' identifica quem efetuou a baixa (p.ex. nome do porteiro).
     */
    @PostMapping("/parcels/{id}/pickup")
    public ResponseEntity<ParcelResponse> markPickedUp(@PathVariable("id") Long id,
                                                       @RequestParam(value = "pickedBy", required = false) String pickedBy) {
        try {
            ParcelResponse resp = parcelService.markPickedUp(id, pickedBy);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro ao marcar pickup para id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}