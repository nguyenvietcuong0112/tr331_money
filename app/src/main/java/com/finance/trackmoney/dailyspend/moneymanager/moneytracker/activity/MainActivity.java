//package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;
//
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.OnBackPressedCallback;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.nativead.NativeAd;
//import com.google.android.gms.ads.nativead.NativeAdView;
//import com.google.gson.Gson;
//import com.mallegan.ads.callback.InterCallback;
//import com.mallegan.ads.callback.NativeCallback;
//import com.mallegan.ads.util.Admob;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityFullCallback;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityLoadNativeFullV2;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.Constant;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.LoadNativeFullNew;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.TransactionUpdateEvent;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.BudgetFragment;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.SettingsFragment;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment.StatisticsFragment;
//import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.List;
//
//import io.ak1.BubbleTabBar;
//
//public class MainActivity extends BaseActivity {
//
//
//    private static final int ADD_TRANSACTION_REQUEST = 1;
//    private LinearLayout navHome, llAddTransaction, llFinancial, llStatistic, llSetting;
//
//    private Fragment activeFragment;
//    private SharePreferenceUtils sharePreferenceUtils;
//    private List<TransactionModel> transactionList;
//    private Handler handler = new Handler();
//
//
//    private FrameLayout frAdsBanner;
//
//
//
//    @Override
//    public void bind() {
//        setContentView(R.layout.activity_main);
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
//
//        initializeViews();
//        setOnClickListener();
//        sharePreferenceUtils = new SharePreferenceUtils(this);
//        sharePreferenceUtils.incrementCounter();
//        transactionList = sharePreferenceUtils.getTransactionList();
//
//
//        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                CustomBottomSheetDialogExitFragment dialog = CustomBottomSheetDialogExitFragment.newInstance();
//                dialog.show(getSupportFragmentManager(), "ExitDialog");
//            }
//        });
//
////        loadInterAddTrans();
//
////        if (!SharePreferenceUtils.isOrganic(this)) {
////            TimerManager.getInstance().startTimer();
////        }
//        hideNavigationBar();
//    }
//
//    private void hideNavigationBar() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//        );
//    }
//
//    private void initializeViews() {
//        navHome = findViewById(R.id.nav_home);
//        llAddTransaction = findViewById(R.id.llAddTransaction);
//        llFinancial = findViewById(R.id.llFinancial);
//        llSetting = findViewById(R.id.llSetting);
//        llStatistic = findViewById(R.id.llStatistic);
//
//        frAdsBanner = findViewById(R.id.fr_ads_banner);
//
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
//            if (data != null && data.hasExtra("transactionData")) {
//                String transactionJson = data.getStringExtra("transactionData");
//                TransactionModel newTransaction = TransactionModel.fromJson(transactionJson);
//
//                if (newTransaction != null) {
//                    transactionList = sharePreferenceUtils.getTransactionList();
//
//                    if (activeFragment instanceof HomeFragment) {
//                        ((HomeFragment) activeFragment).onTransactionUpdated(new TransactionUpdateEvent(transactionList));
//                    } else if (activeFragment instanceof StatisticsFragment) {
//                        ((StatisticsFragment) activeFragment).onTransactionUpdated(new TransactionUpdateEvent(transactionList));
//                    } else if (activeFragment instanceof BudgetFragment) {
//                        ((BudgetFragment) activeFragment).onTransactionUpdated(new TransactionUpdateEvent(transactionList));
//                    }
//
//                    EventBus.getDefault().post(new TransactionUpdateEvent(transactionList));
//
//                    Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//
//
//    private void setOnClickListener() {
//        llAddTransaction.setOnClickListener(view -> {
//            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
//            startActivityForResult(intent, ADD_TRANSACTION_REQUEST);
//        });
//
//    }
//
//
//
//    private void loadAdsBanner() {
//
//        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_nav_bar), new NativeCallback() {
//            @Override
//            public void onNativeAdLoaded(NativeAd nativeAd) {
//                super.onNativeAdLoaded(nativeAd);
//                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
//                frAdsBanner.setVisibility(View.VISIBLE);
//                frAdsBanner.removeAllViews();
//                frAdsBanner.addView(adView);
//                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
//            }
//
//            @Override
//            public void onAdFailedToLoad() {
//                super.onAdFailedToLoad();
//                frAdsBanner.setVisibility(View.GONE);
//            }
//        });
//
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        navHome.setEnabled(true);
//        loadAdsBanner();
//
////        if (!SharePreferenceUtils.isOrganic(this)) {
////            TimerManager.getInstance().startTimer();
////        }
//    }
//
//    //    @Override
////    protected void onPause() {
////        super.onPause();
////        TimerManager.getInstance().stopTimer();
////
////    }
//
//
//}