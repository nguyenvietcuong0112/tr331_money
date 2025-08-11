package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.AbsBaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ActivityNativeFullBinding;

public class ActivityLoadNativeFull extends AbsBaseActivity {

    private ActivityNativeFullBinding binding;
    private static final String NATIVE_FULL_AD_ID = "native_full_ad_id";
    private static ActivityFullCallback callback;
    private int count = 0;

    public static void open(Context context, String adId, ActivityFullCallback cb) {
        callback = cb;
        Intent intent = new Intent(context, ActivityLoadNativeFull.class);
        intent.putExtra(NATIVE_FULL_AD_ID, adId);
        context.startActivity(intent);
    }


    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String adId = getIntent().getStringExtra(NATIVE_FULL_AD_ID);
        if (adId == null || adId.isEmpty()) {
            finish();
            return;
        }

        loadNativeFull(adId);
    }

    private void loadNativeFull(String adId) {
        Admob.getInstance().loadNativeAds(this, adId, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAdsFull.setVisibility(View.GONE);
                if (callback != null) {
                    callback.onResultFromActivityFull();
                }
                finish();
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFull.this)
                        .inflate(R.layout.native_full_language, null);

                ImageView closeButton = adView.findViewById(R.id.close);
                MediaView mediaView = adView.findViewById(R.id.ad_media);

                closeButton.setOnClickListener(v -> mediaView.performClick());

                new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Optional: update UI if needed
                    }

                    @Override
                    public void onFinish() {
                        closeButton.setOnClickListener(v -> {
                            if (callback != null) {
                                callback.onResultFromActivityFull();
                            }
                            finish();
                        });
                    }
                }.start();

                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        count++;
        if (count >= 2) {
            if (callback != null) {
                callback.onResultFromActivityFull();
            }
            finish();
        }
    }
}
