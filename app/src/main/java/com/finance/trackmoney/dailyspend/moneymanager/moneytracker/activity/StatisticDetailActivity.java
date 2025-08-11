package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter.MonthlyStatisticsAdapter;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.MonthlyStatisticsModel;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticDetailActivity extends AppCompatActivity {

    private TextView tvTotalBalance;
    private TextView tvExpendTotal;
    private TextView tvIncomeTotal;
    private RecyclerView rvMonthlyStatistics;
    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expense";
    private String currentMonth;
    private ImageView ivBack;
    private String currentCurrency;
    private MonthlyStatisticsAdapter adapter;

    private FrameLayout frAds;
    private FrameLayout frAdsBanner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        initViews();
        loadTransactionData();


        processData();
    }

    private void initViews() {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvExpendTotal = findViewById(R.id.tv_expend_total);
        ivBack = findViewById(R.id.btn_back);
        tvIncomeTotal = findViewById(R.id.tv_income_total);
        rvMonthlyStatistics = findViewById(R.id.rv_monthly_statistics);
        rvMonthlyStatistics.setLayoutManager(new LinearLayoutManager(this));
        frAds = findViewById(R.id.frAds);
        frAdsBanner = findViewById(R.id.fr_ads_banner);

        ivBack.setOnClickListener(v -> onBackPressed());
        loadAds();
        loadAdsBanner();

    }

    private void loadAdsBanner() {

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_total_balance), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(StatisticDetailActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
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



    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(StatisticDetailActivity.this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_total_balance), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView
                            adView = (NativeAdView) LayoutInflater.from(StatisticDetailActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);

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


    private void loadTransactionData() {
        allTransactionList.clear();
        if (getIntent().getExtras() != null) {
            String transactionListJson = getIntent().getStringExtra("transactionList");
            if (transactionListJson != null && !transactionListJson.isEmpty()) {
                Type type = new TypeToken<List<TransactionModel>>() {}.getType();
                allTransactionList = new Gson().fromJson(transactionListJson, type);
                if (allTransactionList == null) {
                    allTransactionList = new ArrayList<>();
                }
            }

            currentTransactionType = getIntent().getStringExtra("currentTransactionType");
            currentMonth = getIntent().getStringExtra("currentMonth");
        }
    }

    private void processData() {
        double totalBalance = 0;
        double totalExpend = 0;
        double totalIncome = 0;

        for (TransactionModel transaction : allTransactionList) {
            double amount = Double.parseDouble(transaction.getAmount());

            if (transaction.getTransactionType().equals("Expense")) {
                totalExpend += amount;
            } else if (transaction.getTransactionType().equals("Income")) {
                totalIncome += amount;
                totalBalance += amount;
            } else if (transaction.getTransactionType().equals("Loan")) {
                // Handle loan transactions if needed
            }
        }

        totalBalance = totalIncome - totalExpend;

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBalance.setText(currentCurrency + formatter.format(totalBalance));
        tvExpendTotal.setText(currentCurrency + formatter.format(totalExpend));
        tvIncomeTotal.setText(currentCurrency + formatter.format(totalIncome));

        List<MonthlyStatisticsModel> monthlyStatistics = generateMonthlyStatistics();
        adapter = new MonthlyStatisticsAdapter(monthlyStatistics, currentCurrency);
        rvMonthlyStatistics.setAdapter(adapter);
    }

    private List<MonthlyStatisticsModel> generateMonthlyStatistics() {
        List<MonthlyStatisticsModel> result = new ArrayList<>();
        Map<String, MonthlyStatisticsModel> monthlyData = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            MonthlyStatisticsModel model = new MonthlyStatisticsModel();
            model.setMonth(month);
            model.setExpend(0.0);
            model.setIncome(0.0);
            model.setLoan(0.0);
            model.setBorrow(0.0);
            model.setBalance(0.0);
            monthlyData.put(month, model);
        }

        for (TransactionModel transaction : allTransactionList) {
            Date transactionDate = parseTransactionDate(transaction.getDate());
            if (transactionDate == null) continue;

            calendar.setTime(transactionDate);
            int year = calendar.get(Calendar.YEAR);

            if (year == currentYear) {
                int monthIndex = calendar.get(Calendar.MONTH);
                String monthKey = months[monthIndex];
                double amount = Double.parseDouble(transaction.getAmount());

                MonthlyStatisticsModel monthModel = monthlyData.get(monthKey);

                if (transaction.getTransactionType().equals("Expense")) {
                    monthModel.setExpend(monthModel.getExpend() + amount);
                } else if (transaction.getTransactionType().equals("Income")) {
                    monthModel.setIncome(monthModel.getIncome() + amount);
                } else if (transaction.getTransactionType().equals("Loan")) {
                    monthModel.setLoan(monthModel.getLoan() + amount);
                } else if (transaction.getTransactionType().equals("Borrow")) {
                    monthModel.setBorrow(monthModel.getBorrow() + amount);
                }

                monthModel.setBalance((monthModel.getIncome() + monthModel.getLoan()) - (monthModel.getExpend() + monthModel.getBorrow()));
            }
        }

        for (String month : months) {
            result.add(monthlyData.get(month));
        }

        return result;
    }

    private Date parseTransactionDate(String dateString) {
        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MMMM, d yyyy", Locale.US),
                new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateString);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}