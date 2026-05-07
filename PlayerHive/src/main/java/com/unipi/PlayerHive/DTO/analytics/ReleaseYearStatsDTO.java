package com.unipi.PlayerHive.DTO.analytics;

public class ReleaseYearStatsDTO {
    private Integer releaseYear;
    private Double avgScore;
    private Integer totalGames;

    public ReleaseYearStatsDTO(Integer releaseYear, Double avgScore, Integer totalGames) {
        this.releaseYear = releaseYear;
        this.avgScore = avgScore;
        this.totalGames = totalGames;
    }

    public Integer getReleaseYear() { return releaseYear; }
    public Double getAvgScore() { return avgScore; }
    public Integer getTotalGames() { return totalGames; }
}
