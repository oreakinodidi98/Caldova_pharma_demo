package com.caldova.production.service;

import com.caldova.production.model.LineStatus;
import com.caldova.production.model.Records;
import com.caldova.production.repository.ProductionRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionService {
    private final ProductionRepository repository;

    public ProductionService(ProductionRepository repository) { this.repository = repository; }

    public void validateFilters(String plant, String status) {
        validatePlant(plant);
        if (!status.isEmpty()) {
            try { LineStatus.valueOf(status); }
            catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Select Running, Attention, or Stopped.", ex);
            }
        }
    }

    public void validatePlant(String plant) {
        if (!plant.isEmpty() && !Arrays.asList("NB", "WH", "EM", "SW").contains(plant)) {
            throw new IllegalArgumentException("Select a plant from the list.");
        }
    }

    public List<Records.Plant> plants() { return repository.plants(); }
    public int lineCount() { return repository.lineCount(); }
    public List<Records.Line> lines(String plant, String status) {
        validateFilters(plant, status);
        return repository.lines(plant, status);
    }
    @Transactional(readOnly = true)
    public Records.Detail detail(long id) {
        if (id <= 0) { throw new IllegalArgumentException("The line ID must be a positive number."); }
        Records.Line line = repository.line(id).orElseThrow(LineNotFoundException::new);
        return new Records.Detail(line, repository.alarms(id), repository.signals(id),
                repository.checks(id), repository.manuals(id));
    }
}
