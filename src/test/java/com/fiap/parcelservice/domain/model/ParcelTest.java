package com.fiap.parcelservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParcelTest {

    @Test
    @DisplayName("Deve criar Parcel com valores do construtor e retornar via getters")
    void constructorAndGetters_shouldReturnValues() {
        // Arrange
        Parcel parcel = new Parcel("João Silva", "A101", "Caixa grande", ParcelStatus.RECEIVED);

        // Act & Assert
        assertEquals("João Silva", parcel.getResidentName());
        assertEquals("A101", parcel.getApartment());
        assertEquals("Caixa grande", parcel.getDescription());
        assertEquals(ParcelStatus.RECEIVED, parcel.getStatus());
        assertNotNull(parcel.getCreatedAt());
        assertNotNull(parcel.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve atualizar createdAt e updatedAt quando prePersist for chamado e torná-los iguais")
    void prePersist_shouldSetCreatedAtAndUpdatedAtToNowAndEqual() {
        // Arrange
        Parcel parcel = new Parcel("Maria", "B202", "Envelope", ParcelStatus.RECEIVED);
        OffsetDateTime before = parcel.getCreatedAt();

        // Act
        parcel.prePersist();

        // Assert
        OffsetDateTime created = parcel.getCreatedAt();
        OffsetDateTime updated = parcel.getUpdatedAt();

        assertNotNull(created);
        assertNotNull(updated);
        assertEquals(created, updated);
        assertFalse(created.isBefore(before));
    }

    @Test
    @DisplayName("Deve atualizar updatedAt quando preUpdate for chamado para um instante posterior")
    void preUpdate_shouldChangeUpdatedAt() throws InterruptedException {
        // Arrange
        Parcel parcel = new Parcel("Carlos", "C303", "Pacote", ParcelStatus.RECEIVED);
        OffsetDateTime originalUpdated = parcel.getUpdatedAt();

        Thread.sleep(5);

        // Act
        parcel.preUpdate();

        // Assert
        OffsetDateTime newUpdated = parcel.getUpdatedAt();
        assertNotNull(newUpdated);
        assertTrue(newUpdated.isAfter(originalUpdated));
    }

    @Test
    @DisplayName("Deve setar e obter contact, channel e id corretamente")
    void settersAndGetters_shouldSetAndGetContactChannelAndId() {
        // Arrange
        Parcel parcel = new Parcel();
        // Act & Arrange
        parcel.setId(123L);
        parcel.setContact("maria@example.com");
        parcel.setChannel("WHATS");

        // Assert
        assertEquals(123L, parcel.getId());
        assertEquals("maria@example.com", parcel.getContact());
        assertEquals("WHATS", parcel.getChannel());
    }
}