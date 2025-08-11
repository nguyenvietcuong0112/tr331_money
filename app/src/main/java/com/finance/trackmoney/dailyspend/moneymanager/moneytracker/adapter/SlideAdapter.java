package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.R;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.databinding.ItemSlideLayoutBinding;


public class SlideAdapter extends PagerAdapter {

    private final int[] images = {R.drawable.intro1, R.drawable.intro2, R.drawable.intro3,R.drawable.intro4};
    private final int[] titles = {R.string.note_continue1, R.string.note_continue2, R.string.note_continue3, R.string.note_continue4};
    private final Context context;

    public SlideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ItemSlideLayoutBinding binding = ItemSlideLayoutBinding.inflate(LayoutInflater.from(context), container, false);
        binding.imLogoSlide.setImageResource(images[position]);
        binding.tvTitle.setText(titles[position]);
        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
