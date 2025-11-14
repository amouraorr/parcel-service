package com.fiap.parcelservice.infrastructure.persistence;

import com.fiap.parcelservice.domain.model.Parcel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelRepositoryImplTest {

    @Mock
    private ParcelJpaRepository jpaRepository;

    @InjectMocks
    private ParcelRepositoryImpl repository;

    @Test
    @DisplayName("Deve salvar e retornar a encomenda")
    void save_shouldSaveParcel() {
        // Arrange
        Parcel parcel = mock(Parcel.class);
        when(jpaRepository.save(parcel)).thenReturn(parcel);

        // Act
        Parcel result = repository.save(parcel);

        // Assert
        assertSame(parcel, result);
        // Verify
        verify(jpaRepository, times(1)).save(parcel);
    }

    @Test
    @DisplayName("Deve retornar encomenda quando findById encontrar")
    void findById_shouldReturnParcelWhenPresent() {
        // Arrange
        Parcel parcel = mock(Parcel.class);
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(parcel));

        // Act
        Optional<Parcel> result = repository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertSame(parcel, result.get());
        // Verify
        verify(jpaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando findById não encontrar")
    void findById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(jpaRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Optional<Parcel> result = repository.findById(2L);

        // Assert
        assertFalse(result.isPresent());
        // Verify
        verify(jpaRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Deve retornar todas as encomendas")
    void findAll_shouldReturnAllParcels() {
        // Arrange
        Parcel p1 = mock(Parcel.class);
        Parcel p2 = mock(Parcel.class);
        List<Parcel> list = Arrays.asList(p1, p2);
        when(jpaRepository.findAll()).thenReturn(list);

        // Act
        List<Parcel> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(list, result);
        // Verify
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar encomendas filtradas por status")
    void findByStatus_shouldReturnParcelsWithGivenStatus() {
        // Arrange
        String status = "DELIVERED";
        Parcel p = mock(Parcel.class);
        List<Parcel> list = Collections.singletonList(p);
        when(jpaRepository.findByStatus(status)).thenReturn(list);

        // Act
        List<Parcel> result = repository.findByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(list, result);
        // Verify
        verify(jpaRepository, times(1)).findByStatus(status);
    }
}