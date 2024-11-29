package com.rprelevic.xm.recom.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstantUtilsTest {

    @Test
    void whenInstantiateInstantUtils_thenThrowUnsupportedOperationException() throws Exception {
        var constructor = InstantUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                constructor.newInstance();
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    void givenNegativeTimestamp_whenToInstant_thenCorrectInstant() {
        long timestamp = -1633072800000L; // Before epoch
        Instant expected = Instant.ofEpochMilli(timestamp);
        assertEquals(expected, InstantUtils.toInstant(timestamp));
    }

    @Test
    void givenLocalDateTimeWithUTC_whenToInstant_thenCorrectInstant() {
        LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 1, 0, 0);
        Instant expected = localDateTime.toInstant(ZoneOffset.UTC);
        assertEquals(expected, InstantUtils.toInstant(localDateTime.atOffset(ZoneOffset.ofHours(2)).toLocalDateTime()));
    }

    @Test
    void givenNullLocalDateTime_whenToInstant_thenThrowNullPointerException() {
        LocalDateTime localDateTime = null;
        assertThrows(NullPointerException.class, () -> InstantUtils.toInstant(localDateTime));
    }

    @Test
    void givenNullTimestamp_whenToInstant_thenThrowNullPointerException() {
        Timestamp timestamp = null;
        assertThrows(NullPointerException.class, () -> InstantUtils.toInstant(timestamp));
    }

    @Test
    void givenInstantWithUTCZone_whenToLocalDate_thenCorrectLocalDateTimeTime() {
        Instant instant = Instant.parse("2021-10-01T00:00:00Z");
        LocalDateTime expected = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        assertEquals(expected, InstantUtils.toLocalDateTime(instant.atOffset(ZoneOffset.UTC).toInstant()));
    }

    @Test
    void givenInstant_whenToLocalDate_thenCorrectLocalDate() {
        Instant instant = Instant.parse("2021-10-01T00:00:00Z");
        LocalDate expected = LocalDate.of(2021, 10, 1);
        assertEquals(expected, InstantUtils.toLocalDate(instant));
    }

}