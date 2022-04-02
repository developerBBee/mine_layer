package jp.developer.bbee.minegame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class RankingActivity extends AppCompatActivity {

    View decorView;

    Button btBack;
    FrameLayout rankingFrame;
    public static Score[][] allScore;

    ImageView pageLeft;
    ImageView pageRight;

    static RankingFragment mRankingFragment;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        decorView = getWindow().getDecorView();
        hideSystemUi();

        allScore = MainActivity.getAllScore();

        btBack = findViewById(R.id.btBack);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pageLeft = findViewById(R.id.pageLeft);
        pageRight = findViewById(R.id.pageRight);

        rankingFrame = findViewById(R.id.rankingFrame);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        mRankingFragment = new RankingFragment();
        transaction.replace(R.id.rankingFrame, mRankingFragment);
        transaction.commit();

        PageSlideButtonTouch leftListener = new PageSlideButtonTouch(false);
        PageSlideButtonTouch rightListener = new PageSlideButtonTouch(true);
        pageLeft.setOnClickListener(leftListener);
        pageLeft.setOnTouchListener(leftListener);
        pageRight.setOnClickListener(rightListener);
        pageRight.setOnTouchListener(rightListener);
    }

    private void hideSystemUi() {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private class PageSlideButtonTouch implements View.OnTouchListener, View.OnClickListener {

        private final boolean isRight;
        private final ImageView pageChange;

        PageSlideButtonTouch(boolean isRight) {
            this.isRight = isRight;
            if (isRight) {
                pageChange = pageRight;
            } else {
                pageChange = pageLeft;
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (pageChange.isEnabled()) {
                        pageChange.setColorFilter(Color.argb(0x1F, 0xFF, 0xFF, 0xFF));
                    }
                    return false;
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_BUTTON_RELEASE:
                    if (pageChange.isEnabled()) {
                        pageChange.setColorFilter(Color.argb(0x00, 0xFF, 0xFF, 0xFF));
                    }
                    return false;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (!pageChange.isEnabled()) {
                return;
            }
            int pageMove = -1;
            if (isRight) {
                pageMove = 1;
            }
            int level = mRankingFragment.pager.getCurrentItem() + pageMove;
            mRankingFragment.pager.setCurrentItem(level);
        }
    }
}