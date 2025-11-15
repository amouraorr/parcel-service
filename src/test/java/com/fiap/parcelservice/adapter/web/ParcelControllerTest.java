package com.fiap.parcelservice.adapter.web;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.application.service.ParcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelControllerTest {

    @Mock
    private ParcelService parcelService;

    private ParcelController controller;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    @BeforeEach
    void setUp() {
        controller = new ParcelController(parcelService);
    }

    @Test
    @DisplayName("Registrar nova encomenda - retorna 201 Created com Location")
    void receiveParcel_returnsCreatedWithLocation() {
        // Arrange
        ParcelRequest request = mock(ParcelRequest.class);
        ParcelResponse created = mock(ParcelResponse.class);
        when(created.getId()).thenReturn(1L);
        when(parcelService.receiveParcel(any(ParcelRequest.class))).thenReturn(created);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("porteiro1");

        // Act
        ResponseEntity<ParcelResponse> response = controller.receiveParcel(request, auth);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("/api/parcels/1", response.getHeaders().getLocation().toString());
        assertSame(created, response.getBody());

        // Verify
        verify(parcelService, times(1)).receiveParcel(any(ParcelRequest.class));
    }

    @Test
    @DisplayName("Registrar nova encomenda - retorna 500 quando serviço retorna null")
    void receiveParcel_returnsInternalServerErrorWhenServiceReturnsNull() {
        // Arrange
        ParcelRequest request = mock(ParcelRequest.class);
        when(parcelService.receiveParcel(any(ParcelRequest.class))).thenReturn(null);
        Authentication auth = mock(Authentication.class);

        // Act
        ResponseEntity<ParcelResponse> response = controller.receiveParcel(request, auth);

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertNull(response.getBody());

        // Verify
        verify(parcelService, times(1)).receiveParcel(any(ParcelRequest.class));
    }

    @Test
    @DisplayName("Consultar encomenda por ID - retorna 200 quando encontrado")
    void getParcel_returnsOkWhenFound() {
        // Arrange
        ParcelResponse resp = mock(ParcelResponse.class);
        when(parcelService.getParcel(10L)).thenReturn(resp);

        // Act
        ResponseEntity<ParcelResponse> response = controller.getParcel(10L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());

        // Verify
        verify(parcelService, times(1)).getParcel(10L);
    }

    @Test
    @DisplayName("Consultar encomenda por ID - retorna 404 quando não encontrado")
    void getParcel_returnsNotFoundWhenMissing() {
        // Arrange
        when(parcelService.getParcel(99L)).thenReturn(null);

        // Act
        ResponseEntity<ParcelResponse> response = controller.getParcel(99L);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());

        // Verify
        verify(parcelService, times(1)).getParcel(99L);
    }

    @Test
    @DisplayName("Listar todas as encomendas - retorna 200 com a lista")
    void listParcels_returnsListOk() {
        // Arrange
        ParcelResponse p1 = mock(ParcelResponse.class);
        ParcelResponse p2 = mock(ParcelResponse.class);
        List<ParcelResponse> list = Arrays.asList(p1, p2);
        when(parcelService.listParcels()).thenReturn(list);

        // Act
        ResponseEntity<List<ParcelResponse>> response = controller.listParcels();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertSame(list, response.getBody());

        // Verify
        verify(parcelService, times(1)).listParcels();
    }

    @Test
    @DisplayName("Listar encomendas - retorna lista vazia quando não há registros")
    void listParcels_returnsEmptyListWhenNoParcels() {
        // Arrange
        when(parcelService.listParcels()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ParcelResponse>> response = controller.listParcels();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());

        // Verify
        verify(parcelService, times(1)).listParcels();
    }

    @Test
    @DisplayName("Marcar encomenda como retirada - retorna 200 e passa username do porteiro")
    void markPickedUp_returnsOkAndPassesUsername() {
        // Arrange
        ParcelResponse resp = mock(ParcelResponse.class);
        when(parcelService.markPickedUp(eq(5L), eq("porterUser"))).thenReturn(resp);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("porterUser");

        // Act
        ResponseEntity<ParcelResponse> response = controller.markPickedUp(5L, auth);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());

        // Verify
        verify(parcelService, times(1)).markPickedUp(5L, "porterUser");
    }

    @Test
    @DisplayName("Marcar encomenda como retirada - retorna 404 quando serviço lança IllegalArgumentException")
    void markPickedUp_returnsNotFoundOnIllegalArgument() {
        // Arrange
        doThrow(new IllegalArgumentException("not found")).when(parcelService).markPickedUp(eq(7L), any());
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("porterX");

        // Act
        ResponseEntity<ParcelResponse> response = controller.markPickedUp(7L, auth);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());

        // Verify
        verify(parcelService, times(1)).markPickedUp(7L, "porterX");
    }

    @Test
    @DisplayName("Marcar encomenda como retirada - retorna 500 quando serviço lança exceção inesperada")
    void markPickedUp_returnsInternalServerErrorOnException() {
        // Arrange
        doThrow(new RuntimeException("boom")).when(parcelService).markPickedUp(eq(8L), any());
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("porterY");

        // Act
        ResponseEntity<ParcelResponse> response = controller.markPickedUp(8L, auth);

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertNull(response.getBody());

        // Verify
        verify(parcelService, times(1)).markPickedUp(8L, "porterY");
    }
}