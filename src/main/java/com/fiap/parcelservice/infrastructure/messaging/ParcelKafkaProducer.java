package com.fiap.parcelservice.infrastructure.messaging;

import com.fiap.parcelservice.domain.model.Parcel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Producer Kafka para eventos relacionados a encomendas.
 *
 * Enviar eventos de domínio (PARCEL_RECEIVED e PARCEL_PICKED_UP) para o tópico
 * de entrada (parcelsInTopic). Consumidores (Notification Service, Audit) ficam responsáveis por processar
 * e enviar notificações/saídas.
 *
 */
@Component
public class ParcelKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ParcelKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String parcelsInTopic;
    private final String notificationsOutTopic;

    public ParcelKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${kafka.topics.parcels-in:parcels-in}") String parcelsInTopic,
                               @Value("${kafka.topics.notifications-out:notifications-out}") String notificationsOutTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.parcelsInTopic = (parcelsInTopic == null || parcelsInTopic.isBlank()) ? "parcels-in" : parcelsInTopic;
        this.notificationsOutTopic = (notificationsOutTopic == null || notificationsOutTopic.isBlank()) ? "notifications-out" : notificationsOutTopic;

        if ("parcels-in".equals(this.parcelsInTopic)) {
            log.warn("Usando o valor padrão kafka.topics.parcels-in='parcels-in'.");
        }
        if ("notifications-out".equals(this.notificationsOutTopic)) {
            log.warn("Usando o valor padrão kafka.topics.notifications-out='notifications-out'.");
        }
    }

    /**
     * Publica evento PARCEL_RECEIVED no tópico de entrada (parcelsInTopic).
     *
     */
    public void sendParcelReceivedEvent(Parcel parcel) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PARCEL_RECEIVED");
        event.put("parcelId", parcel.getId());
        event.put("recipientName", parcel.getRecipientName());
        event.put("apartment", parcel.getApartment());
        event.put("description", parcel.getDescription());
        event.put("status", parcel.getStatus() != null ? parcel.getStatus().name() : null);
        event.put("notified", parcel.isNotified());
        event.put("timestamp", parcel.getCreatedAt() != null ? parcel.getCreatedAt().toString() : Instant.now().toString());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(parcelsInTopic, String.valueOf(parcel.getId()), payload);
            log.info("Evento 'PARCEL_RECEIVED' enviado para encomenda id={} no tópico='{}'.", parcel.getId(), parcelsInTopic);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar o evento 'PARCEL_RECEIVED' para a encomenda id={}.", parcel.getId(), e);
        }
    }

    /**
     * Publica evento PARCEL_PICKED_UP no mesmo tópico de entrada para que consumidores processem.
     *
     */
    public void sendParcelPickedUpEvent(Parcel parcel, String pickedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PARCEL_PICKED_UP");
        event.put("parcelId", parcel.getId());
        event.put("recipientName", parcel.getRecipientName());
        event.put("apartment", parcel.getApartment());
        event.put("description", parcel.getDescription());
        event.put("pickedBy", pickedBy);
        event.put("status", parcel.getStatus() != null ? parcel.getStatus().name() : null);
        event.put("timestamp", parcel.getUpdatedAt() != null ? parcel.getUpdatedAt().toString() : Instant.now().toString());

        try {
            String payload = objectMapper.writeValueAsString(event);
            // Envia para o tópico de entrada (parcelsInTopic) para processamento por Notification/Audit
            kafkaTemplate.send(parcelsInTopic, String.valueOf(parcel.getId()), payload);
            log.info("Evento 'PARCEL_PICKED_UP' enviado para encomenda id={} no tópico='{}'. (retirada por: {})", parcel.getId(), parcelsInTopic, pickedBy);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar o evento 'PARCEL_PICKED_UP' para a encomenda id={}.", parcel.getId(), e);
        }
    }
}