package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.activity.BudgetDetailActivity;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.BudgetManager;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.CircularProgressView;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.TransactionUpdateEvent;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter.BudgetAdapter;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.BudgetItem;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity implements BudgetAdapter.BudgetItemListener {
    private BudgetManager budgetManager;
    private CircularProgressView mainProgressView;
    private TextView tvTotalBudget, tvExpenses;
    private LinearLayout btnBudgetDetail;
    private List<TransactionModel> allTransactionList;
    ImageView ivEditBalance;
    String currentCurrency;
    LinearLayout llBanner;

    private boolean isFirstLoad = true;

    FrameLayout frAdsHomeTop;
    FrameLayout frAdsCollap;

    ImageView ivBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_budget);

        initializeViews();
        setupBudgetManager();
        loadTransactionData();
        setupListeners();
        updateUI();
    }

    private void initializeViews() {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        mainProgressView = findViewById(R.id.main_progress_view);
        tvTotalBudget = findViewById(R.id.tv_total_budget);
        ivEditBalance = findViewById(R.id.iv_edit_balance);
        tvExpenses = findViewById(R.id.tv_expenses);
        btnBudgetDetail = findViewById(R.id.btn_budget_detail);
        llBanner = findViewById(R.id.ll_banner);
        frAdsHomeTop = findViewById(R.id.frAdsHomeTop);
        frAdsCollap = findViewById(R.id.frAdsCollap);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupBudgetManager() {
        budgetManager = new BudgetManager(this);
        if (budgetManager.getTotalBudget() == 0) {
            budgetManager.setTotalBudget(0);
        }
    }

    private void setupListeners() {
        View.OnClickListener editBudgetListener = v -> showEditBudgetDialog();
        ivEditBalance.setOnClickListener(editBudgetListener);

        btnBudgetDetail.setOnClickListener(v -> {
            Intent intent = new Intent(this, BudgetDetailActivity.class);
            startActivity(intent);
        });
        ivBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void loadTransactionData() {
        if (getIntent() == null || !getIntent().hasExtra("transactionList")) {
            return;
        }

        String transactionListJson = getIntent().getStringExtra("transactionList");
        if (transactionListJson == null || transactionListJson.isEmpty()) {
            return;
        }

        Type type = new TypeToken<List<TransactionModel>>() {}.getType();
        allTransactionList = new Gson().fromJson(transactionListJson, type);

        if (allTransactionList == null) {
            allTransactionList = new ArrayList<>();
        }

        calculateAndUpdateTotalExpenses();
    }

    private void calculateAndUpdateTotalExpenses() {
        if (allTransactionList == null || allTransactionList.isEmpty()) {
            budgetManager.setTotalExpenses(0);
            updateUI();
            return;
        }

        double totalExpenseAmount = 0.0;
        for (TransactionModel transaction : allTransactionList) {
            if ("Expense".equals(transaction.getTransactionType())) {
                totalExpenseAmount += Double.parseDouble(transaction.getAmount());
            }
        }

        budgetManager.setTotalExpenses(totalExpenseAmount);
        updateUI();
    }

    private void showEditBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);

        EditText inputBudget = dialogView.findViewById(R.id.input_budget);
        inputBudget.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }

                try {
                    isUpdating = true;

                    String str = s.toString();
                    if (str.equals(current)) {
                        isUpdating = false;
                        return;
                    }

                    String cleanString = str.replaceAll("[,]", "");

                    if (cleanString.isEmpty()) {
                        inputBudget.setText("");
                        isUpdating = false;
                        return;
                    }

                    long parsed = Long.parseLong(cleanString);
                    String formatted = NumberFormat.getNumberInstance(Locale.US).format(parsed);

                    current = formatted;
                    inputBudget.setText(formatted);
                    inputBudget.setSelection(formatted.length());

                } catch (NumberFormatException e) {
                } finally {
                    isUpdating = false;
                }
            }
        });
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newBudget = inputBudget.getText().toString().replaceAll(",", "");
            if (!newBudget.isEmpty()) {
                double budgetAmount = Double.parseDouble(newBudget);
                budgetManager.setTotalBudget(budgetAmount);
                updateUI();
                dialog.dismiss();
            }
        });
    }

    private void updateUI() {
        double totalBudget = budgetManager.getTotalBudget();
        double totalExpenses = budgetManager.getTotalExpenses();
        double remaining = totalBudget - totalExpenses;

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBudget.setText(formatter.format(totalBudget) + " " + currentCurrency);
        tvExpenses.setText(formatter.format(totalExpenses) + " " + currentCurrency);

        int progress = totalBudget > 0 ? (int) ((remaining / totalBudget) * 100) : 0;
        progress = Math.max(0, Math.min(100, progress));

        mainProgressView.setProgress(progress);
        mainProgressView.setShowRemainingText(true);
    }

    @Override
    public void onBudgetItemClick(BudgetItem item) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
        calculateAndUpdateTotalExpenses(); // thêm dòng này

    }

    @Override
    protected void onResume() {
        super.onResume();


    }



}