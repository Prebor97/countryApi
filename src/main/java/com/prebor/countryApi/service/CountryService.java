package com.prebor.countryApi.service;

import com.prebor.countryApi.dto.CountryApiDto;
import com.prebor.countryApi.dto.ErrorResponse;
import com.prebor.countryApi.dto.ExchangeRateResponse;
import com.prebor.countryApi.entity.Country;
import com.prebor.countryApi.repository.CountryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CountryService {
    private final RestTemplate restTemplate;
    private final CountryRepository repository;

    public CountryService(RestTemplate restTemplate, CountryRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public ResponseEntity<?> refreshCountryData() {
        String countriesUrl = "https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies";
        String exchangeUrl = "https://open.er-api.com/v6/latest/USD";

        try {
            CountryApiDto[] countries;
            ExchangeRateResponse exchangeRates;

            try {
                countries = restTemplate.getForObject(countriesUrl, CountryApiDto[].class);
                exchangeRates = restTemplate.getForObject(exchangeUrl, ExchangeRateResponse.class);
            } catch (RestClientException e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ErrorResponse("Failed to fetch data from external APIs. Please try again later."));
            }

            if (countries == null || exchangeRates == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ErrorResponse("External API returned no data. Please try again later."));
            }

            try {
                for (CountryApiDto country : countries) {
                    Country existing = repository.findByName(country.getName()).orElse(null);
                    if (existing != null) {
                        saveCountry(existing, country, exchangeRates);
                    } else {
                        Country newCountry = new Country();
                        saveCountry(newCountry, country, exchangeRates);
                    }
                }
                generateSummaryImage();
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ErrorResponse("Country data successfully refreshed"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("An error occurred while saving country data: " + e.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }
    public ResponseEntity<?> getCountries(String region, String currencyCode, String sort){
        List<Country> countries = repository.findAll();

        if (region != null && !region.isBlank()) {
            String regionLower = region.trim().toLowerCase();
            countries = countries.stream()
                    .filter(c -> c.getRegion() != null && c.getRegion().trim().toLowerCase().equals(regionLower))
                    .collect(Collectors.toList());
        }

        if (currencyCode != null && !currencyCode.isBlank()) {
            String currencyLower = currencyCode.trim().toLowerCase();
            countries = countries.stream()
                    .filter(c -> c.getCurrencyCode() != null && c.getCurrencyCode().trim().toLowerCase().equals(currencyLower))
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            if ("gdp_desc".equalsIgnoreCase(sort)) {
                countries = countries.stream()
                        .sorted(Comparator.comparing(
                                Country::getEstimatedGdp,
                                Comparator.nullsLast(Double::compareTo)
                        ).reversed())
                        .collect(Collectors.toList());
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid sort parameter. Allowed values: gdp_desc"
                ));
            }
        }

        if (countries.isEmpty()) {
            return ResponseEntity.status(404).body(new ErrorResponse("No countries found matching your filters."));
        }

        return ResponseEntity.ok(countries);
    }

    public ResponseEntity<?> getCountryByName(String name){
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Country name is required."));
        }

        Country country = repository.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);

        if (country == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Country with name '" + name + "' not found."));
        }

        return ResponseEntity.ok(country);
    }

    public ResponseEntity<?> deleteCountryByName(String name){
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Country name is required."));
        }

        Country country = repository.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);

        if (country == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Country with name '" + name + "' not found."));
        }
        repository.delete(country);
        return ResponseEntity.ok(new ErrorResponse("Country '" + country.getName() + "' has been deleted successfully."));
    }

    public ResponseEntity<?> getStatus(){
        long totalCountries = repository.count();

        Instant lastRefreshedAt = repository.findAll().stream()
                .map(Country::getLastRefreshedAt)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("total_countries", totalCountries);
        response.put("last_refreshed_at", lastRefreshedAt != null ? lastRefreshedAt.toString() : null);

        return ResponseEntity.ok(response);
    }

    private void saveCountry(Country country, CountryApiDto countryApiDto, ExchangeRateResponse rateResponse) {
        country.setName(countryApiDto.getName());
        country.setCapital(countryApiDto.getCapital());
        country.setRegion(countryApiDto.getRegion());
        country.setPopulation(countryApiDto.getPopulation());

        // Defensive check for null or empty currencies
        if (countryApiDto.getCurrencies() == null ||
                countryApiDto.getCurrencies().isEmpty() ||
                countryApiDto.getCurrencies().getFirst() == null ||
                countryApiDto.getCurrencies().getFirst().getCode() == null ||
                !rateResponse.getRates().containsKey(countryApiDto.getCurrencies().getFirst().getCode())) {

            country.setCurrencyCode(null);
            country.setExchangeRate(null);
            country.setEstimatedGdp(null);

        } else {
            String code = countryApiDto.getCurrencies().getFirst().getCode();
            Double rate = rateResponse.getRates().get(code);

            country.setCurrencyCode(code);
            country.setExchangeRate(rate);

            double randomFactor = 1000 + Math.random() * 1000;
            country.setEstimatedGdp(countryApiDto.getPopulation() * randomFactor / rate);
        }

        country.setFlagUrl(countryApiDto.getFlag());
        country.setLastRefreshedAt(Instant.now());
        repository.save(country);
    }


    private void generateSummaryImage() {
        List<Country> countries = repository.findAll();
        long totalCountries = countries.size();
        List<Country> top5 = countries.stream()
                .filter(c -> c.getEstimatedGdp() != null)
                .sorted(Comparator.comparing(Country::getEstimatedGdp).reversed())
                .limit(5)
                .toList();

        Instant lastRefreshedAt = countries.stream()
                .map(Country::getLastRefreshedAt)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(Instant.now());

        try {
            int width = 600;
            int height = 300;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // Background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Text settings
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 16));

            int y = 40;
            g.drawString("🌍 Country Summary", 20, y);
            y += 30;
            g.drawString("Total countries: " + totalCountries, 20, y);
            y += 30;
            g.drawString("Top 5 by Estimated GDP:", 20, y);
            y += 25;

            for (int i = 0; i < top5.size(); i++) {
                Country c = top5.get(i);
                g.drawString((i + 1) + ". " + c.getName() + " — " + String.format("%.2f", c.getEstimatedGdp()), 40, y);
                y += 20;
            }

            y += 20;
            g.drawString("Last Refreshed: " + lastRefreshedAt.toString(), 20, y);

            g.dispose();

            // Save image to cache folder
            File dir = new File("cache");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "summary.png");
            ImageIO.write(image, "png", file);

        } catch (IOException e) {
            System.err.println("Failed to generate summary image: " + e.getMessage());
        }
    }
}