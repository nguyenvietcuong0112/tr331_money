package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.base.BaseActivity;
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

public class SeeAllActivity extends BaseActivity implements TransactionAdapter.OnTransactionClickListener {

    private TextView tvTotalBalance, tvSelectedMonth, tvTotalExpenditure, tvTotalLabel;
    private LinearLayout llExpend, llIncome, llLoan, headerTotal;
    private RecyclerView rvTransactions;
    private TransactionAdapter regularAdapter;
    private LoanTransactionAdapter loanAdapter;

    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private List<TransactionModel> filteredTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expense";
    private String currentMonth = "";
    private Map<String, List<TransactionModel>> transactionsByDate = new HashMap<>();
    String currentCurrency;
    ImageView ivEditBalance, ivBack;

    String totalAmount;
    LinearLayout llBanner;
    private LinearLayout noDataView;

    boolean isBalanceVisible = true;
    private boolean isFirstLoad = true;

    FrameLayout frAdsHomeTop;
    FrameLayout frAdsCollap;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedLoadExpandTask;

    @Override
    public void bind() {
        setContentView(R.layout.seeall_activity);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        initViews();
        loadTransactionData();
        setupClickListeners();
        setupInitialFilter();


    }

    private void initViews() {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        ivBack = findViewById(R.id.iv_back);
        headerTotal = findViewById(R.id.header_total);
        tvTotalLabel = findViewById(R.id.tv_total_label);
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvSelectedMonth = findViewById(R.id.tv_selected_month);
        tvTotalExpenditure = findViewById(R.id.tv_total_expenditure);
        llExpend = findViewById(R.id.ll_expend);
        llIncome = findViewById(R.id.ll_income);
        llLoan = findViewById(R.id.ll_loan);
        rvTransactions = findViewById(R.id.rv_transactions);
        ivEditBalance = findViewById(R.id.iv_edit_balance);
        noDataView = findViewById(R.id.layout_no_data);
        llBanner = findViewById(R.id.ll_banner);
        frAdsHomeTop = findViewById(R.id.frAdsHomeTop);
        frAdsCollap = findViewById(R.id.frAdsCollap);
        regularAdapter = new TransactionAdapter(filteredTransactionList, this, this);
        loanAdapter = new LoanTransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(regularAdapter);

        tvTotalExpenditure.setText(currentCurrency);

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
            rvTransactions.setVisibility(View.VISIBLE);
            noDataView.setVisibility(View.GONE);
        } else {
            rvTransactions.setVisibility(View.GONE);
            noDataView.setVisibility(View.VISIBLE);
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
        ivBack.setOnClickListener(v -> {
           onBackPressed();
        });
        llExpend.setOnClickListener(v -> {
            currentTransactionType = "Expense";
            updateTransactionTypeUI();
            filterTransactions();
        });

        llIncome.setOnClickListener(v -> {
            currentTransactionType = "Income";
            updateTransactionTypeUI();
            filterTransactions();
        });

        llLoan.setOnClickListener(v -> {
            currentTransactionType = "Loan";
            updateTransactionTypeUI();
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

        updateTransactionTypeUI();
        filterTransactions();
    }

    private void updateTransactionTypeUI() {
        // Get references to indicators

        View indicatorLoan = llLoan.findViewById(R.id.indicator_loan);

        indicatorLoan.setVisibility(currentTransactionType.equals("Loan") ? View.VISIBLE : View.INVISIBLE);

        TextView tvExpendLabel = llExpend.findViewById(R.id.tv_expend_label);
        TextView tvIncomeLabel = llIncome.findViewById(R.id.tv_income_label);
        ImageView ivLoan = llLoan.findViewById(R.id.iv_loan);
        TextView tvLoanLabel = llLoan.findViewById(R.id.tv_loan_label);

        int colorActive = getResources().getColor(android.R.color.white);
        int colorInactive = getResources().getColor(R.color.icon_inactive);

        tvExpendLabel.setTextColor(currentTransactionType.equals("Expense") ? colorActive : colorInactive);

        tvIncomeLabel.setTextColor(currentTransactionType.equals("Income") ? colorActive : colorInactive);

        ivLoan.setColorFilter(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
    }

    private void filterTransactions() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM, d yyyy", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        SimpleDateFormat alternateInputFormat = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());

        filteredTransactionList.clear();
        transactionsByDate.clear();

        double totalAmount = 0;

        if ("Loan".equals(currentTransactionType)) {
            rvTransactions.setAdapter(loanAdapter);
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
            rvTransactions.setAdapter(regularAdapter);
            headerTotal.setVisibility(View.VISIBLE);

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
                llIncome.setBackgroundResource(R.drawable.bg_tab_home);
                llExpend.setBackgroundResource(R.drawable.bg_tab_home_trans);
                break;
            case "Expense":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalExpenditure));
                tvTotalExpenditure.setTextColor(Color.parseColor("#F04438"));
                llExpend.setBackgroundResource(R.drawable.bg_tab_home);
                llIncome.setBackgroundResource(R.drawable.bg_tab_home_trans);
                break;
            case "Loan":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalLoan));
                break;
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
}