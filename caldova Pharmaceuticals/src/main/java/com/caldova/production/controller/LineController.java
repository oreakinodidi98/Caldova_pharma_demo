package com.caldova.production.controller;

import com.caldova.production.model.Records;
import com.caldova.production.service.ProductionService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LineController {
    private final ProductionService service;

    public LineController(ProductionService service) { this.service = service; }

    @GetMapping("/lines")
    public String overview(@RequestParam(defaultValue = "") String plant,
                           @RequestParam(defaultValue = "") String status, Model model) {
        service.validateFilters(plant, status);
        List<Records.Line> lines = service.lines(plant, status);
        model.addAttribute("plants", service.plants());
        model.addAttribute("lines", lines);
        model.addAttribute("totalLines", service.lineCount());
        model.addAttribute("plant", plant);
        model.addAttribute("status", status);
        model.addAttribute("snapshotTime", lines.isEmpty() ? "No matching recorded samples" : lines.get(0).snapshotTime);
        return "lines";
    }

    @GetMapping("/lines/{lineId}")
    public String detail(@PathVariable long lineId,
                         @RequestParam(defaultValue = "") String plant,
                         @RequestParam(defaultValue = "") String status, Model model) {
        service.validateFilters(plant, status);
        model.addAttribute("detail", service.detail(lineId));
        model.addAttribute("plant", plant);
        model.addAttribute("status", status);
        return "line-detail";
    }
}
