package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.CategoryItem;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryItem> categories;
    private Context context;
    private int selectedPosition = -1;
    private OnCategoryClickListener listener;

    public void setSelectedPosition(int position) {
        int oldSelectedPosition = selectedPosition;
        selectedPosition = position;

        if (oldSelectedPosition != -1) {
            notifyItemChanged(oldSelectedPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }
    public int getSelectedPosition() {
        return selectedPosition;
    }
    public CategoryItem getSelectedCategory() {
        if (selectedPosition != -1 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return null;
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem category, int position);
    }

    public CategoryAdapter(Context context, List<CategoryItem> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = categories.get(position);
        holder.ivIcon.setImageResource(item.getIconResource());
        holder.tvName.setText(item.getName());

        holder.itemView.setBackgroundResource(selectedPosition == position ?
                R.drawable.bg_category_selected : R.drawable.bg_category_normal);

        holder.itemView.setBackgroundResource(selectedPosition == position ?
                R.drawable.bg_category_selected : R.drawable.bg_category_normal);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;

            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(item, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
    public void updateCategories(List<CategoryItem> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}