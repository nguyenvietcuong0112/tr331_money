package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter.SlideAdapter;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityFullCallback;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityLoadNativeFullV2;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ActivityIntroBinding;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.HomeActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SystemConfiguration;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SystemUtil;

public class IntroActivity extends BaseActivity implements View.OnClickListener {
    private ImageView[] dots = null;
    private ActivityIntroBinding binding;

    private boolean loadNative1 = true;
    private boolean loadNative2 = true;
    private boolean loadNative3 = true;
    private boolean loadNative4 = true;

    private boolean firstIntro1 = true;
    private boolean firstIntro2 = true;
    private boolean firstIntro3 = true;
    private boolean firstIntro4 = true;
    private boolean isInline = true;

    private boolean isBanner = false;


    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (SystemUtil.isNetworkConnected(this)) {
            binding.frAds.setVisibility(View.VISIBLE);
        } else binding.frAds.setVisibility(View.GONE);
        dots = new ImageView[]{binding.cricle1, binding.cricle2, binding.cricle3, binding.cricle4};
        SlideAdapter adapter = new SlideAdapter(this);
        binding.viewPager2.setAdapter(adapter);
        setUpSlideIntro();
        binding.btnNext.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);

        loadNative1();
        loadNativeIntro2();
        loadNative3();
        loadNative4();

        if (!SharePreferenceUtils.isOrganic(this)) {
            loadBannerInline();
            loadAdsBanner2();
        } else {
            binding.adCardView.setVisibility(View.GONE);
            binding.adsBanner.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnNext) {
            if (binding.viewPager2.getCurrentItem() == 3) {
                checkNextButtonStatus(false);
                goToHome();
                new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
            } else if (binding.viewPager2.getCurrentItem() == 2) {
                binding.adCardView.setVisibility(View.GONE);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
                if (!SharePreferenceUtils.isOrganic(this)) {
                    checkNextButtonStatus(false);
                    new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
                }
                loadNativeAds(3);
            } else if (binding.viewPager2.getCurrentItem() == 1) {
                binding.adCardView.setVisibility(View.GONE);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
                if (!SharePreferenceUtils.isOrganic(this)) {
                    checkNextButtonStatus(false);
                    new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
                }
                loadNativeAds(2);
            } else if (binding.viewPager2.getCurrentItem() == 0) {
                binding.adCardView.setVisibility(View.GONE);
                binding.adsBanner.setVisibility(View.VISIBLE);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
                if (!SharePreferenceUtils.isOrganic(this)) {
                    checkNextButtonStatus(false);
                    new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
                }
                loadNativeAds(1);
            } else {
                loadNativeAds(0);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
            }
        } else if (v == binding.btnBack) {
            if (binding.viewPager2.getCurrentItem() == 3) {
                loadNativeAds(2);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() - 1);
            } else if (binding.viewPager2.getCurrentItem() == 2) {
                loadNativeAds(1);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() - 1);
            } else if (binding.viewPager2.getCurrentItem() == 1) {
                loadNativeAds(0);
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() - 1);
            }
        }
    }

    private void loadBannerInline() {
        if(!SharePreferenceUtils.isOrganic(this)){
            if (isInline) {
                binding.adsBanner.setVisibility(View.GONE);
                binding.adCardView.setVisibility(View.VISIBLE);
                Admob.getInstance().loadInlineBanner(this, getString(R.string.banner_inline_intro), Admob.BANNER_INLINE_LARGE_STYLE);
            } else {
                binding.adCardView.setVisibility(View.GONE);
            }
        }
    }

    private void loadAdsBanner2() {
        if(!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_intro), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this).inflate(R.layout.ad_native_admod_banner, null);
                    binding.adsBanner.setVisibility(View.VISIBLE);
                    binding.adsBanner.removeAllViews();
                    binding.adsBanner.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    isBanner = true;
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    binding.adsBanner.setVisibility(View.GONE);
                }
            });
        } else {
            binding.adsBanner.setVisibility(View.GONE);
            binding.adsBanner.removeAllViews();
        }

    }

    private void loadNativeAds(int position) {
        switch (position) {
            case 0:
                if (firstIntro1) {
                    loadNative1();
                    loadAdsBanner2();
                }
                break;
            case 1:
                if (!firstIntro2) {
                    loadNativeIntro2();
                    loadAdsBanner2();
                }
                firstIntro2 = false;
                break;
            case 2:
                if (!firstIntro3) {
                    loadNative3();
                    loadAdsBanner2();
                }
                firstIntro3 = false;
                break;
            case 3:
                if (!firstIntro4) {
                    loadNative4();
                    loadBannerInline();
                }
                firstIntro4 = false;
                break;
            default:
                break;
        }
    }


//    public void goToHome() {
//            startActivity(new Intent(this, HomeActivity.class));
//    }

    public void goToHome() {
        String selectedCurrencyCode = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (selectedCurrencyCode.isEmpty()) {

            if (!SharePreferenceUtils.isOrganic(IntroActivity.this)) {
                Admob.getInstance().loadAndShowInter(this,getString(R.string.inter_intro), 0 , 30000, new InterCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro), new ActivityFullCallback() {
                            @Override
                            public void onResultFromActivityFull() {
//                                ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro2), new ActivityFullCallback() {
//                                    @Override
//                                    public void onResultFromActivityFull() {
                                        Intent intent = new Intent(IntroActivity.this, CurrencyUnitActivity.class);
                                        startActivity(intent);
                                        finish();
//                                    }
//                                });
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro), new ActivityFullCallback() {
                            @Override
                            public void onResultFromActivityFull() {
//                                ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro2), new ActivityFullCallback() {
//                                    @Override
//                                    public void onResultFromActivityFull() {
                                        Intent intent = new Intent(IntroActivity.this, CurrencyUnitActivity.class);
                                        startActivity(intent);
                                        finish();
//                                    }
//                                });
                            }
                        });
                    }
                } );
            } else {
                startActivity(new Intent(IntroActivity.this, CurrencyUnitActivity.class));
            }
        } else {
            if (!SharePreferenceUtils.isOrganic(IntroActivity.this)) {
                Admob.getInstance().loadAndShowInter(this,getString(R.string.inter_intro), 0 , 30000, new InterCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro), new ActivityFullCallback() {
                            @Override
                            public void onResultFromActivityFull() {
//                                ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro2), new ActivityFullCallback() {
//                                    @Override
//                                    public void onResultFromActivityFull() {
                                        Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
                                        startActivity(intent);
//                                    }
//                                });
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro), new ActivityFullCallback() {
                            @Override
                            public void onResultFromActivityFull() {
//                                ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro2), new ActivityFullCallback() {
//                                    @Override
//                                    public void onResultFromActivityFull() {
                                        Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
                                        startActivity(intent);
//                                    }
//                                });
                            }
                        });
                    }
                } );
            } else {
                Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
                startActivity(intent);
            }

        }

    }

    public static boolean isAccessibilitySettingsOn(Context r7) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(r7.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.i("TAG", e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(r7.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(r7.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        changeContentInit(binding.viewPager2.getCurrentItem());
    }

    private void setUpSlideIntro() {
        SlideAdapter adapter = new SlideAdapter(this);
        binding.viewPager2.setAdapter(adapter);

        binding.viewPager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeContentInit(position);
                loadNativeAds(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void changeContentInit(int position) {
        for (int i = 0; i < 4; i++) {
            if (i == position)
                dots[i].setImageResource(R.drawable.bg_indicator_true);
            else
                dots[i].setImageResource(R.drawable.bg_indicator);
        }
        if (position == 0) {
            binding.adCardView.setVisibility(View.GONE);
            if (!SharePreferenceUtils.isOrganic(this)) {
                binding.adsBanner.setVisibility(View.VISIBLE);
            }
            if (loadNative1) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds1.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds1.setVisibility(View.GONE);
            }
            binding.frAds4.setVisibility(View.GONE);
            binding.frAds3.setVisibility(View.GONE);
            binding.frAds2.setVisibility(View.GONE);
            binding.btnBack.setAlpha(0.5f);

            SystemUtil.setLocale(this);

        } else if (position == 1) {
            binding.adCardView.setVisibility(View.GONE);
            if (loadNative2) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds2.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds2.setVisibility(View.GONE);
            }
            binding.btnBack.setAlpha(1f);
            binding.frAds1.setVisibility(View.GONE);
            binding.frAds3.setVisibility(View.GONE);
            binding.frAds4.setVisibility(View.GONE);

            SystemUtil.setLocale(this);
        } else if (position == 2) {
            if (isBanner) {
                binding.adsBanner.setVisibility(View.VISIBLE);
                binding.adCardView.setVisibility(View.GONE);
            }
            if (loadNative3) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds3.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds3.setVisibility(View.GONE);
            }
            binding.btnBack.setAlpha(1f);
            binding.frAds4.setVisibility(View.GONE);
            binding.frAds1.setVisibility(View.GONE);
            binding.frAds2.setVisibility(View.GONE);
            SystemUtil.setLocale(this);
        } else if (position == 3) {
            binding.adsBanner.setVisibility(View.GONE);
            if (isInline & !SharePreferenceUtils.isOrganic(this)) {
                binding.adCardView.setVisibility(View.VISIBLE);
            }
            if (loadNative4) {
                binding.frAds.setVisibility(View.VISIBLE);
                binding.frAds4.setVisibility(View.VISIBLE);
            } else {
                binding.frAds.setVisibility(View.GONE);
                binding.frAds4.setVisibility(View.GONE);
            }
            binding.btnBack.setAlpha(1f);
            binding.frAds1.setVisibility(View.GONE);
            binding.frAds2.setVisibility(View.GONE);
            binding.frAds3.setVisibility(View.GONE);
            SystemUtil.setLocale(this);
        }
        hideNavigationBar();
    }

    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and later
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    int count = 0;
    @Override
    protected void onResume() {
        super.onResume();
        count++;
        int currentItem = binding.viewPager2.getCurrentItem();

        switch (currentItem) {
            case 0:
                if (count >= 2) {
                    loadNative1();
                    loadAdsBanner2();
                }
                break;
            case 1:
                if (count >= 2) {
                    loadNativeIntro2();
                    loadAdsBanner2();
                }
                break;
            case 2:
                if (count >= 2) {
                    loadNative3();
                    loadAdsBanner2();
                }
                break;
            case 3:
                if (count >= 2) {
                    loadNative4();
                    loadBannerInline();
                }
                break;
        }
    }

    private void loadNative1() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding1), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds1.setVisibility(View.GONE);
                loadNative1 = false;
                checkNextButtonStatus(true);
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(IntroActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                            .inflate(R.layout.layout_native_intro, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
                loadNative1 = true;
                binding.frAds1.removeAllViews();
                binding.frAds1.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                checkNextButtonStatus(true);
            }
        });
    }


    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.btnNext.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    private void loadNative3() {
        if (!SharePreferenceUtils.isOrganic(IntroActivity.this)) {
            checkNextButtonStatus(false);
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding3), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    runOnUiThread(() -> {
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this).inflate(R.layout.layout_native_introthree_non_organic, null);
                        binding.frAds3.removeAllViews();
                        binding.frAds3.addView(adView);
                        loadNative3 = true;
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        new Handler().postDelayed(() -> {
                            checkNextButtonStatus(true);
                        }, 500);

                    });
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    runOnUiThread(() -> {
                        loadNative3 = true;
                        binding.frAds3.setVisibility(View.GONE);
                        checkNextButtonStatus(true);
                    });
                }

            });
        } else {
            loadNative3 = false;
            binding.frAds3.removeAllViews();
            binding.frAds3.setVisibility(View.GONE);
        }
    }

    private void loadNativeIntro2() {
        if (!SharePreferenceUtils.isOrganic(IntroActivity.this)) {
            checkNextButtonStatus(false);
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding2), new NativeCallback() {
                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    runOnUiThread(() -> {
                        binding.frAds2.removeAllViews();
                        binding.frAds2.setVisibility(View.GONE);
                        loadNative2 = false;
                        checkNextButtonStatus(true);

                    });

                }

                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    runOnUiThread(() -> {
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this).inflate(R.layout.layout_native_introtwo_non_organic, null);
                        binding.frAds2.removeAllViews();
                        binding.frAds2.addView(adView);
                        loadNative2 = true;
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        new Handler().postDelayed(() -> {
                            checkNextButtonStatus(true);
                        }, 500);

                    });

                }
            });
        } else {
            loadNative2 = false;
            binding.frAds2.removeAllViews();
            binding.frAds2.setVisibility(View.GONE);
        }
    }

    private void loadNative4() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding4), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                runOnUiThread(() -> {
                    loadNative4 = false;
                    binding.frAds4.removeAllViews();
                    binding.frAds4.setVisibility(View.GONE);
                    checkNextButtonStatus(true);
                });

            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(IntroActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                            .inflate(R.layout.layout_native_intro, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
                runOnUiThread(() -> {
                    loadNative4 = true;
                    binding.frAds4.removeAllViews();
                    binding.frAds4.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    new Handler().postDelayed(() -> {
                        checkNextButtonStatus(true);
                    }, 500);
                });
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}