package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.CustomBottomSheetDialogExitFragment;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.SeeAllActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.TrackAllYourSpendingActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.TransactionDetailActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityFullCallback;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityLoadNativeFullV2;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.RoundedPieChartView;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.TransactionUpdateEvent;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter.LoanTransactionAdapter;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter.TransactionAdapter;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HomeActivity extends BaseActivity implements TransactionAdapter.OnTransactionClickListener {

    private TextView tvTotalBalance, tvSelectedMonth, tvTotalExpenditure, tvTotalLabel, tvExpendTab, tvIncomeTab,tvSeeAll;
    private LinearLayout llExpend, llIncome, headerTotal;
    private TransactionAdapter regularAdapter;
    private LoanTransactionAdapter loanAdapter;
    private LinearLayout llTrackAll, llFinancial, llStatistic, llSetting;

//    private PieChart pieChart;

    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private List<TransactionModel> filteredTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expense";
    private String currentMonth = "";
    private Map<String, List<TransactionModel>> transactionsByDate = new HashMap<>();
    String currentCurrency;
    ImageView ivEditBalance;
    private static final int ADD_TRANSACTION_REQUEST = 1;

    ImageView ivIncome, ivExpend;
    String totalAmount;
    LinearLayout llBanner;
    private LinearLayout noDataView;

    boolean isBalanceVisible = true;
    private boolean isFirstLoad = true;

    private RoundedPieChartView customPieChart;
    private FrameLayout frAdsBanner;


    FrameLayout frAdsHomeTop;
    FrameLayout frAdsCollap;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedLoadExpandTask;

    @Override
    public void bind() {
        setContentView(R.layout.fragment_home);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initViews();
        setOnClickListener();
        loadTransactionData();
        setupClickListeners();
        setupInitialFilter();



        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                CustomBottomSheetDialogExitFragment dialog = CustomBottomSheetDialogExitFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "ExitDialog");
            }
        });

//        loadAds();

    }

    private void initViews() {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";
        customPieChart = findViewById(R.id.pieChart); // ID trong XML

        llTrackAll = findViewById(R.id.llTrackAll);
        llFinancial = findViewById(R.id.llFinancial);
        llSetting = findViewById(R.id.llSetting);
        llStatistic = findViewById(R.id.llStatistic);
        tvSeeAll = findViewById(R.id.tvSeeAll);
//        pieChart = findViewById(R.id.pieChart);
        frAdsBanner = findViewById(R.id.fr_ads_banner);

        frAdsHomeTop = findViewById(R.id.frAdsHomeTop);
        frAdsCollap = findViewById(R.id.frAdsCollap);
        headerTotal = findViewById(R.id.header_total);
        tvTotalLabel = findViewById(R.id.tv_total_label);
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvSelectedMonth = findViewById(R.id.tv_selected_month);
        tvTotalExpenditure = findViewById(R.id.tv_total_expenditure);
        tvExpendTab = findViewById(R.id.tv_expend_tab);
        tvIncomeTab = findViewById(R.id.tv_income_tab);
        llExpend = findViewById(R.id.ll_expend);
        llIncome = findViewById(R.id.ll_income);
        ivEditBalance = findViewById(R.id.iv_edit_balance);
        noDataView = findViewById(R.id.layout_no_data);
        llBanner = findViewById(R.id.ll_banner);
        frAdsHomeTop = findViewById(R.id.frAdsHomeTop);
        frAdsCollap = findViewById(R.id.frAdsCollap);
        regularAdapter = new TransactionAdapter(filteredTransactionList, this, this);
        loanAdapter = new LoanTransactionAdapter();


        tvTotalExpenditure.setText(currentCurrency);

    }

    private void updatePieChart(float income, float expense) {
        if (customPieChart == null) return;

        // Kiểm tra nếu cả income và expense đều bằng 0
        if (income == 0 && expense == 0) {
            customPieChart.setData(0, 0);
            customPieChart.setCenterText("No data\navailable");
            return;
        }

        // Thiết lập dữ liệu
        customPieChart.setData(income, expense);

        // Thiết lập màu sắc
        int[] colors = {
                Color.parseColor("#10B981"), // Green for Income
                Color.parseColor("#EF4444")  // Red for Expense
        };
        customPieChart.setColors(colors);

        // Thiết lập text ở giữa (tùy chọn)
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String centerText = "Balance\n" + currentCurrency + formatter.format(income - expense);
        customPieChart.setCenterText("");
    }



    private void setOnClickListener() {
        llTrackAll.setOnClickListener(view -> {
            Intent intent = new Intent(this, TrackAllYourSpendingActivity.class);
            showInterHome(
                    intent,
                    getString(R.string.inter_home),
                    getString(R.string.native_full_home),
                    false,
                    0
            );
        });
        llStatistic.setOnClickListener(view -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            showInterHome(
                    intent,
                    getString(R.string.inter_home),
                    getString(R.string.native_full_home),
                    false,
                    0
            );
        });
        llFinancial.setOnClickListener(view -> {
            List<TransactionModel> all = SharePreferenceUtils.getInstance(this).getTransactionList();
            if (all == null) all = new ArrayList<>();
            String json = new Gson().toJson(all);

            Intent intent = new Intent(this, BudgetActivity.class);
            intent.putExtra("transactionList", json);

            showInterHome(
                    intent,
                    getString(R.string.inter_home),
                    getString(R.string.native_full_home),
                    false,
                    0
            );
        });
        llSetting.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            showInterHome(
                    intent,
                    getString(R.string.inter_home),
                    getString(R.string.native_full_home),
                    true,
                    999
            );
        });
        tvSeeAll.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SeeAllActivity.class);
            startActivity(intent);
        });

    }

    public  void showInterHome(Intent targetIntent, String interAdId, String nativeAdId, boolean useStartForResult, int requestCode) {
        if (!SharePreferenceUtils.isOrganic(HomeActivity.this)) {
            Admob.getInstance().loadAndShowInter(HomeActivity.this,
                    interAdId,
                    0,
                    30000,
                    new InterCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            ActivityLoadNativeFullV2.open(HomeActivity.this, nativeAdId, new ActivityFullCallback() {
                                @Override
                                public void onResultFromActivityFull() {
                                    if (useStartForResult) {
                                        HomeActivity.this.startActivityForResult(targetIntent, requestCode);
                                    } else {
                                        HomeActivity.this.startActivity(targetIntent);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            ActivityLoadNativeFullV2.open(HomeActivity.this, nativeAdId, new ActivityFullCallback() {
                                @Override
                                public void onResultFromActivityFull() {
                                    if (useStartForResult) {
                                        HomeActivity.this.startActivityForResult(targetIntent, requestCode);
                                    } else {
                                        HomeActivity.this.startActivity(targetIntent);
                                    }
                                }
                            });
                        }
                    });
        } else {
            if (useStartForResult) {
                HomeActivity.this.startActivityForResult(targetIntent, requestCode);
            } else {
                HomeActivity.this.startActivity(targetIntent);
            }
        }
    }


    private void checkEmptyState() {
        boolean hasTransactions = false;

        for (TransactionModel transaction : allTransactionList) {
            if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                continue;
            }

            try {
                Date transactionDate = parseTransactionDate(transaction.getDate());
                if (transactionDate != null) {
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                    String transactionMonth = outputDateFormat.format(transactionDate);

                    if (transaction.getTransactionType().equals(currentTransactionType) &&
                            transactionMonth.equals(currentMonth)) {
                        hasTransactions = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (hasTransactions) {
            noDataView.setVisibility(View.GONE);
        } else {
            noDataView.setVisibility(View.GONE);
        }
    }


    private void loadTransactionData() {
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("transactionList")) {
            return;
        }

        String transactionListJson = intent.getStringExtra("transactionList");
        if (transactionListJson == null || transactionListJson.isEmpty()) {
            return;
        }

        Type type = new TypeToken<List<TransactionModel>>() {
        }.getType();
        allTransactionList = new Gson().fromJson(transactionListJson, type);

        if (allTransactionList == null) {
            allTransactionList = new ArrayList<>();
        }
    }

    private void setupClickListeners() {
        llExpend.setOnClickListener(v -> {
            currentTransactionType = "Expense";
            filterTransactions();
        });

        llIncome.setOnClickListener(v -> {
            currentTransactionType = "Income";
            filterTransactions();
        });


        tvSelectedMonth.setOnClickListener(v -> {
            showMonthPickerDialog();
        });

        ivEditBalance.setOnClickListener(view -> {
            if (isBalanceVisible) {
                tvTotalBalance.setText("******");
                ivEditBalance.setImageResource(R.drawable.ic_visibility);
            } else {
                tvTotalBalance.setText(totalAmount);
                ivEditBalance.setImageResource(R.drawable.ic_visibility_off);
            }
            isBalanceVisible = !isBalanceVisible;
        });
    }

    private void setupInitialFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        currentMonth = dateFormat.format(calendar.getTime());
        tvSelectedMonth.setText(currentMonth);

        filterTransactions();
    }

    private void updateTransactionTypeUI() {
        // Get references to indicators

        ivExpend = llExpend.findViewById(R.id.iv_expend);
        ivIncome = llIncome.findViewById(R.id.iv_income);


        int colorActive = getResources().getColor(android.R.color.white);
        int colorInactive = getResources().getColor(R.color.icon_inactive);

        ivExpend.setColorFilter(currentTransactionType.equals("Expense") ? colorActive : colorInactive);

        ivIncome.setColorFilter(currentTransactionType.equals("Income") ? colorActive : colorInactive);

    }

    private void filterTransactions() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM, d yyyy", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        SimpleDateFormat alternateInputFormat = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());

        filteredTransactionList.clear();
        transactionsByDate.clear();

        double totalAmount = 0;

        if ("Loan".equals(currentTransactionType)) {
            headerTotal.setVisibility(View.GONE);

            List<TransactionModel> loanTransactions = new ArrayList<>();
            for (TransactionModel transaction : allTransactionList) {
                if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                    continue;
                }

                try {
                    Date transactionDate;
                    String dateStr = transaction.getDate();

                    try {
                        transactionDate = inputDateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        try {
                            transactionDate = alternateInputFormat.parse(dateStr);
                        } catch (ParseException e2) {
                            System.err.println("Could not parse date: " + dateStr);
                            continue;
                        }
                    }

                    String transactionMonth = outputDateFormat.format(transactionDate);

                    if (transaction.getTransactionType().equals(currentTransactionType) &&
                            transactionMonth.equals(currentMonth)) {
                        loanTransactions.add(transaction);

                        try {
                            double amount = Double.parseDouble(transaction.getAmount());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            loanAdapter.updateData(loanTransactions);
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvTotalExpenditure.setText(currentCurrency + formatter.format(totalAmount));

        } else {
            headerTotal.setVisibility(View.GONE);

            if ("Expense".equals(currentTransactionType)) {
                tvTotalLabel.setText("Total expenditure");
            } else {
                tvTotalLabel.setText("Total income");

            }

            for (TransactionModel transaction : allTransactionList) {
                if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                    continue;
                }

                try {
                    Date transactionDate;
                    String dateStr = transaction.getDate();

                    try {
                        transactionDate = inputDateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        try {
                            transactionDate = alternateInputFormat.parse(dateStr);
                        } catch (ParseException e2) {
                            System.err.println("Could not parse date: " + dateStr);
                            continue;
                        }
                    }

                    String transactionMonth = outputDateFormat.format(transactionDate);

                    if (transaction.getTransactionType().equals(currentTransactionType) &&
                            transactionMonth.equals(currentMonth)) {

                        filteredTransactionList.add(transaction);

                        String dayKey = new SimpleDateFormat("EEE, dd MMMM", Locale.US).format(transactionDate);
                        if (!transactionsByDate.containsKey(dayKey)) {
                            transactionsByDate.put(dayKey, new ArrayList<>());
                        }
                        transactionsByDate.get(dayKey).add(transaction);

                        try {
                            double amount = Double.parseDouble(transaction.getAmount());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            regularAdapter.updateData(filteredTransactionList, transactionsByDate);
            updateTotalAmount();
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvTotalExpenditure.setText(currentCurrency + formatter.format(totalAmount));
        }
        updateTotalAmount();
        checkEmptyState();

    }

    private void updateTotalAmount() {
        double totalBalance = 0;
        double totalExpenditure = 0;
        double totalIncome = 0;
        double totalLoan = 0;

        for (TransactionModel transaction : allTransactionList) {
            try {
                double amount = Double.parseDouble(transaction.getAmount());

                if (transaction.getTransactionType().equals("Income")) {
                    totalBalance += amount;
                } else if (transaction.getTransactionType().equals("Expense")) {
                    totalBalance -= amount;
                }
                if (transaction.getDate() != null && !transaction.getDate().trim().isEmpty()) {
                    try {
                        Date transactionDate = parseTransactionDate(transaction.getDate());
                        if (transactionDate != null) {
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                            String transactionMonth = outputDateFormat.format(transactionDate);

                            if (transactionMonth.equals(currentMonth)) {
                                switch (transaction.getTransactionType()) {
                                    case "Income":
                                        totalIncome += amount;
                                        break;
                                    case "Expense":
                                        totalExpenditure += amount;
                                        break;
                                    case "Loan":
                                        totalLoan += amount;
                                        break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBalance.setText(formatter.format(totalBalance) + " " + currentCurrency);
        totalAmount = currentCurrency + formatter.format(totalBalance);
        switch (currentTransactionType) {
            case "Income":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalIncome));
                tvTotalExpenditure.setTextColor(Color.parseColor("#1DEB7C"));
                tvIncomeTab.setText(formatter.format(totalIncome) + " " + currentCurrency);
                tvExpendTab.setText(formatter.format(totalExpenditure) + " " + currentCurrency);


                break;
            case "Expense":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalExpenditure));
                tvTotalExpenditure.setTextColor(Color.parseColor("#F04438"));
                tvExpendTab.setText(formatter.format(totalExpenditure) + " " + currentCurrency);
                tvIncomeTab.setText(formatter.format(totalIncome) + " " + currentCurrency);

                break;
            case "Loan":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalLoan));
                break;
        }

        try {
            String incomeStr = tvIncomeTab.getText().toString().replaceAll("[^\\d.]", "");
            String expenseStr = tvExpendTab.getText().toString().replaceAll("[^\\d.]", "");

            float incomeValue = incomeStr.isEmpty() ? 0 : Float.parseFloat(incomeStr);
            float expenseValue = expenseStr.isEmpty() ? 0 : Float.parseFloat(expenseStr);

            updatePieChart(incomeValue, expenseValue);
        } catch (Exception e) {
            e.printStackTrace();
            updatePieChart(0, 0);
        }
    }

    private Date parseTransactionDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MMMM, d yyyy", Locale.US),
                new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateString);
            } catch (ParseException e) {
            }
        }

        System.err.println("Could not parse date with any format: " + dateString);
        return null;
    }

    private void showMonthPickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.picker_month_dialog);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        TextView yearText = dialog.findViewById(R.id.tv_year);
        yearText.setText(String.valueOf(currentYear));

        String currentSelectedMonth = tvSelectedMonth.getText().toString();
        String[] currentParts = currentSelectedMonth.split(" ");
        if (currentParts.length > 0) {
            yearText.setText(currentParts[1]);
        }

        ImageButton prevYear = dialog.findViewById(R.id.btn_previous_year);
        ImageButton nextYear = dialog.findViewById(R.id.btn_next_year);

        prevYear.setOnClickListener(v -> {
            int year = Integer.parseInt(yearText.getText().toString()) - 1;
            yearText.setText(String.valueOf(year));
        });

        nextYear.setOnClickListener(v -> {
            int year = Integer.parseInt(yearText.getText().toString()) + 1;
            yearText.setText(String.valueOf(year));
        });

        int[] monthIds = {
                R.id.month_jan, R.id.month_feb, R.id.month_mar, R.id.month_apr,
                R.id.month_may, R.id.month_jun, R.id.month_jul, R.id.month_aug,
                R.id.month_sep, R.id.month_oct, R.id.month_nov, R.id.month_dec
        };

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        final TextView[] selectedMonthHolder = new TextView[1];

        String currentMonthAbbrev = currentSelectedMonth.split(" ")[0].substring(0, 3);
        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);
            if (monthView.getText().toString().equals(currentMonthAbbrev)) {
                monthView.setBackgroundResource(R.drawable.bg_selected_month);
                monthView.setTextColor(getResources().getColor(android.R.color.white));
                selectedMonthHolder[0] = monthView;
            }
        }

        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);

            monthView.setOnClickListener(v -> {
                for (int id : monthIds) {
                    dialog.findViewById(id).setBackgroundResource(android.R.color.transparent);
                    ((TextView) dialog.findViewById(id)).setTextColor(getResources().getColor(android.R.color.black));
                }

                v.setBackgroundResource(R.drawable.bg_selected_month);
                ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));

                selectedMonthHolder[0] = (TextView) v;
            });
        }

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (selectedMonthHolder[0] != null) {
                String month = selectedMonthHolder[0].getText().toString();
                String year = yearText.getText().toString();
                String fullMonthName = months[Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").indexOf(month)];
                currentMonth = fullMonthName + " " + year;
                tvSelectedMonth.setText(currentMonth);
                filterTransactions();
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = width;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
        allTransactionList = preferenceUtils.getTransactionList();
        filterTransactions();
        updateTotalAmount();
        regularAdapter.notifyDataSetChanged();
        loanAdapter.notifyDataSetChanged();

        loadAdsBanner();
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


    private void loadAdsBanner() {

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
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


    private void loadNativeCollap(@Nullable final Runnable onLoaded) {

        Log.d("Truowng", "loadNativeCollapA: ");
        if (frAdsHomeTop != null) {
            frAdsHomeTop.removeAllViews();
        }

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_collap_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {

                NativeAdView adView = (NativeAdView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_native_home_collap, null);
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
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_expand_home), new NativeCallback() {

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_native_home_expnad, null);
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



    @Override
    public void onPause() {
        super.onPause();
        if (delayedLoadExpandTask != null) {
            handler.removeCallbacks(delayedLoadExpandTask);
            delayedLoadExpandTask = null;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
        filterTransactions();
        updateTotalAmount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            int deletedPosition = data.getIntExtra("deleted_position", -1);
            if (deletedPosition != -1) {
                SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
                List<TransactionModel> allTransactions = preferenceUtils.getTransactionList();

                for (int i = 0; i < filteredTransactionList.size(); i++) {
                    TransactionModel currentTransaction = filteredTransactionList.get(i);
                    boolean stillExists = false;
                    for (TransactionModel t : allTransactions) {
                        if (isSameTransaction(currentTransaction, t)) {
                            stillExists = true;
                            break;
                        }
                    }
                    if (!stillExists) {
                        filteredTransactionList.remove(i);
                        break;
                    }
                }
            }
            filterTransactions();
            updateTotalAmount();
            regularAdapter.notifyDataSetChanged();
            loanAdapter.notifyDataSetChanged();
        }
        if (requestCode == 999 && resultCode == RESULT_OK && data != null) {
            boolean updated = data.getBooleanExtra("currency_updated", false);
            if (updated) {
                // 🔥 Load lại currency
                currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
                if (currentCurrency.isEmpty()) currentCurrency = "$";
                updateTotalAmount(); // nếu bạn muốn cập nhật lại currency trong UI
            }
        }
    }

    private boolean isSameTransaction(TransactionModel t1, TransactionModel t2) {
        return t1.getDate().equals(t2.getDate())
                && t1.getAmount().equals(t2.getAmount())
                && t1.getCategoryName().equals(t2.getCategoryName())
                && t1.getTransactionType().equals(t2.getTransactionType())
                && t1.getTime().equals(t2.getTime());
    }


    @Override
    public void onTransactionClick(TransactionModel transaction) {
        Intent intent = new Intent(this, TransactionDetailActivity.class);
        // pass whatever you need; e.g., serialize the transaction or pass an ID
        intent.putExtra("transaction_json", new Gson().toJson(transaction));
        startActivityForResult(intent, 1001);
    }

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
}