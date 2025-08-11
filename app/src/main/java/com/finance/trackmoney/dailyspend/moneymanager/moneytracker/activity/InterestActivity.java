package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SystemConfiguration;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SystemUtil;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ActivityInterestBinding;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InterestActivity  extends BaseActivity {
    private ActivityInterestBinding binding;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    boolean isNativeLanguageSelectLoaded = false;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityInterestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (SharePreferenceUtils.isOrganic(this)) {
            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {

                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");
                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic"));
                }

                @Override
                public void onConversionDataFail(String s) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> map) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String s) {
                    // Handle attribution failure
                }
            });
        }
        initializeCheckboxes();
        setupListeners();


        loadAdsNative();
    }

    public void loadAdsNativeLanguageSelect() {
        NativeAdView adView;
        if (SharePreferenceUtils.isOrganic(this)) {
            adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.layout_native_language, null);
        } else {
            adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.layout_native_language_non_organic, null);
        }
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(InterestActivity.this, getString(R.string.native_language_select), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                isNativeLanguageSelectLoaded = true;
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                checkNextButtonStatus(true);
            }

            @Override
            public void onAdFailedToLoad() {
                binding.frAds.removeAllViews();
                checkNextButtonStatus(true);
            }
        });
    }


    private void loadAdsNative() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(InterestActivity.this, getString(R.string.native_language), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(InterestActivity.this);
                if (!SharePreferenceUtils.isOrganic(InterestActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(InterestActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(InterestActivity.this).inflate(R.layout.layout_native_language, null);
                }
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                checkNextButtonStatus(true);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds.removeAllViews();
                checkNextButtonStatus(true);
            }

        });
    }

    private void initializeCheckboxes() {
        checkBoxes.add(binding.cbTrackExpenses);
        checkBoxes.add(binding.cbMonitorSavings);
        checkBoxes.add(binding.cbAnalyzeSpending);
        checkBoxes.add(binding.cbOptimizeSpending);
        checkBoxes.add(binding.cbPlanInvestments);
    }

    private void setupListeners() {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isNativeLanguageSelectLoaded) {
                    loadAdsNativeLanguageSelect();
                }
                updateContinueButtonState();
            });
        }

        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(InterestActivity.this, IntroActivity.class);
            startActivity(intent);
        });
    }

    private void updateContinueButtonState() {
        boolean hasSelection = false;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                hasSelection = true;
                break;
            }
        }
        binding.btnContinue.setEnabled(hasSelection);
    }

    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.btnContinue.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.btnContinue.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}