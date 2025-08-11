package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.AppOpenManager;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.CurrencyUnitActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.LanguageActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsActivity extends BaseActivity {
    private boolean isBtnProcessing = false;
    private static final int REQUEST_CURRENCY_SELECT = 100;
    String currentCurrency;

    TextView tvCurrency;
    ImageView ivBack;

    LinearLayout btnShare, btnLanguage, btnRateUs, btnPrivacyPolicy, llCurrency;
    FrameLayout frAds;

    @Override
    public void bind() {
        setContentView(R.layout.fragment_settings);

        initViews();
        setupClickListeners();

    }

    private void initViews() {
        btnShare = findViewById(R.id.btn_share);
        btnLanguage = findViewById(R.id.btn_language);
        btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy);
        btnRateUs = findViewById(R.id.btn_rate_us);
        llCurrency = findViewById(R.id.llCurrency);
        frAds = findViewById(R.id.fr_ads);
        ivBack = findViewById(R.id.iv_back);

        tvCurrency = findViewById(R.id.tv_currency);
        tvCurrency.setText(currentCurrency);

        updateCurrencyDisplay();
        loadAds();
    }


    private void setupClickListeners() {
        ivBack.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("currency_updated", true); // gửi flag
            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // thay vì onBackPressed()
                    });
        btnShare.setOnClickListener(v -> {
            if (isBtnProcessing) return;
            isBtnProcessing = true;

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = "có link app thì điền vào";
            String sub = "AI Money";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, "Share"));
            AppOpenManager.getInstance().disableAppResumeWithActivity(com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.HomeActivity.class);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBtnProcessing = false;
                }
            }, 1000);
        });

        btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LanguageActivity.class);
            intent.putExtra("from_settings", true);
            startActivity(intent);
        });

        btnRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                this.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=")));
            }
        });


        btnPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://triple888studio.vercel.app/policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });


        llCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CurrencyUnitActivity.class);
            intent.putExtra(CurrencyUnitActivity.EXTRA_FROM_SETTINGS, true);
            startActivityForResult(intent, REQUEST_CURRENCY_SELECT);
        });


    }

    private void updateCurrencyDisplay() {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";
        tvCurrency.setText(currentCurrency);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CURRENCY_SELECT && resultCode == Activity.RESULT_OK) {
            updateCurrencyDisplay();
        }
    }

    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadNativeAd(SettingsActivity.this, getString(R.string.native_setting), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);

                    NativeAdView adView = (NativeAdView) LayoutInflater.from(SettingsActivity.this)
                            .inflate(R.layout.ad_native_admob_banner_3, null);

                    frAds.removeAllViews();
                    frAds.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    frAds.setVisibility(View.GONE);
                }
            });

        } else {
            frAds.removeAllViews();
        }


    }
}