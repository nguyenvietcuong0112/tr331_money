package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends BaseActivity {
    private TextView tvDate, tvAmount, tvCategory, tvNote;
    private LinearLayout btnDelete , btnEdit;
    private ImageButton btnBack;
    ImageView ivCategory;
    private TransactionModel transaction;
    private FrameLayout frAds;
    private FrameLayout frAdsBanner;
    private boolean isFirstLoad = true;

    FrameLayout frAdsHomeTop;

    FrameLayout frAdsCollap;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedLoadExpandTask;

    private Runnable loadTask = new Runnable() {
        @Override
        public void run() {
            loadNativeExpnad();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public void bind() {
        setContentView(R.layout.activity_transaction_detail);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        initializeViews();
        setupBackButton();
        loadTransactionData();
        setupDeleteButton();
        setupEditButton();

        loadAdsBanner();
    }

    private void setupEditButton() {
        btnEdit.setOnClickListener(v -> {

            if (indexToDelete == -1) {
                findTransactionIndex();
            }
            Intent intent = new Intent(TransactionDetailActivity.this, AddTransactionActivity.class);
            intent.putExtra("transaction", transaction);
            intent.putExtra("position", indexToDelete);
            startActivityForResult(intent, 1);


        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        tvDate = findViewById(R.id.tv_date);
        tvAmount = findViewById(R.id.tv_amount);
        tvCategory = findViewById(R.id.tv_category);
        tvNote = findViewById(R.id.tv_note);
        btnDelete = findViewById(R.id.btn_delete);
        frAds = findViewById(R.id.frAds);
        btnEdit = findViewById(R.id.btn_edit);
        ivCategory = findViewById(R.id.iv_category);
        frAdsBanner = findViewById(R.id.fr_ads_banner);

        frAdsHomeTop = findViewById(R.id.frAdsHomeTop);
        frAdsCollap = findViewById(R.id.frAdsCollap);

    }

    private void loadAdsBanner() {

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_detail_transaction), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(TransactionDetailActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
                frAdsBanner.setVisibility(View.VISIBLE);
                frAdsBanner.removeAllViews();
                frAdsBanner.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                frAdsBanner.setVisibility(View.GONE);
            }
        });


    }


    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadTransactionData() {
        Intent intent = getIntent();
        if (intent == null) {
            showError("Invalid transaction data");
            return;
        }

        // First try to get as serializable object
        transaction = (TransactionModel) intent.getSerializableExtra("transaction");

        // If that fails, try to get as JSON string
        if (transaction == null && intent.hasExtra("transaction_json")) {
            String transactionJson = intent.getStringExtra("transaction_json");
            if (transactionJson != null && !transactionJson.isEmpty()) {
                transaction = new Gson().fromJson(transactionJson, TransactionModel.class);
            }
        }

        if (transaction == null) {
            showError("Transaction details not found");
            return;
        }

        displayTransactionDetails();
    }

    private void displayTransactionDetails() {
        tvDate.setText(transaction.getDate());
        tvCategory.setText(transaction.getCategoryName());
        ivCategory.setImageResource(transaction.getCategoryIcon());
        String note = transaction.getNote();
        tvNote.setText(note != null && !note.isEmpty() ? note : "None");

        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedAmount = String.format("%s%s",
                currentCurrency,
                formatter.format(Double.parseDouble(transaction.getAmount()))
        );

        tvAmount.setText(formattedAmount);
        setAmountColor();
    }


    private void setAmountColor() {
        int colorResId;
        switch (transaction.getTransactionType()) {
            case "Income":
                colorResId = R.color.green;
                break;
            case "Expense":
                colorResId = R.color.red;
                break;
            default:
                colorResId = R.color.black;
        }
        tvAmount.setTextColor(getResources().getColor(colorResId));
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Cancel", null)
                .show();
    }

    int indexToDelete = -1;

    private void deleteTransaction() {
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
        List<TransactionModel> transactions = preferenceUtils.getTransactionList();

        if (transactions == null || transactions.isEmpty()) {
            showError("Unable to delete transaction");
            return;
        }


        for (int i = 0; i < transactions.size(); i++) {
            TransactionModel t = transactions.get(i);
            if (t.getDate().equals(transaction.getDate()) &&
                    t.getAmount().equals(transaction.getAmount()) &&
                    t.getCategoryName().equals(transaction.getCategoryName()) &&
                    t.getTransactionType().equals(transaction.getTransactionType()) &&
                    t.getTime().equals(transaction.getTime())) {
                indexToDelete = i;
                break;
            }
        }

        if (indexToDelete == -1) {
            showError("Transaction not found");
            return;
        }
        transactions.remove(indexToDelete);
        preferenceUtils.saveTransactionList(transactions);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleted_position", indexToDelete);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void findTransactionIndex() {
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
        List<TransactionModel> transactions = preferenceUtils.getTransactionList();

        if (transactions != null && !transactions.isEmpty()) {
            for (int i = 0; i < transactions.size(); i++) {
                TransactionModel t = transactions.get(i);
                if (t.getDate().equals(transaction.getDate()) &&
                        t.getAmount().equals(transaction.getAmount()) &&
                        t.getCategoryName().equals(transaction.getCategoryName()) &&
                        t.getTransactionType().equals(transaction.getTransactionType()) &&
                        t.getTime().equals(transaction.getTime())) {
                    indexToDelete = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("transactionData")) {
                String transactionJson = data.getStringExtra("transactionData");
                Gson gson = new Gson();
                TransactionModel updatedTransaction = gson.fromJson(transactionJson, TransactionModel.class);

                transaction = updatedTransaction;

                SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
                List<TransactionModel> transactions = preferenceUtils.getTransactionList();
                if (transactions != null && indexToDelete != -1) {
                    transactions.set(indexToDelete, updatedTransaction);
                    preferenceUtils.saveTransactionList(transactions);
                }

                displayTransactionDetails();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (!SharePreferenceUtils.isOrganic(this)) {
            if (isFirstLoad) {
                loadNativeCollap(() -> {
                    delayedLoadExpandTask = new Runnable() {
                        @Override
                        public void run() {
                            loadNativeExpnad();
                            isFirstLoad = false;
                        }
                    };
                    handler.postDelayed(delayedLoadExpandTask, 1000);
                });
            } else {
                loadNativeCollap(() -> {
                    delayedLoadExpandTask = new Runnable() {
                        @Override
                        public void run() {
                            loadNativeExpnad();
                        }
                    };
                    handler.postDelayed(delayedLoadExpandTask, 10000);
                });
            }
        } else {
            frAdsCollap.removeAllViews();
            frAdsHomeTop.removeAllViews();
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(loadTask);
        if (delayedLoadExpandTask != null) {
            handler.removeCallbacks(delayedLoadExpandTask);
            delayedLoadExpandTask = null;
        }
    }

    private void loadNativeCollap(@Nullable final Runnable onLoaded) {

        Log.d("Truowng", "loadNativeCollapA: ");
        if (frAdsHomeTop != null) {
            frAdsHomeTop.removeAllViews();
        }

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_collap_detail_transaction), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {

                NativeAdView adView = (NativeAdView) LayoutInflater.from(TransactionDetailActivity.this).inflate(R.layout.layout_native_home_collap, null);
                if (frAdsCollap != null) {
                    frAdsCollap.removeAllViews();
                    frAdsCollap.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                if (onLoaded != null) {
                    onLoaded.run();
                }
            }

            @Override
            public void onAdFailedToLoad() {

                if (frAdsCollap != null) {
                    frAdsCollap.removeAllViews();
                }

                if (onLoaded != null) {
                    onLoaded.run();
                }
            }
        });
    }


    private void loadNativeExpnad() {

        Log.d("Truowng", "loadNativeCollapB: ");
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_expand_detail_transaction), new NativeCallback() {

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(TransactionDetailActivity.this).inflate(R.layout.layout_native_expand_transaction, null);
                if (frAdsHomeTop != null) {
                    frAdsHomeTop.removeAllViews();
                    MediaView mediaView = adView.findViewById(R.id.ad_media);
                    ImageView closeButton = adView.findViewById(R.id.close);
                    closeButton.setOnClickListener(v -> {
                        mediaView.performClick();
                    });
                    Log.d("truong", "onNativeAdLoaded: ");
                    frAdsHomeTop.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }
            }

            @Override
            public void onAdFailedToLoad() {

                if (frAdsHomeTop != null) {
                    frAdsHomeTop.removeAllViews();
                }
            }
        });
    }


}