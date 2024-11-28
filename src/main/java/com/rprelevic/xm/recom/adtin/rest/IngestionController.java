package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import com.rprelevic.xm.recom.impl.SourceType;
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

    private final IngestionOrchestrator ingestionOrchestrator;
    private final ExecutorService executorService;

    public IngestionController(IngestionOrchestrator ingestionOrchestrator) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        executorService = Executors.newFixedThreadPool(10);;
    }

    @GetMapping("/start")
    public void startIngestion() {

        final List<String> fileNames = fetchFileNamesFromSourceLocation();
        for (String fileName : fileNames) {
            executorService.submit(() -> {
                IngestionRequest request = new IngestionRequest(fileName, fileName.split("_")[0], SourceType.CSV);
                ingestionOrchestrator.ingest(request);
            });
        }
    }

    private List<String> fetchFileNamesFromSourceLocation() {
        try {
            return Files.walk(Paths.get("src/main/resources/prices"))
                    .filter(Files::isRegularFile)
                    .map(path -> path.toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace(); // TODO: Properly log and return exception
            return List.of();
        }
    }
}
