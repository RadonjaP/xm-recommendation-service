package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import com.rprelevic.xm.recom.impl.SourceType;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionController.class);

    private final IngestionOrchestrator ingestionOrchestrator;
    private final ExecutorService executorService;

    @Value("${source.location}")
    private String sourceLocation;

    public IngestionController(IngestionOrchestrator ingestionOrchestrator) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.executorService = Executors.newFixedThreadPool(10);;
    }


    @Operation(summary = "Start ingestion process", description = "Starts the ingestion process for all files in the source location")
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

        try (var paths = Files.walk(Paths.get(sourceLocation))) {
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
