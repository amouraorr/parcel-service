package com.fiap.parcelservice.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.parcelservice.domain.model.Parcel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ParcelKafkaProducer.
 */
@ExtendWith(MockitoExtension.class)
public class ParcelKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private ParcelKafkaProducer createProducer() {
        return new ParcelKafkaProducer(kafkaTemplate, objectMapper, "parcels.received", "notifications.sent");
    }

    @Test
    @DisplayName("Enviar evento PARCEL_RECEIVED com canal padrão PUSH quando channel for nulo")
    public void shouldSendParcelReceivedEvent_whenChannelIsNull() throws Exception {
        // Arrange
        Parcel parcel = mock(Parcel.class);
        when(parcel.getId()).thenReturn(123L);
        when(parcel.getResidentName()).thenReturn("João Silva");
        when(parcel.getApartment()).thenReturn("A101");
        when(parcel.getDescription()).thenReturn("Caixa grande");
        when(parcel.getContact()).thenReturn("99999-0000");
        when(parcel.getChannel()).thenReturn(null);
        when(parcel.getCreatedAt()).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(arg);
        });

        when(kafkaTemplate.send(eq("parcels.received"), eq(String.valueOf(123L)), any(String.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        ParcelKafkaProducer producer = createProducer();

        // Act
        producer.sendParcelReceivedEvent(parcel);

        // Assert
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        // Verify
        verify(kafkaTemplate, times(1)).send(eq("parcels.received"), eq("123"), payloadCaptor.capture());
        String capturedPayload = payloadCaptor.getValue();

        // Assert
        assertTrue(capturedPayload.contains("\"channel\":\"PUSH\""));
        assertTrue(capturedPayload.contains("\"eventType\":\"PARCEL_RECEIVED\""));
    }

    @Test
    @DisplayName("Enviar evento PARCEL_PICKED_UP incluindo pickedBy e status")
    public void shouldSendParcelPickedUpEvent_whenParcelIsPickedUp() throws Exception {
        // Arrange
        Parcel parcel = mock(Parcel.class);
        when(parcel.getId()).thenReturn(555L);
        when(parcel.getResidentName()).thenReturn("Maria Oliveira");
        when(parcel.getApartment()).thenReturn("B202");
        when(parcel.getDescription()).thenReturn("Envelope");
        when(parcel.getContact()).thenReturn("98888-1111");
        when(parcel.getChannel()).thenReturn("email");
        when(parcel.getUpdatedAt()).thenReturn(null);
        when(parcel.getStatus()).thenReturn(null);

        when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(arg);
        });

        when(kafkaTemplate.send(eq("parcels.received"), eq(String.valueOf(555L)), any(String.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        ParcelKafkaProducer producer = createProducer();

        // Act
        producer.sendParcelPickedUpEvent(parcel, "CondoAdmin");

        // Assert
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        // Verify
        verify(kafkaTemplate, times(1)).send(eq("parcels.received"), eq("555"), payloadCaptor.capture());
        String capturedPayload = payloadCaptor.getValue();

        // Assert
        assertTrue(capturedPayload.contains("\"eventType\":\"PARCEL_PICKED_UP\""));
        assertTrue(capturedPayload.contains("\"pickedBy\":\"CondoAdmin\""));
        assertTrue(capturedPayload.contains("\"channel\":\"EMAIL\""));
    }

    @Test
    @DisplayName("Não enviar evento quando ocorrer erro de serialização (JsonProcessingException)")
    public void shouldNotSend_whenSerializationFails() throws Exception {
        // Arrange
        Parcel parcel = mock(Parcel.class);
        when(parcel.getId()).thenReturn(777L);
        when(parcel.getResidentName()).thenReturn("Teste Erro");
        when(parcel.getApartment()).thenReturn("C303");
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});

        ParcelKafkaProducer producer = createProducer();

        // Act
        producer.sendParcelReceivedEvent(parcel);

        // Assert & Verify
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
}