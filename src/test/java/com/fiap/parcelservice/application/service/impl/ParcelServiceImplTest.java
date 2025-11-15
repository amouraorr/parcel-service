package com.fiap.parcelservice.application.service.impl;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.application.mapper.ParcelMapper;
import com.fiap.parcelservice.domain.model.Parcel;
import com.fiap.parcelservice.domain.model.ParcelStatus;
import com.fiap.parcelservice.domain.repository.ParcelRepository;
import com.fiap.parcelservice.infrastructure.messaging.ParcelKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapper parcelMapper;

    @Mock
    private ParcelKafkaProducer kafkaProducer;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    @Captor
    private ArgumentCaptor<Parcel> parcelCaptor;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Receber encomenda - configura status RECEIVED, salva e publica evento")
    void receiveParcel_setsStatusAndSendsEvent() {
        // Arrange
        ParcelRequest request = new ParcelRequest();
        Parcel toSave = new Parcel();
        Parcel saved = new Parcel();
        saved.setId(100L);
        ParcelResponse response = new ParcelResponse();

        when(parcelMapper.toEntity(request)).thenReturn(toSave);
        when(parcelRepository.save(any(Parcel.class))).thenReturn(saved);
        when(parcelMapper.toResponse(saved)).thenReturn(response);

        // Act
        ParcelResponse result = parcelService.receiveParcel(request);

        // Assert
        assertSame(response, result);

        // Verify
        verify(parcelMapper, times(1)).toEntity(request);
        verify(parcelRepository, times(1)).save(parcelCaptor.capture());
        Parcel captured = parcelCaptor.getValue();
        assertNotNull(captured);
        assertEquals(ParcelStatus.RECEIVED, captured.getStatus());
        verify(kafkaProducer, times(1)).sendParcelReceivedEvent(saved);
        verify(parcelMapper, times(1)).toResponse(saved);
    }

    @Test
    @DisplayName("Consultar encomenda - retorna response quando encontrado")
    void getParcel_returnsResponseWhenFound() {
        // Arrange
        Parcel parcel = new Parcel();
        ParcelResponse response = new ParcelResponse();
        when(parcelRepository.findById(10L)).thenReturn(Optional.of(parcel));
        when(parcelMapper.toResponse(parcel)).thenReturn(response);

        // Act
        ParcelResponse result = parcelService.getParcel(10L);

        // Assert
        assertSame(response, result);

        // Verify
        verify(parcelRepository, times(1)).findById(10L);
        verify(parcelMapper, times(1)).toResponse(parcel);
    }

    @Test
    @DisplayName("Consultar encomenda - retorna null quando não encontrado")
    void getParcel_returnsNullWhenNotFound() {
        // Arrange
        when(parcelRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ParcelResponse result = parcelService.getParcel(99L);

        // Assert
        assertNull(result);

        // Verify
        verify(parcelRepository, times(1)).findById(99L);
        verifyNoInteractions(parcelMapper);
    }

    @Test
    @DisplayName("Listar encomendas - retorna lista de responses")
    void listParcels_returnsMappedList() {
        // Arrange
        Parcel p1 = new Parcel();
        Parcel p2 = new Parcel();
        ParcelResponse r1 = new ParcelResponse();
        ParcelResponse r2 = new ParcelResponse();

        when(parcelRepository.findAll()).thenReturn(Arrays.asList(p1, p2));
        when(parcelMapper.toResponse(p1)).thenReturn(r1);
        when(parcelMapper.toResponse(p2)).thenReturn(r2);

        // Act
        List<ParcelResponse> result = parcelService.listParcels();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));

        // Verify
        verify(parcelRepository, times(1)).findAll();
        verify(parcelMapper, times(1)).toResponse(p1);
        verify(parcelMapper, times(1)).toResponse(p2);
    }

    @Test
    @DisplayName("Marcar retirada - altera status para PICKED_UP, salva e publica evento")
    void markPickedUp_setsStatusSavesAndSendsEvent() {
        // Arrange
        Long id = 5L;
        String pickedBy = "porter1";
        Parcel existing = new Parcel();
        existing.setId(id);
        Parcel saved = new Parcel();
        saved.setId(id);
        ParcelResponse response = new ParcelResponse();

        when(parcelRepository.findById(id)).thenReturn(Optional.of(existing));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(saved);
        when(parcelMapper.toResponse(saved)).thenReturn(response);

        // Act
        ParcelResponse result = parcelService.markPickedUp(id, pickedBy);

        // Assert
        assertSame(response, result);

        // Verify
        verify(parcelRepository, times(1)).findById(id);
        verify(parcelRepository, times(1)).save(parcelCaptor.capture());
        Parcel captured = parcelCaptor.getValue();
        assertEquals(ParcelStatus.PICKED_UP, captured.getStatus());
        verify(kafkaProducer, times(1)).sendParcelPickedUpEvent(saved, pickedBy);
        verify(parcelMapper, times(1)).toResponse(saved);
    }

    @Test
    @DisplayName("Marcar retirada - lança IllegalArgumentException quando não encontrado")
    void markPickedUp_throwsWhenNotFound() {
        // Arrange
        Long id = 77L;
        when(parcelRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> parcelService.markPickedUp(id, "someone"));

        // Verify
        verify(parcelRepository, times(1)).findById(id);
        verify(parcelRepository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }
}