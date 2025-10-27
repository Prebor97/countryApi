package com.prebor.countryApi.dto;

public class CountryUpdateDto {
    private Long population;
    private String currencyCode;
    private Double exchangeRate;

    public CountryUpdateDto(Long population, String currencyCode, Double exchangeRate) {
        this.population = population;
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
    }

    public CountryUpdateDto() {
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
