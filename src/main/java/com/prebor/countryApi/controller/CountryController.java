package com.prebor.countryApi.controller;

import com.prebor.countryApi.service.CountryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CountryController {
    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping("/countries/refresh")
    public ResponseEntity<?> refreshCountryData(){
        return countryService.refreshCountryData();
    }

    @GetMapping("/countries")
    public ResponseEntity<?> getCountries( @RequestParam(required = false) String region,
                                           @RequestParam(required = false) String currency,
                                           @RequestParam(required = false) String sort){
        return countryService.getCountries(region,currency,sort);
    }

    @GetMapping("/countries/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name){
        return countryService.getCountryByName(name);
    }

    @DeleteMapping("/countries/{name}")
    public ResponseEntity<?> deleteCountryByName(@PathVariable String name){
        return countryService.deleteCountryByName(name);
    }

    @GetMapping("/countries/image")
    public ResponseEntity<?> getSummaryImage() throws IOException {
        File file = new File("cache/summary.png");

        if (!file.exists()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Summary image not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        byte[] imageBytes = Files.readAllBytes(file.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(){
        return countryService.getStatus();
    }
}
