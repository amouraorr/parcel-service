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

@Component
public class ParcelKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ParcelKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String parcelsInTopic;
    private final String notificationsOutTopic;

    public ParcelKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${kafka.topics.parcels-in}") String parcelsInTopic,
                               @Value("${kafka.topics.notifications-out}") String notificationsOutTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.parcelsInTopic = parcelsInTopic;
        this.notificationsOutTopic = notificationsOutTopic;
    }

    public void sendParcelReceivedEvent(Parcel parcel) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PARCEL_RECEIVED");
        event.put("parcelId", parcel.getId());
        event.put("recipientName", parcel.getRecipientName());
        event.put("apartment", parcel.getApartment());
        event.put("description", parcel.getDescription());
        event.put("timestamp", parcel.getCreatedAt().toString());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(parcelsInTopic, String.valueOf(parcel.getId()), payload);
            log.info("Sent PARCEL_RECEIVED event for id={} to topic={}", parcel.getId(), parcelsInTopic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize parcel received event", e);
        }
    }

    public void sendParcelPickedUpEvent(Parcel parcel, String pickedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PARCEL_PICKED_UP");
        event.put("parcelId", parcel.getId());
        event.put("recipientName", parcel.getRecipientName());
        event.put("apartment", parcel.getApartment());
        event.put("description", parcel.getDescription());
        event.put("pickedBy", pickedBy);

        event.put("timestamp", Instant.now().toString());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(notificationsOutTopic, String.valueOf(parcel.getId()), payload);
            log.info("Sent PARCEL_PICKED_UP event for id={} to topic={}", parcel.getId(), notificationsOutTopic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize parcel picked-up event", e);
        }
    }
}
