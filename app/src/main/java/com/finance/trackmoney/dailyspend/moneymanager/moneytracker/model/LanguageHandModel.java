package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model;

public class LanguageHandModel {
    public String languageName;
    public String isoLanguage;
    public Boolean isCheck;
    public int image;
    private boolean isHandVisible;


    public LanguageHandModel(String languageName, String isoLanguage, Boolean isCheck, int image, boolean isHandVisible) {
        this.languageName = languageName;
        this.isoLanguage = isoLanguage;
        this.isCheck = isCheck;
        this.image = image;
        this.isHandVisible = isHandVisible;

    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getIsoLanguage() {
        return isoLanguage;
    }

    public void setIsoLanguage(String isoLanguage) {
        this.isoLanguage = isoLanguage;
    }

    public Boolean getCheck() {
        return isCheck;
    }

    public void setCheck(Boolean check) {
        isCheck = check;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
    public boolean isHandVisible() {
        return isHandVisible;
    }

    public void setHandVisible(boolean handVisible) {
        isHandVisible = handVisible;
    }
}
