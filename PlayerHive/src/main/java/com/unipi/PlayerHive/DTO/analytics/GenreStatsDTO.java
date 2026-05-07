package com.unipi.PlayerHive.DTO.analytics;

public class GenreStatsDTO {
    private String genre;
    private Double avgScore;
    private Double avgHoursPerPlayer;
    private Integer totalGames;

    public GenreStatsDTO(String genre, Double avgScore, Double avgHoursPerPlayer, Integer totalGames) {
        this.genre = genre;
        this.avgScore = avgScore;
        this.avgHoursPerPlayer = avgHoursPerPlayer;
        this.totalGames = totalGames;
    }

    public String getGenre() { return genre; }
    public Double getAvgScore() { return avgScore; }
    public Double getAvgHoursPerPlayer() { return avgHoursPerPlayer; }
    public Integer getTotalGames() { return totalGames; }
}
