package com.caldova.production.service;

import com.caldova.production.model.OperationsRecords;
import com.caldova.production.repository.OperationsRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OperationsService {
    public static final String DEMO_DATE = "2026-11-19";
    private final OperationsRepository repository;
    private final ProductionService production;

    public OperationsService(OperationsRepository repository, ProductionService production) {
        this.repository = repository; this.production = production;
    }

    public List<OperationsRecords.PlantSummary> plantSummaries() { return repository.plantSummaries(); }
    public List<OperationsRecords.Maintenance> upcomingMaintenance() {
        return repository.maintenance("","").stream()
                .filter(work -> !"COMPLETED".equals(work.status)).collect(Collectors.toList());
    }
    public List<OperationsRecords.ShiftSummary> shiftSummaries() {
        return repository.shiftSummaries(LocalDate.parse(DEMO_DATE));
    }
    public List<OperationsRecords.Maintenance> maintenance(String plant, String status) {
        production.validatePlant(plant);
        if (!status.isEmpty() && !Arrays.asList("SCHEDULED","IN_PROGRESS","COMPLETED").contains(status)) {
            throw new IllegalArgumentException("Select a listed maintenance status.");
        }
        return repository.maintenance(plant,status);
    }
    public List<OperationsRecords.RotaEntry> rota(String plant, String date, String shift) {
        production.validatePlant(plant);
        if (!shift.isEmpty() && !Arrays.asList("DAY","NIGHT").contains(shift)) {
            throw new IllegalArgumentException("Select Day or Night.");
        }
        LocalDate day;
        try { day = LocalDate.parse(date); }
        catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Use a valid YYYY-MM-DD rota date.",exception);
        }
        return repository.rota(plant,day,shift);
    }
}
