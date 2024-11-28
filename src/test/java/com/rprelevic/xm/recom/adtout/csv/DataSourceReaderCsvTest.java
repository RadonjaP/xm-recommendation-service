package com.rprelevic.xm.recom.adtout.csv;

import com.rprelevic.xm.recom.adtout.csv.ex.DataSourceReadFailedException;
import com.rprelevic.xm.recom.api.model.Price;
import com.rprelevic.xm.recom.impl.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceReaderCsvTest {

    private DataSourceReaderCsv dataSourceReaderCsv;
    private CsvIngestionRequest request;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dataSourceReaderCsv = new DataSourceReaderCsv();
        request = new CsvIngestionRequest(tempDir.resolve("ETH_values.csv").toString(), "ETH", SourceType.CSV);
    }

    @Test
    void givenFileExists_whenReadPrices_thenAllObjectsCreated() throws IOException {
        Path filePath = tempDir.resolve("ETH_values.csv");
        Files.writeString(filePath, "dateTime,symbol,rate\n1641009600000,ETH,46813.21\n1641020400000,ETH,46979.61");

        List<Price> prices = dataSourceReaderCsv.readPrices(request);

        assertEquals(2, prices.size());
        assertEquals(Instant.ofEpochMilli(1641020400000L), prices.get(0).dateTime());
        assertEquals("ETH", prices.get(0).symbol());
        assertEquals(46979.61, prices.get(0).rate());
        assertEquals(Instant.ofEpochMilli(1641009600000L), prices.get(1).dateTime());
        assertEquals("ETH", prices.get(1).symbol());
        assertEquals(46813.21, prices.get(1).rate());
    }

    @Test
    void givenFileDoesNotExist_whenReadPrices_thenThrowDataSourceReadFailedException() {
        request = new CsvIngestionRequest(tempDir.resolve("non_existent.csv").toString(), "ETH", SourceType.CSV);

        DataSourceReadFailedException exception = assertThrows(DataSourceReadFailedException.class, () -> dataSourceReaderCsv.readPrices(request));
        assertEquals("File not found: " + request.source(), exception.getMessage());
    }

    @Test
    void givenRowInFile_whenHasInvalidTimestamp_thenThrowDataSourceReadFailedException() throws IOException {
        Path filePath = tempDir.resolve("ETH_values.csv");
        Files.writeString(filePath, "dateTime,symbol,rate\ninvalid_timestamp,ETH,46813.21");

        DataSourceReadFailedException exception = assertThrows(DataSourceReadFailedException.class, () -> dataSourceReaderCsv.readPrices(request));
        assertEquals("Error reading file: " + request.source(), exception.getMessage());
    }

    @Test
    void givenRowInFile_whenHasInvalidPrice_thenThrowDataSourceReadFailedException() throws IOException {
        Path filePath = tempDir.resolve("ETH_values.csv");
        Files.writeString(filePath, "dateTime,symbol,rate\n1641009600000,ETH,invalid_price");

        DataSourceReadFailedException exception = assertThrows(DataSourceReadFailedException.class, () -> dataSourceReaderCsv.readPrices(request));
        assertEquals("Error reading file: " + request.source(), exception.getMessage());
    }

    @Test
    void givenFileIsEmpty_whenReadPrices_thenReturnEmptyList() throws IOException {
        Path filePath = tempDir.resolve("ETH_values.csv");
        Files.writeString(filePath, "dateTime,symbol,rate\n");

        List<Price> prices = dataSourceReaderCsv.readPrices(request);

        assertTrue(prices.isEmpty());
    }
}