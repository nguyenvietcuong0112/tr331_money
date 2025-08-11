package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter;

import static com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils.getSelectedCurrencyCode;
import static com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils.saveSelectedCurrencyCode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ItemCurencyUnitBinding;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.NativeCurencyInlineBinding;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.CurrencyUnitModel;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class CurrencyUnitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_AD = 0;
    private static final int ITEM_TYPE_CURRENCY = 1;

    private Activity context;
    private List<CurrencyUnitModel> lists;
    private List<CurrencyUnitModel> listsFiltered;
    private AdView adView;

    private IClickCurrencyUnit iClickCurrencyUnit;
    private int selectedPosition = RecyclerView.NO_POSITION;


    public interface IClickCurrencyUnit {
        void onClick(CurrencyUnitModel model);
        void onStartLoading(); // Bắt đầu loading

    }

    public CurrencyUnitAdapter(Activity context, List<CurrencyUnitModel> lists, IClickCurrencyUnit iClickCurrencyUnit, AdView adView) {
        this.context = context;
        this.lists = lists;
        this.iClickCurrencyUnit = iClickCurrencyUnit;
        this.listsFiltered = new ArrayList<>(lists);
        this.adView = adView;


        String savedCode = getSelectedCurrencyCode(context);
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getCode().equals(savedCode)) {
                selectedPosition = i;
                break;
            }
        }
    }

    public void filter(String query) {
        listsFiltered.clear();

        if (query.isEmpty()) {
            listsFiltered.addAll(lists);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();

            for (CurrencyUnitModel currency : lists) {
                // Search by code, name or country name
                if (currency.getCode().toLowerCase().contains(lowerCaseQuery) ||
                        currency.getLanguageName().toLowerCase().contains(lowerCaseQuery)) {
                    listsFiltered.add(currency);
                }
            }
        }

        // Reset selected position for filtered list
        selectedPosition = RecyclerView.NO_POSITION;
        String savedCode = getSelectedCurrencyCode(context);
        for (int i = 0; i < listsFiltered.size(); i++) {
            if (listsFiltered.get(i).getCode().equals(savedCode)) {
                selectedPosition = i;
                break;
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 1) {
            return ITEM_TYPE_AD;
        }
        return ITEM_TYPE_CURRENCY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_AD) {
            NativeCurencyInlineBinding binding = NativeCurencyInlineBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new AdViewHolder(binding);
        } else {
            ItemCurencyUnitBinding binding = ItemCurencyUnitBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new CurrencyUnitViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AdViewHolder) {
            ((AdViewHolder) holder).bind(context, adView);
        } else if (holder instanceof CurrencyUnitViewHolder) {
            int dataPosition;
            if (position > 1) {
                dataPosition = position - 1;
            } else {
                dataPosition = position;
            }

            if (dataPosition < listsFiltered.size()) {
                CurrencyUnitModel data = listsFiltered.get(dataPosition);
                CurrencyUnitViewHolder currencyHolder = (CurrencyUnitViewHolder) holder;
                currencyHolder.bind(data, dataPosition == selectedPosition);

                currencyHolder.binding.rlItem.setOnClickListener(view -> {
                    if (iClickCurrencyUnit != null) {
                        iClickCurrencyUnit.onStartLoading();
                    }

                    view.postDelayed(() -> {
                        int previousPosition = selectedPosition;
                        selectedPosition = dataPosition;

                        saveSelectedCurrencyCode(context, data.getSymbol());
                        notifyCurrencyChanged(context);

                        // Update UI
                        if (previousPosition != RecyclerView.NO_POSITION) {
                            notifyItemChanged(getPositionInAdapter(previousPosition));
                        }
                        notifyItemChanged(getPositionInAdapter(selectedPosition));

                        if (iClickCurrencyUnit != null) {
                            iClickCurrencyUnit.onClick(data);
                        }
                    }, 500);
                });
            }
        }
    }

    // Chuyển đổi từ vị trí dữ liệu sang vị trí trong adapter (tính cả quảng cáo)
    private int getPositionInAdapter(int dataPosition) {
        if (dataPosition >= 1) {
            return dataPosition + 1; // Cộng 1 để bỏ qua vị trí quảng cáo
        }
        return dataPosition;
    }

    private void notifyCurrencyChanged(Context context) {
        Intent intent = new Intent("CURRENCY_CHANGED");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        // +1 cho quảng cáo
        return listsFiltered.size() + 1;
    }

    public class CurrencyUnitViewHolder extends RecyclerView.ViewHolder {
        final ItemCurencyUnitBinding binding;

        public CurrencyUnitViewHolder(ItemCurencyUnitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CurrencyUnitModel data, boolean isSelected) {
            binding.ivAvatar.setImageDrawable(ContextCompat.getDrawable(binding.getRoot().getContext(), data.getImage()));
            binding.tvName.setText(data.getLanguageName());
            binding.tvCode.setText(data.getCode());
            binding.v2.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            binding.rlItem.setBackground(isSelected ? ContextCompat.getDrawable(context, R.drawable.bg_item_currency_true) :
                    ContextCompat.getDrawable(context, R.drawable.bg_item_currency));
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {
        private final NativeCurencyInlineBinding binding;

        public AdViewHolder(NativeCurencyInlineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Activity activityContext, AdView adView) {
            if (!SharePreferenceUtils.isOrganic(activityContext) && adView != null) {
                binding.adCardView.removeAllViews();
                binding.adCardView.addView(adView);
            } else {
                binding.adCardView.setVisibility(View.GONE);
            }
        }
    }
}