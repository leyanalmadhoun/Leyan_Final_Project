package com.example.leyan_final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 pager;
    private LinearLayout indicators;
    private TextView tvSkip;

    private final List<OnboardingPage> pages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager = findViewById(R.id.pagerOnboarding);
        indicators = findViewById(R.id.indicators);
        tvSkip = findViewById(R.id.tvSkip);

        tvSkip.setOnClickListener(v -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit().putBoolean("onboarding_done", true).apply();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        pages.clear();
        pages.add(new OnboardingPage(R.drawable.onboarding1, R.string.onb_title_1, R.string.onb_sub_1));
        pages.add(new OnboardingPage(R.drawable.onboarding2, R.string.onb_title_2, R.string.onb_sub_2));
        pages.add(new OnboardingPage(R.drawable.onboarding3, R.string.onb_title_3, R.string.onb_sub_3));

        pager.setAdapter(new Adapter());
        setupIndicators();
        setCurrentIndicator(0);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { setCurrentIndicator(position); }
        });
    }

    private void setupIndicators() {
        indicators.removeAllViews();
        ImageView[] dots = new ImageView[pages.size()];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_inactive);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMargins(8, 0, 8, 0);
            indicators.addView(dots[i], p);
        }
    }

    private void setCurrentIndicator(int index) {
        int n = indicators.getChildCount();
        for (int i = 0; i < n; i++) {
            ImageView img = (ImageView) indicators.getChildAt(i);
            img.setImageResource(i == index ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    static class OnboardingPage {
        final int imageRes;
        @StringRes final int titleRes;
        @StringRes final int subRes;
        OnboardingPage(int imageRes, @StringRes int titleRes, @StringRes int subRes) {
            this.imageRes = imageRes; this.titleRes = titleRes; this.subRes = subRes;
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_onboarding_page, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) { holder.bind(pages.get(position)); }
        @Override public int getItemCount() { return pages.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView img; TextView title, sub; Button btn;
            VH(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.imgHero);
                title = itemView.findViewById(R.id.tvTitle);
                sub = itemView.findViewById(R.id.tvSubtitle);
                btn = itemView.findViewById(R.id.btnContinue);
            }
            void bind(OnboardingPage page) {
                img.setImageResource(page.imageRes);
                title.setText(page.titleRes);
                sub.setText(page.subRes);

                btn.setOnClickListener(v -> {
                    if (getAdapterPosition() == pages.size() - 1) {
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit().putBoolean("onboarding_done", true).apply();
                        startActivity(new Intent(OnboardingActivity.this, AuthActivity.class));
                        finish();
                    } else {
                        pager.setCurrentItem(getAdapterPosition() + 1);
                    }
                });
            }
        }
    }
}
