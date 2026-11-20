package com.caldova.production.controller;

import com.caldova.production.service.OperationsService;
import com.caldova.production.service.ProductionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OperationsController {
    private final OperationsService operations;
    private final ProductionService production;

    public OperationsController(OperationsService operations, ProductionService production) {
        this.operations = operations; this.production = production;
    }

    @GetMapping("/")
    public String overview(Model model) {
        model.addAttribute("plantSummaries",operations.plantSummaries());
        model.addAttribute("totalLines",production.lineCount());
        model.addAttribute("upcomingMaintenance",operations.upcomingMaintenance());
        model.addAttribute("shiftSummaries",operations.shiftSummaries());
        model.addAttribute("demoDate",OperationsService.DEMO_DATE);
        model.addAttribute("snapshotTime","2026-11-19 14:20 UTC");
        return "overview";
    }

    @GetMapping("/maintenance")
    public String maintenance(@RequestParam(defaultValue="") String plant,
                              @RequestParam(defaultValue="") String status, Model model) {
        model.addAttribute("work",operations.maintenance(plant,status));
        model.addAttribute("plants",production.plants());
        model.addAttribute("plant",plant);
        model.addAttribute("status",status);
        return "maintenance";
    }

    @GetMapping("/rota")
    public String rota(@RequestParam(defaultValue="") String plant,
                       @RequestParam(defaultValue=OperationsService.DEMO_DATE) String date,
                       @RequestParam(defaultValue="") String shift, Model model) {
        model.addAttribute("assignments",operations.rota(plant,date,shift));
        model.addAttribute("plants",production.plants());
        model.addAttribute("plant",plant);
        model.addAttribute("date",date);
        model.addAttribute("shift",shift);
        return "rota";
    }
}
