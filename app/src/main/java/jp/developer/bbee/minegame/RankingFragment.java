package jp.developer.bbee.minegame;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class RankingFragment extends Fragment {

    public static final int RANKING_REGISTER_NUM = MainActivity.RANKING_REGISTER_NUM;
    public static final int LEVEL_MAX = MainActivity.levelMax;
    public static final int LEVEL_MIN = MainActivity.levelMin;
    public static Score[][] allScore;

    public ViewPager2 pager;
    FragmentStateAdapter adapter;

    private ImageView pageLeft;
    private ImageView pageRight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ranking, container, false);
        pageLeft = view.findViewById(R.id.pageLeft);
        pageRight = view.findViewById(R.id.pageRight);
        allScore = MainActivity.getAllScore();
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

    public static class RankingSlideAdapter extends FragmentStateAdapter {
        public RankingSlideAdapter(Fragment f) {
            super(f);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return newInstance(position);
        }

        @Override
        public int getItemCount() {
            return LEVEL_MAX - LEVEL_MIN + 1;
        }

        public static final RankingSlideFragment newInstance(int position) {
            RankingSlideFragment rsf = new RankingSlideFragment();
            Bundle b = new Bundle(1);
            b.putInt("position", position);
            rsf.setArguments(b);
            return rsf;
        }
    }

    public static class RankingSlideFragment extends Fragment {

        private int level;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            assert getArguments() != null;
            level = getArguments().getInt("position") + LEVEL_MIN - 1;
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
