package com.rprelevic.xm.recom.adtout.csv;

import com.rprelevic.xm.recom.adtout.csv.ex.DataSourceReadFailedException;
import com.rprelevic.xm.recom.api.DataSourceReader;
import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.api.model.Price;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class DataSourceReaderCsv implements DataSourceReader {

    private static final Logger LOGGER = Logger.getLogger(DataSourceReaderCsv.class.getName());

    @Override
    public List<Price> readPrices(IngestionRequest request) {

        try (var reader = Files.newBufferedReader(Path.of(request.source()))) {

            return reader.lines()
                    .skip(1) // Skip header
                    .map(line -> {
                        String[] parts = line.split(",");
                        return new Price(
                                Instant.ofEpochMilli(Long.parseLong(parts[0])), // dateTime
                                parts[1], // symbol
                                Double.parseDouble(parts[2]) // rate
                        );
                    })
                    .sorted((p1, p2) -> p2.dateTime().compareTo(p1.dateTime())) // Sort by dateTime descending
                    .toList();

        } catch (NoSuchFileException e) {
            LOGGER.severe("File not found: " + request.source());
            throw new DataSourceReadFailedException("File not found: " + request.source(), e); // Rethrow exception
        } catch (IOException e) {
            LOGGER.severe("Error reading data: " + request.source());
            throw new DataSourceReadFailedException("Error reading data: " + request.source(), e);
        } catch (Exception e) {
            LOGGER.severe("Error reading file: " + request.source());
            throw new DataSourceReadFailedException("Error reading file: " + request.source(), e);
        }
    }

}
