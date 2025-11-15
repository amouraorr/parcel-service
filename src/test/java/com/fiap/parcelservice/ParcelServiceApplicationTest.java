package com.fiap.parcelservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ParcelServiceApplicationTest {

    @Test
    @DisplayName("Deve chamar SpringApplication.run com args vazios ao iniciar a aplicação")
    void mainCallsSpringApplicationRunWithEmptyArgs() {
        // Arrange
        String[] args = new String[0];
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(ParcelServiceApplication.class, args)).thenReturn(mockContext);

            // Act
            ParcelServiceApplication.main(args);

            // Assert
            mocked.verify(() -> SpringApplication.run(ParcelServiceApplication.class, args));
        }
    }

    @Test
    @DisplayName("Deve chamar SpringApplication.run com argumentos recebidos ao iniciar a aplicação")
    void mainCallsSpringApplicationRunWithProvidedArgs() {
        // Arrange
        String[] args = new String[] { "--spring.profiles.active=test", "--server.port=0" };
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(ParcelServiceApplication.class, args)).thenReturn(mockContext);

            // Act
            ParcelServiceApplication.main(args);

            // Assert
            mocked.verify(() -> SpringApplication.run(ParcelServiceApplication.class, args));
        }
    }
}