package com.fiap.parcelservice;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class StartupLoggerTest {

    @Test
    @DisplayName("Deve registrar 'default' quando não houver perfis ativos")
    public void onReady_logsDefaultWhenNoProfiles() {
        // Arrange
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{});
        StartupLogger startupLogger = new StartupLogger(env);

        Logger logger = (Logger) LoggerFactory.getLogger(StartupLogger.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        // Act
        startupLogger.onReady();

        // Assert
        boolean found = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("ParcelService started. activeProfiles=[] (default)."));
        assertTrue(found, "Deveria registrar mensagem indicando 'default' quando não houver perfis ativos");

        // Verify
        verify(env, times(1)).getActiveProfiles();
    }

    @Test
    @DisplayName("Deve registrar 'ok' quando houver perfis ativos")
    public void onReady_logsOkWhenProfilesPresent() {
        // Arrange
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev", "aws"});
        StartupLogger startupLogger = new StartupLogger(env);

        Logger logger = (Logger) LoggerFactory.getLogger(StartupLogger.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        // Act
        startupLogger.onReady();

        // Assert
        boolean found = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("ParcelService started. activeProfiles=[dev, aws] (ok)."));
        assertTrue(found, "Deveria registrar mensagem indicando 'ok' quando houver perfis ativos");

        // Verify
        verify(env, times(1)).getActiveProfiles();
    }
}