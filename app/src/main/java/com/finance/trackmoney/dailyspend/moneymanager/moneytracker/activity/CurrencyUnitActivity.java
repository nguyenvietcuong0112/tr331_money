package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.AdType;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.FirebaseUtil;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.HomeActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.CurrencyUnitModel;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.Utils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter.CurrencyUnitAdapter;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.AbsBaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ActivityCurrencyUnitBinding;

public class CurrencyUnitActivity extends AbsBaseActivity {

    public static final String EXTRA_FROM_SETTINGS = "extra_from_settings";

    CurrencyUnitAdapter currencyUnitAdapter;
    private ActivityCurrencyUnitBinding binding;

    private AdView adView;


    @Override
    public void bind() {
        binding = ActivityCurrencyUnitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);


        if (!SharePreferenceUtils.isOrganic(this)) {
            adView = new AdView(CurrencyUnitActivity.this);
            adView.setAdSize(AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this, getAdWidth()));
            adView.setAdUnitId(getString(R.string.banner_inline_currency));
            adView.setAdListener(
                    new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            adView.setOnPaidEventListener(adValue -> {
                                FirebaseUtil.logPaidAdImpression(CurrencyUnitActivity.this,
                                        adValue,
                                        adView.getAdUnitId(), AdType.BANNER);
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.d("23312321", "truomhj ");
                        }
                    });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView = null;
        }
        currencyUnitAdapter = new CurrencyUnitAdapter(this, Utils.getCurrencyUnit(), new CurrencyUnitAdapter.IClickCurrencyUnit() {
            @Override
            public void onStartLoading() {
                if (binding.btnNextLoading != null) {
                    binding.btnNextLoading.show();
                    binding.ivSelect.setVisibility(View.GONE);
                    binding.btnNextLoading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onClick(CurrencyUnitModel data) {
                if (binding.btnNextLoading != null) {
                    binding.btnNextLoading.hide();
                    binding.btnNextLoading.setVisibility(View.GONE);

                    binding.ivSelect.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.ivSelect.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                }

                binding.ivSelect.setEnabled(true);
                binding.ivSelect.setAlpha(1.0f);
            }
        }, adView);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the list when text changes
                currencyUnitAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        binding.rvCurrencyUnit.setAdapter(currencyUnitAdapter);

        if (fromSettings) {
            binding.ivSelect.setEnabled(true);
            binding.ivSelect.setAlpha(1.0f);
        } else {
            binding.ivSelect.setEnabled(false);
            binding.ivSelect.setAlpha(0.3f);
        }

        binding.ivSelect.setOnClickListener(v -> {
            if (fromSettings) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                startActivity(new Intent(CurrencyUnitActivity.this, HomeActivity.class));
            }
        });
        loadAds();
    }


    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_currency), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(CurrencyUnitActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(CurrencyUnitActivity.this)
                            .inflate(R.layout.layout_native_language, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(CurrencyUnitActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds.setVisibility(View.GONE);
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public int getAdWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int adWidthPixels = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            adWidthPixels = windowMetrics.getBounds().width();
        }

        float density = displayMetrics.density;
        return (int) (adWidthPixels / density);
    }
}
