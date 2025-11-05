package com.fiap.parcelservice.infrastructure.messaging;

import com.fiap.parcelservice.domain.model.Parcel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Producer Kafka para eventos relacionados a encomendas.
 *
 * Mantém também eventType e parcelId para auditoria.
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
                               @Value("${KAFKA_TOPICS_PARCELS_IN:parcels.received}") String parcelsInTopic,
                               @Value("${KAFKA_TOPICS_NOTIFICATIONS_OUT:notifications.sent}") String notificationsOutTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.parcelsInTopic = (parcelsInTopic == null || parcelsInTopic.isBlank()) ? "parcels.received" : parcelsInTopic;
        this.notificationsOutTopic = (notificationsOutTopic == null || notificationsOutTopic.isBlank()) ? "notifications.sent" : notificationsOutTopic;

        if ("parcels.received".equals(this.parcelsInTopic)) {
            log.warn("Usando valor padrão KAFKA_TOPICS_PARCELS_IN='parcels.received'");
        }
        if ("notifications.sent".equals(this.notificationsOutTopic)) {
            log.warn("Usando valor padrão KAFKA_TOPICS_NOTIFICATIONS_OUT='notifications.sent'.");
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
        event.put("residentName", parcel.getResidentName());
        event.put("apartment", parcel.getApartment());
        event.put("description", parcel.getDescription());
        event.put("contact", parcel.getContact());
        String channel = parcel.getChannel();
        event.put("channel", (channel == null || channel.isBlank()) ? "PUSH" : channel.toUpperCase());
        OffsetDateTime receivedAt = parcel.getCreatedAt() != null ? parcel.getCreatedAt() : OffsetDateTime.now();
        event.put("receivedAt", receivedAt.toString());

        event.put("status", parcel.getStatus() != null ? parcel.getStatus().name() : null);
        event.put("notified", parcel.isNotified());

        try {
            String payload = objectMapper.writeValueAsString(event);
            var future = kafkaTemplate.send(parcelsInTopic, String.valueOf(parcel.getId()), payload);
            final String preview = preview(payload);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    if (result != null && result.getRecordMetadata() != null) {
                        var meta = result.getRecordMetadata();
                        log.info("Evento PARCEL_RECEIVED enviado id={} tópico={} partição={} offset={} previewPayload={}",
                                parcel.getId(), meta.topic(), meta.partition(), meta.offset(), preview);
                    } else {
                        log.info("Evento PARCEL_RECEIVED enviado id={} mas sem metadata disponível previewPayload={}", parcel.getId(), preview);
                    }
                } else {
                    log.error("Falha ao enviar evento PARCEL_RECEIVED id={} previewPayload={}", parcel.getId(), preview, ex);
                }
            });

            log.debug("Envio disparado para PARCEL_RECEIVED id={} previewPayload={}", parcel.getId(), preview);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar evento PARCEL_RECEIVED para id={}", parcel.getId(), e);
        }
    }

    /**
     * Publica evento PARCEL_PICKED_UP
     */
    public void sendParcelPickedUpEvent(Parcel parcel, String pickedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PARCEL_PICKED_UP");
        event.put("parcelId", parcel.getId());

        event.put("residentName", parcel.getResidentName());
        event.put("apartment", parcel.getApartment());
        event.put("description", parcel.getDescription());
        event.put("contact", parcel.getContact());
        String channel = parcel.getChannel();
        event.put("channel", (channel == null || channel.isBlank()) ? "PUSH" : channel.toUpperCase());
        event.put("pickedBy", pickedBy);
        OffsetDateTime updatedAt = parcel.getUpdatedAt() != null ? parcel.getUpdatedAt() : OffsetDateTime.now();
        event.put("receivedAt", updatedAt.toString());

        event.put("status", parcel.getStatus() != null ? parcel.getStatus().name() : null);

        try {
            String payload = objectMapper.writeValueAsString(event);
            var future = kafkaTemplate.send(parcelsInTopic, String.valueOf(parcel.getId()), payload);
            final String preview = preview(payload);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    if (result != null && result.getRecordMetadata() != null) {
                        var meta = result.getRecordMetadata();
                        log.info("Evento PARCEL_PICKED_UP enviado id={} tópico={} partição={} offset={} previewPayload={}",
                                parcel.getId(), meta.topic(), meta.partition(), meta.offset(), preview);
                    } else {
                        log.info("Evento PARCEL_PICKED_UP enviado id={} mas sem metadata disponível previewPayload={}", parcel.getId(), preview);
                    }
                } else {
                    log.error("Falha ao enviar evento PARCEL_PICKED_UP id={} previewPayload={}", parcel.getId(), preview, ex);
                }
            });

            log.debug("Envio disparado para PARCEL_PICKED_UP id={} previewPayload={}", parcel.getId(), preview);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar evento PARCEL_PICKED_UP para id={}", parcel.getId(), e);
        }
    }

    private String preview(String s) {
        if (s == null) return "";
        int max = 800;
        return s.length() <= max ? s : s.substring(0, max) + "...[truncado]";
    }
}