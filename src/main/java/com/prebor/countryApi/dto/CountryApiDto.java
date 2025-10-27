package com.prebor.countryApi.dto;

import java.util.List;

public class CountryApiDto {
    private String name;
    private String capital;
    private String region;
    private long population;
    private List<Currency> currencies;
    private String flag;
    private boolean independent;

    public CountryApiDto() {
    }

    public CountryApiDto(String name, String capital, String region, long population,
                         List<Currency> currencies, String flag, boolean independent) {
        this.name = name;
        this.capital = capital;
        this.region = region;
        this.population = population;
        this.currencies = currencies;
        this.flag = flag;
        this.independent = independent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public boolean isIndependent() {
        return independent;
    }

    public void setIndependent(boolean independent) {
        this.independent = independent;
    }
}
