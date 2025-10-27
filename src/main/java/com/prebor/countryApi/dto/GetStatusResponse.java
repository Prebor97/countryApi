package com.prebor.countryApi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class GetStatusResponse {
    @JsonProperty("total_countries")
    public int totalCountries;
    @JsonProperty("last_refreshed_at")
    public Instant lastRefreshedAt;

    public GetStatusResponse(int totalCountries, Instant lastRefreshedAt) {
        this.totalCountries = totalCountries;
        this.lastRefreshedAt = lastRefreshedAt;
    }

    public GetStatusResponse() {
    }

    public int getTotalCountries() {
        return totalCountries;
    }

    public void setTotalCountries(int totalCountries) {
        this.totalCountries = totalCountries;
    }

    public Instant getLastRefreshedAt() {
        return lastRefreshedAt;
    }

    public void setLastRefreshedAt(Instant lastRefreshedAt) {
        this.lastRefreshedAt = lastRefreshedAt;
    }
}
