package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.LoadAdError;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.util.Admob;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityFullCallback;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.ads.ActivityLoadNativeFullV2;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionModel transaction);
    }

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;
    private final AppCompatActivity context;
    private final OnTransactionClickListener listener;
    private List<Object> items = new ArrayList<>();
    private Map<String, List<TransactionModel>> transactionsByDate = new TreeMap<>(Collections.reverseOrder());
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.US);
    private final SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.US);
    private final SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.US);
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

    public TransactionAdapter(List<TransactionModel> transactionList, AppCompatActivity context, OnTransactionClickListener listener) {
        this.context = context;
        this.listener = listener;
        updateData(transactionList, null);
    }

    public void updateData(List<TransactionModel> transactionList, Map<String, List<TransactionModel>> transactionsByDate) {
        items.clear();

        if (transactionsByDate != null) {
            this.transactionsByDate = new TreeMap<>(Collections.reverseOrder());
            this.transactionsByDate.putAll(transactionsByDate);

            // First, add only the headers to ensure they are in the correct order
            for (String date : this.transactionsByDate.keySet()) {
                items.add(date);
            }
        } else {
            items.addAll(transactionList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_DATE_HEADER;
        } else {
            return TYPE_TRANSACTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view, context, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_DATE_HEADER) {
            DateHeaderViewHolder headerHolder = (DateHeaderViewHolder) holder;
            String dateString = (String) items.get(position);
            headerHolder.bind(dateString);

            double totalAmount = 0;
            if (transactionsByDate.containsKey(dateString)) {
                for (TransactionModel transaction : transactionsByDate.get(dateString)) {
                    if ("Expense".equals(transaction.getTransactionType())) {
                        totalAmount -= Double.parseDouble(transaction.getAmount());
                    } else if ("Income".equals(transaction.getTransactionType())) {
                        totalAmount += Double.parseDouble(transaction.getAmount());
                    }
                }
            }
            headerHolder.setTotalAmount(totalAmount);

            List<TransactionModel> transactions = transactionsByDate.get(dateString);
            if (transactions != null && !transactions.isEmpty()) {
                Collections.sort(transactions, (t1, t2) -> t2.getTime().compareTo(t1.getTime()));

                TransactionItemAdapter nestedAdapter = new TransactionItemAdapter(transactions, context, listener);
                headerHolder.rvTransactions.setAdapter(nestedAdapter);
                headerHolder.rvTransactions.setVisibility(View.VISIBLE);
            } else {
                headerHolder.rvTransactions.setVisibility(View.GONE);
            }
        } else {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            TransactionModel transaction = (TransactionModel) items.get(position);
            transactionHolder.bind(transaction, context);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateNumber, tvDayOfWeek, tvMonthYear, tvTotalAmount;
        RecyclerView rvTransactions;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateNumber = itemView.findViewById(R.id.tv_date_number);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            tvMonthYear = itemView.findViewById(R.id.tv_month_year);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            rvTransactions = itemView.findViewById(R.id.rv_transactions);

            rvTransactions.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvTransactions.setNestedScrollingEnabled(false);
        }

        void bind(String dateString) {
            try {
                Date date = new SimpleDateFormat("EEE, dd MMMM", Locale.US).parse(dateString);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    Calendar now = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));

                    SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.US);
                    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.US);
                    SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

                    tvDateNumber.setText(dayNumberFormat.format(calendar.getTime()) + " ");
                    tvDayOfWeek.setText(dayOfWeekFormat.format(calendar.getTime()) + ", ");
                    tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                tvDateNumber.setText("");
                tvDayOfWeek.setText(dateString);
                tvMonthYear.setText("");
            }
        }

        void setTotalAmount(double amount) {
            String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(itemView.getContext());
            if (currentCurrency.isEmpty()) currentCurrency = "$";
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            String formattedAmount = formatter.format(Math.abs(amount));

            String prefix;
            if (amount < 0) {
                prefix = "-" + currentCurrency + " ";
                tvTotalAmount.setTextColor(Color.parseColor("#FF5757"));
            } else {
                prefix = currentCurrency + " ";
                tvTotalAmount.setTextColor(Color.parseColor("#33CF75"));
            }

            tvTotalAmount.setText(prefix + formattedAmount);
        }
    }

    // Inner adapter for transactions within a date group
    class TransactionItemAdapter extends RecyclerView.Adapter<TransactionViewHolder> {
        private final List<TransactionModel> transactions;
        private final AppCompatActivity context;
        private final OnTransactionClickListener listener;

        TransactionItemAdapter(List<TransactionModel> transactions, AppCompatActivity context, OnTransactionClickListener listener) {
            this.transactions = transactions;
            this.context = context;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view, context, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            holder.bind(transactions.get(position), context);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvTime;
        ImageView imCategory;
        private TransactionModel boundTransaction;
        private final OnTransactionClickListener listener;
        private final AppCompatActivity context;

        TransactionViewHolder(@NonNull View itemView, AppCompatActivity context, OnTransactionClickListener listener) {
            super(itemView);
            this.context = context;
            this.listener = listener;
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
            imCategory = itemView.findViewById(R.id.iv_category);

            itemView.setOnClickListener(v -> {
                if (boundTransaction == null) return;

                // Ad + native full flow, then callback
                if (!SharePreferenceUtils.isOrganic(itemView.getContext())) {
                    Admob.getInstance().loadAndShowInter(context, itemView.getContext().getString(R.string.inter_detail_transaction), 0, 30000, new InterCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            ActivityLoadNativeFullV2.open(context, context.getString(R.string.native_full_inter_detail_transaction), new ActivityFullCallback() {
                                @Override
                                public void onResultFromActivityFull() {
//                                    ActivityLoadNativeFullV2.open(context, context.getString(R.string.native_full_inter_detail_transaction2), new ActivityFullCallback() {
//                                        @Override
//                                        public void onResultFromActivityFull() {
                                            listener.onTransactionClick(boundTransaction);
//                                        }
//                                    });
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            ActivityLoadNativeFullV2.open(context, context.getString(R.string.native_full_inter_detail_transaction), new ActivityFullCallback() {
                                @Override
                                public void onResultFromActivityFull() {
//                                    ActivityLoadNativeFullV2.open(context, context.getString(R.string.native_full_inter_detail_transaction2), new ActivityFullCallback() {
//                                        @Override
//                                        public void onResultFromActivityFull() {
                                            listener.onTransactionClick(boundTransaction);
//                                        }
//                                    });
                                }
                            });
                        }
                    });
                } else {
                    listener.onTransactionClick(boundTransaction);
                }
            });
        }

        void bind(TransactionModel transaction, AppCompatActivity context) {
            this.boundTransaction = transaction;
            tvCategory.setText(transaction.getCategoryName());
            tvTime.setText(transaction.getTime());
            String amountPrefix = "";
            int textColor = 0;

            switch (transaction.getTransactionType()) {
                case "Income":
                    amountPrefix = "+";
                    textColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
                case "Expense":
                    amountPrefix = "-";
                    textColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
                case "Loan":
                    amountPrefix = "~";
                    textColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
            }
            String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(itemView.getContext());
            if (currentCurrency.isEmpty()) currentCurrency = "$";
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);

            String amountText = amountPrefix + formatter.format(Double.parseDouble(transaction.getAmount())) + currentCurrency;
            tvAmount.setText(amountText);
            tvAmount.setTextColor(textColor);
            imCategory.setBackgroundResource(transaction.getCategoryIcon());
        }
    }
}
