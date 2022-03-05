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

    Button btBack;
    FrameLayout rankingFrame;
    public static Score[][] allScore;

    ImageView pageLeft;
    ImageView pageRight;

    RankingFragment mRankingFragment;

    public static final int RANKING_REGISTER_NUM = MainActivity.RANKING_REGISTER_NUM;
    public static final int LEVEL_MAX = MainActivity.levelMax;
    public static final int LEVEL_MIN = MainActivity.levelMin;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

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
        mRankingFragment = new RankingFragment(pageLeft, pageRight);
        transaction.replace(R.id.rankingFrame, mRankingFragment);
        transaction.commit();

        PageSlideButtonTouch leftListener = new PageSlideButtonTouch(false);
        PageSlideButtonTouch rightListener = new PageSlideButtonTouch(true);
        pageLeft.setOnClickListener(leftListener);
        pageLeft.setOnTouchListener(leftListener);
        pageRight.setOnClickListener(rightListener);
        pageRight.setOnTouchListener(rightListener);
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

    public static class RankingFragment extends Fragment {

        public ViewPager2 pager;
        FragmentStateAdapter adapter;

        private final ImageView pageLeft;
        private final ImageView pageRight;

        RankingFragment(ImageView left, ImageView right) {
            this.pageLeft = left;
            this.pageRight = right;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ranking, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            pager = view.findViewById(R.id.pager);
            adapter = new RankingSlideAdapter(RankingFragment.this);
            pager.setAdapter(adapter);
            pager.setCurrentItem((int) (LEVEL_MAX - LEVEL_MIN + 1) / 2, false);

            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (position == 0) {
                        pageLeft.setEnabled(false);
                        pageLeft.setColorFilter(Color.argb(0x7F, 0, 0, 0));
                    } else {
                        pageLeft.setEnabled(true);
                        pageLeft.clearColorFilter();
                    }
                    if (position == LEVEL_MAX - LEVEL_MIN) {
                        pageRight.setEnabled(false);
                        pageRight.setColorFilter(Color.argb(0x7F, 0, 0, 0));
                    } else {
                        pageRight.setEnabled(true);
                        pageRight.clearColorFilter();
                    }
                }
            });
        }
    }

    public static class RankingSlideAdapter extends FragmentStateAdapter {
        public RankingSlideAdapter(Fragment f) {
            super(f);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new RankingSlideFragment(position);
        }

        @Override
        public int getItemCount() {
            return LEVEL_MAX - LEVEL_MIN + 1;
        }
    }

    public static class RankingSlideFragment extends Fragment {

        private final int level;
        public RankingSlideFragment(int position) {
            super();
            level = position + LEVEL_MIN - 1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_page, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView rankName = view.findViewById(R.id.rankName);
            rankName.setText("Number of bombs : " + (level+1));

            TextView[] ranks = new TextView[RANKING_REGISTER_NUM];
            ranks[0] = view.findViewById(R.id.rank1);
            ranks[1] = view.findViewById(R.id.rank2);
            ranks[2] = view.findViewById(R.id.rank3);
            ranks[3] = view.findViewById(R.id.rank4);
            ranks[4] = view.findViewById(R.id.rank5);

            int i = 0;
            for (TextView rank : ranks) {
                if (allScore[level][i] != null) {
                    rank.setText(allScore[level][i].strScore);
                } else if (i == 0) {
                    rank.setText("No Score");
                }
                i++;
            }
        }
    }
}