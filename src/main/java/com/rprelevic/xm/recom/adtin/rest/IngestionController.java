package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import com.rprelevic.xm.recom.impl.SourceType;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController("/api/v1/ingestion")
public class IngestionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionController.class);

    private final IngestionOrchestrator ingestionOrchestrator;
    private final ExecutorService executorService;

    public IngestionController(IngestionOrchestrator ingestionOrchestrator) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.executorService = Executors.newFixedThreadPool(10);;
    }

    @GetMapping("/start")
    public void startIngestion() {

        final var fileNameAndPathPairs = fetchFilePathsFromSourceLocation();
        for (Pair<String, String> pair : fileNameAndPathPairs) {
            executorService.submit(() -> {
                final String symbol = pair.getLeft().split("_")[0]; // Extract symbol from file path
                ingestionOrchestrator.ingest(new IngestionRequest(pair.getRight(), symbol, SourceType.CSV));
            });
        }
    }

    private List<Pair<String, String>> fetchFilePathsFromSourceLocation() {

        try (var paths = Files.walk(Paths.get("src/main/resources/rates"))) { // TODO: Move to application.properties
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> Pair.of(path.getFileName().toString(), path.toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error fetching file paths from source location", e);
            return List.of();
        }
    }
}
