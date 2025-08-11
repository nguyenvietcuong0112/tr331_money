package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.language;



import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.LanguageModel;

import java.util.ArrayList;

public class ConstantLangage {

    public static ArrayList<LanguageModel> getLanguage1() {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageModel("English (US)", "en", false, R.drawable.flag_us));
        listLanguage.add(new LanguageModel("English (UK)", "en", false, R.drawable.flag_en_en));
        listLanguage.add(new LanguageModel("English (Canada)", "en", false, R.drawable.flag_ca));
        listLanguage.add(new LanguageModel("English (South Africa)", "en", false, R.drawable.flag_sou));
        return listLanguage;
    }

    public static ArrayList<LanguageModel> getLanguage2() {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageModel("India (भारत)", "hi", false,0));
        listLanguage.add(new LanguageModel("Bengali (বাংলা)", "hi", false, 0));
        listLanguage.add(new LanguageModel("Marathi (मराठी)", "hi", false, 0));
        listLanguage.add(new LanguageModel("Telugu (తెలుగు)", "hi", false,0));
        listLanguage.add(new LanguageModel("Tamil (தமிழ்)", "hi", false,0));
        listLanguage.add(new LanguageModel("Urdu", "hi", false, 0));
        listLanguage.add(new LanguageModel("Kannada (ಕನ್ನಡ)", "hi", false, 0));
        listLanguage.add(new LanguageModel("Odia (ଓଡ଼ିଆ)", "hi", false,0));
        listLanguage.add(new LanguageModel("Malayalam (മലയാളം)", "hi", false,0));
        return listLanguage;
    }

    public static ArrayList<LanguageModel> getLanguage3() {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageModel("Portuguese (Brazil)", "hi", false, R.drawable.flag_bra));
        listLanguage.add(new LanguageModel("Portuguese(Europeu)", "hi", false,  R.drawable.flag_euro));
        listLanguage.add(new LanguageModel("Portuguese(Angona)", "hi", false,  R.drawable.flag_angola));
        listLanguage.add(new LanguageModel("Portuguese(Mozambique)", "hi", false, R.drawable.flag_mozam));
        return listLanguage;
    }
}

