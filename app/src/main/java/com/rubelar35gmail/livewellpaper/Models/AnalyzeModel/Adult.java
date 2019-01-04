package com.rubelar35gmail.livewellpaper.Models.AnalyzeModel;

public class Adult {
    private boolean isAdultContent;
    private boolean isRacyContent;
    private double adultScore;
    private double racyScore;

    public Adult() {
    }

    public Adult(boolean isAdultContent, boolean isRacyContent, double adultScore, double racyScore) {
        this.isAdultContent = isAdultContent;
        this.isRacyContent = isRacyContent;
        this.adultScore = adultScore;
        this.racyScore = racyScore;
    }

    public boolean isAdultContent() {
        return isAdultContent;
    }

    public void setAdultContent(boolean adultContent) {
        isAdultContent = adultContent;
    }

    public boolean isRacyContent() {
        return isRacyContent;
    }

    public void setRacyContent(boolean racyContent) {
        isRacyContent = racyContent;
    }

    public double getAdultScore() {
        return adultScore;
    }

    public void setAdultScore(double adultScore) {
        this.adultScore = adultScore;
    }

    public double getRacyScore() {
        return racyScore;
    }

    public void setRacyScore(double racyScore) {
        this.racyScore = racyScore;
    }
}
