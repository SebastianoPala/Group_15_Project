package com.unipi.PlayerHive.DTO.analytics;

public class OsPlatformStatsDTO {
    private Integer osCount;
    private Double avgScore;
    private Integer totalGames;

    public OsPlatformStatsDTO(Integer osCount, Double avgScore, Integer totalGames) {
        this.osCount = osCount;
        this.avgScore = avgScore;
        this.totalGames = totalGames;
    }

    public Integer getOsCount() { return osCount; }
    public Double getAvgScore() { return avgScore; }
    public Integer getTotalGames() { return totalGames; }
}
