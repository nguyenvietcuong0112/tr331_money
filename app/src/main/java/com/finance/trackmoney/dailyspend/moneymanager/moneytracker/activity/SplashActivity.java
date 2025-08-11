package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Process;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.LoadAdError;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.ConsentHelper;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.HomeActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.ActivityLoadNativeFull;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SystemUtil;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ActivitySplashBinding;


import java.util.Map;


public class SplashActivity extends BaseActivity {
    private InterCallback interCallback;
    SharedPreferences.Editor editor;
    SharedPreferences spref;

    private SharePreferenceUtils sharePreferenceUtils;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        ActivitySplashBinding activitySplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        getWindow().setFlags(1024, 1024);

        setContentView(activitySplashBinding.getRoot());

        new Thread(() -> {
            for (int progress = 0; progress <= 99; progress++) {
                final int currentProgress = progress;
                runOnUiThread(() -> {
                    activitySplashBinding.progressBar.setProgress(currentProgress);
                    activitySplashBinding.tvLoading.setText(currentProgress + "%");
                });
                try {
                    Thread.sleep(180);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
        loadAds();


        SharedPreferences sharedPreferences = getSharedPreferences("pref_ads", 0);
        this.spref = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    private void loadAds() {
        sharePreferenceUtils = new SharePreferenceUtils(this);
        Uri uri = getIntent().getData();

        ConsentHelper consentHelper = ConsentHelper.getInstance(this);
        if (!consentHelper.canLoadAndShowAds()) {
            consentHelper.reset();
        }
        consentHelper.obtainConsentAndShow(this, this::startFakeProgressLoadingAndLoadAd);


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
    }


    private void startFakeProgressLoadingAndLoadAd() {
        sharePreferenceUtils = new SharePreferenceUtils(this);
        int counterValue = sharePreferenceUtils.getCurrentValue();
        new Thread(() -> runOnUiThread(() -> {
            Admob.getInstance().loadAndShowInter(
                    SplashActivity.this,
                    getString(R.string.inter_splash),
                    0,
                    3000,
                    new InterCallback() {

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            proceedToNextActivity();

                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            proceedToNextActivity();
                        }
                    }
            );
        })).start();
    }

    private void proceedToNextActivity() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            ActivityLoadNativeFull.open(this, getString(R.string.native_full_splash), () -> {
//                ActivityLoadNativeFull.open(this, getString(R.string.native_full_splash2), this::handleNavigate);
                handleNavigate();

            });
        } else {
            handleNavigate();
        }

    }

    private void handleNavigate() {
        startActivity(new Intent(SplashActivity.this, LanguageActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExitApp();
    }

    public void ExitApp() {
        moveTaskToBack(true);
        finish();
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
