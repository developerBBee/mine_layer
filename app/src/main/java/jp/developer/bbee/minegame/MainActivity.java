package jp.developer.bbee.minegame;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    final int X_MAX = 12;
    final int Y_MAX = 6;
    final int Z_MAX = 3;
    final int Z_MAX_OF_MAX = 3;
    //    final int BOMB_NUM = 10;
//    final int CLEAR_OPEN_NUM = X_MAX * Y_MAX * Z_MAX - BOMB_NUM;
    final int TIME_DURATION = 10;
    final String MAX_TIME = "23:59:59.99";

    final int DEBUG_TIMER = 0; //23*60*60*1000 + 59*60*1000; //59*60*1000;

    public static final int RANKING_REGISTER_NUM = 5;
    private AppDatabase db;
    private List<Score> scores;
    public static Score[][] allScore;
    public static int levels;
    public static int levelMax;
    public static int levelMin;
    private boolean scoreUpdated;
    ImageView ranking;

    private Timer timer;
    // 'Handler()' is deprecated as of API 30: Android 11.0 (R)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView timerText;
    private final SimpleDateFormat dataFormat =
            new SimpleDateFormat("mm:ss.SS", Locale.US);
    private final SimpleDateFormat dataFormatOverHour =
            new SimpleDateFormat("H:mm:ss.SS", Locale.US);
    private long startTime;

    Context mContext;

    LinearLayout area;
    ImageView[][][] imgBox;
    Button btStart;
    ImageView ivFlag;
    Spinner bombNumSpinner;

    LinearLayout.LayoutParams areaParams;
    ViewGroup.LayoutParams imgParams;

    boolean[][][] isBomb;
    int[][][] numList;
    boolean[][][] isFlag;
    boolean[][][] isOpen;
    boolean isClear;
    boolean isDead;

    boolean modeFlag;
    int openNum;
    int bombNum;
    int clearOpenNum;

    LinearLayout floors;
    ImageView ivFloor1st;
    ImageView ivFloor2nd;
    ImageView ivFloor3rd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        ranking = findViewById(R.id.ivRanking);
        ranking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendScore();
            }
        });

        timerText = findViewById(R.id.timerText);
        timerText.setText(dataFormat.format(0));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.spinner_layout, getResources().getStringArray(R.array.bomb_num)
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        bombNumSpinner = findViewById(R.id.bombNumSpinner);
        bombNumSpinner.setAdapter(adapter);
        levels = bombNumSpinner.getCount();
        bombNumSpinner.setSelection((int) levels/2);
        levelMax = Integer.parseInt((String) bombNumSpinner.getItemAtPosition(levels-1));
        levelMin = Integer.parseInt((String) bombNumSpinner.getItemAtPosition(0));
        bombNum = Integer.parseInt((String) bombNumSpinner.getSelectedItem());
        clearOpenNum = X_MAX * Y_MAX * Z_MAX - bombNum;

        scoreUpdate();

        bombNumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bombNum = Integer.parseInt((String) parent.getSelectedItem());
                clearOpenNum = X_MAX * Y_MAX * Z_MAX - bombNum;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        LinearLayout linearGameArea1 = findViewById(R.id.linearGameArea);
        LinearLayout linearGameArea2 = findViewById(R.id.linearGameArea2);
        LinearLayout linearGameArea3 = findViewById(R.id.linearGameArea3);

        LinearLayout[] linearGameArea = new LinearLayout[Z_MAX_OF_MAX];
        linearGameArea[0] = linearGameArea1;
        linearGameArea[1] = linearGameArea2;
        linearGameArea[2] = linearGameArea3;

        imgBox = new ImageView[X_MAX][Y_MAX][Z_MAX];

        floors = findViewById(R.id.floors);
        ivFloor1st = findViewById(R.id.iv1st);
        ivFloor2nd = findViewById(R.id.iv2nd);
        ivFloor3rd = findViewById(R.id.iv3rd);

        makeParams();

        ivFlag = findViewById(R.id.ivFlag);
        modeFlag = false;
        ivFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeFlag) {
                    modeFlag = false;
                    ivFlag.clearColorFilter();
                } else {
                    modeFlag = true;
                    ivFlag.setColorFilter(Color.argb(0x3F, 0, 0xFF, 0xFF));
                }
            }
        });

        btStart = findViewById(R.id.btStart);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClear = false;
                isDead = false;
                btStart.setEnabled(false);
                bombNumSpinner.setEnabled(false);
                timerStart();

                linearGameArea1.removeAllViews();
                linearGameArea2.removeAllViews();
                linearGameArea3.removeAllViews();
                makeBomb();
                makeNum();
                isOpen = new boolean[X_MAX][Y_MAX][Z_MAX];
                openNum = 0;
                isFlag = new boolean[X_MAX][Y_MAX][Z_MAX];

                for (int z = 0; z < Z_MAX; z++) {
                    for (int y = 0; y < Y_MAX; y++) {
                        for (int x = 0; x < X_MAX; x++) {

                            if (x == 0) {
                                area = new LinearLayout(MainActivity.this);
                                area.setHorizontalGravity(View.TEXT_ALIGNMENT_CENTER);
                                area.setLayoutParams(areaParams);
                                linearGameArea[z].addView(area);
                            }

                            imgBox[x][y][z] = new ImageView(MainActivity.this);
                            imgBox[x][y][z].setImageResource(R.drawable.whitebox);
                            imgBox[x][y][z].setLayoutParams(imgParams);
                            int x1 = x;
                            int y1 = y;
                            int z1 = z;
                            imgBox[x][y][z].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (modeFlag) {
                                        if (isOpen[x1][y1][z1]) {
                                            return;
                                        }
                                        if (isFlag[x1][y1][z1]) {
                                            imgBox[x1][y1][z1].setImageResource(R.drawable.whitebox);
                                            isFlag[x1][y1][z1] = false;
                                        } else {
                                            imgBox[x1][y1][z1].setImageResource(R.drawable.flag);
                                            isFlag[x1][y1][z1] = true;
                                        }
                                    } else {
                                        if (isFlag[x1][y1][z1] || isClear) {
                                            return;
                                        }
                                        openBox(x1, y1, z1);
                                    }
                                }

                                public void openBox(int x2, int y2, int z2) {
                                    if (isBomb[x2][y2][z2]) {
                                        isDead = true;
                                        imgBox[x2][y2][z2].setImageResource(R.drawable.bomb);
                                        btStart.setEnabled(true);
                                        bombNumSpinner.setEnabled(true);
                                        timerStop();
                                        isOpen[x2][y2][z2] = true;
                                    } else {
                                        if (isOpen[x2][y2][z2]) {
                                            return;
                                        }
                                        openNum++;
                                        if (openNum == clearOpenNum && !isDead) {
                                            isClear = true;
                                            btStart.setEnabled(true);
                                            bombNumSpinner.setEnabled(true);
                                            timerStop();
                                            String strScore = timerText.getText().toString();
                                            ResultDialogFragment resultDialog = new ResultDialogFragment();
                                            resultDialog.setClearTime(strScore);
                                            resultDialog.show(getSupportFragmentManager(), "Clear");
                                            scoreUpdate(bombNum, strScore);
                                        }
                                        String n = String.valueOf(numList[x2][y2][z2]);
                                        Uri nUri = Uri.parse("android.resource://jp.developer.bbee.minegame/drawable/num_" + n);
                                        imgBox[x2][y2][z2].setImageURI(nUri);
                                        isOpen[x2][y2][z2] = true;

                                        if (numList[x2][y2][z2] == 0) {
                                            if (x2 != 0) {
                                                openBox(x2 - 1, y2, z2);
                                            }
                                            if (x2 != X_MAX - 1) {
                                                openBox(x2 + 1, y2, z2);
                                            }
                                            if (y2 != 0) {
                                                openBox(x2, y2 - 1, z2);
                                            }
                                            if (y2 != Y_MAX - 1) {
                                                openBox(x2, y2 + 1, z2);
                                            }
                                            if (x2 != 0 && y2 != 0) {
                                                openBox(x2 - 1, y2 - 1, z2);
                                            }
                                            if (x2 != X_MAX - 1 && y2 != 0) {
                                                openBox(x2 + 1, y2 - 1, z2);
                                            }
                                            if (x2 != 0 && y2 != Y_MAX - 1) {
                                                openBox(x2 - 1, y2 + 1, z2);
                                            }
                                            if (x2 != X_MAX - 1 && y2 != Y_MAX - 1) {
                                                openBox(x2 + 1, y2 + 1, z2);
                                            }

                                        }

                                    }
                                }
                            });

                            area.addView(imgBox[x][y][z]);
                        }
                    }
                }

                floors.setVisibility(View.VISIBLE);
                ivFloor1st.setImageResource(R.drawable.floor_1_on);
                ivFloor2nd.setImageResource(R.drawable.floor_2_off);
                ivFloor3rd.setImageResource(R.drawable.floor_3_off);

                linearGameArea[0].setVisibility(View.VISIBLE);
                linearGameArea[1].setVisibility(View.INVISIBLE);
                linearGameArea[2].setVisibility(View.INVISIBLE);

                ivFloor1st.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ivFloor1st.setImageResource(R.drawable.floor_1_on);
                        ivFloor2nd.setImageResource(R.drawable.floor_2_off);
                        ivFloor3rd.setImageResource(R.drawable.floor_3_off);
                        linearGameArea[0].setVisibility(View.VISIBLE);
                        linearGameArea[1].setVisibility(View.INVISIBLE);
                        linearGameArea[2].setVisibility(View.INVISIBLE);
                    }
                });
                ivFloor2nd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ivFloor1st.setImageResource(R.drawable.floor_1_off);
                        ivFloor2nd.setImageResource(R.drawable.floor_2_on);
                        ivFloor3rd.setImageResource(R.drawable.floor_3_off);
                        linearGameArea[0].setVisibility(View.INVISIBLE);
                        linearGameArea[1].setVisibility(View.VISIBLE);
                        linearGameArea[2].setVisibility(View.INVISIBLE);
                    }
                });
                ivFloor3rd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ivFloor1st.setImageResource(R.drawable.floor_1_off);
                        ivFloor2nd.setImageResource(R.drawable.floor_2_off);
                        ivFloor3rd.setImageResource(R.drawable.floor_3_on);
                        linearGameArea[0].setVisibility(View.INVISIBLE);
                        linearGameArea[1].setVisibility(View.INVISIBLE);
                        linearGameArea[2].setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }

    public int dpToPx(int dp) {
        float d = mContext.getResources().getDisplayMetrics().density;
        return (int) ((dp * d) + 0.5);
    }

    public int pxToDp(int px) {
        float d = mContext.getResources().getDisplayMetrics().density;
        return (int) ((px / d) + 0.5);
    }

    private void makeParams() {
        areaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        areaParams.setLayoutDirection(LinearLayout.HORIZONTAL);

        imgParams = new ViewGroup.LayoutParams(
                dpToPx(50),
                dpToPx(50)
        );
    }

    private void makeBomb() {
        int n = bombNum;
        isBomb = new boolean[X_MAX][Y_MAX][Z_MAX];
        for (int x = 0; x < X_MAX; x++) {
            for (int y = 0; y < Y_MAX; y++) {
                for (int z = 0; z < Z_MAX; z++) {
                    isBomb[x][y][z] = false;
                }
            }
        }

        if (n == 0) {
            return;
        }
        while (true) {
            int rnd_x = (int) Math.floor(Math.random() * X_MAX);
            int rnd_y = (int) Math.floor(Math.random() * Y_MAX);
            int rnd_z = (int) Math.floor(Math.random() * Z_MAX);

            if (!isBomb[rnd_x][rnd_y][rnd_z]) {
                isBomb[rnd_x][rnd_y][rnd_z] = true;
                n--;
            }
            if (n <= 0) {
                return;
            }
        }
    }

    private void makeNum() {
        if (isBomb == null) {
            return;
        }
        numList = new int[X_MAX][Y_MAX][Z_MAX];

        boolean isLeftEdge;
        boolean isRightEdge;
        boolean isTopEdge;
        boolean isBottomEdge;
        boolean isGroundEdge;
        boolean isRoofEdge;

        boolean[][][] isEdges;
        isEdges = new boolean[3][3][3];

        boolean[] isEdgeX;
        isEdgeX = new boolean[3];
        boolean[] isEdgeY;
        isEdgeY = new boolean[3];
        boolean[] isEdgeZ;
        isEdgeZ = new boolean[3];

        for (int x = 0; x < X_MAX; x++) {
            for (int y = 0; y < Y_MAX; y++) {
                for (int z = 0; z < Z_MAX; z++) {
                    int num = 0;

                    isLeftEdge = (x == 0);
                    isRightEdge = (x == X_MAX - 1);
                    isTopEdge = (y == 0);
                    isBottomEdge = (y == Y_MAX - 1);
                    isGroundEdge = (z == 0);
                    isRoofEdge = (z == Z_MAX - 1);

                    isEdgeX[0] = !isLeftEdge;
                    isEdgeX[1] = true;
                    isEdgeX[2] = !isRightEdge;

                    isEdgeY[0] = !isTopEdge;
                    isEdgeY[1] = true;
                    isEdgeY[2] = !isBottomEdge;

                    isEdgeZ[0] = !isGroundEdge;
                    isEdgeZ[1] = true;
                    isEdgeZ[2] = !isRoofEdge;

                    for (int dx = 0; dx < 3; dx++) {
                        for (int dy = 0; dy < 3; dy++) {
                            for (int dz = 0; dz < 3; dz++) {
                                if (isEdgeX[dx] && isEdgeY[dy] && isEdgeZ[dz]
                                        && isBomb[x + dx - 1][y + dy - 1][z + dz - 1]) {
                                    num++;
                                }
                            }
                        }
                    }
                    numList[x][y][z] = num;
                }
            }
        }
    }

    public void timerStart() {
        timerStop();

        timer = new Timer();
        timerText.setText(dataFormat.format(0));

        startTime = System.currentTimeMillis();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isClear) {
                            return;
                        }
                        long t = (System.currentTimeMillis() - startTime) + DEBUG_TIMER;
                        if (t < 60 * 60 * 1000) {
                            timerText.setText(dataFormat.format(t));
                        } else if (t >= 24 * 60 * 60 * 1000) {
                            timerText.setText(MAX_TIME);
                            timerStop();
                        } else {
                            t -= 9 * 60 * 60 * 1000;
                            timerText.setText(dataFormatOverHour.format(t));
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, TIME_DURATION);
    }

    public void timerStop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void scoreUpdate(int currentBombNum, String currentStrScore) {
        Score currentScore = new Score(currentBombNum, currentStrScore);
        new Thread(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                db = AppDatabase.getDatabase(MainActivity.this);
                scoreUpdated = false;
                scores = db.scoreDao().getBombScore(currentBombNum);
                if (scores == null || scores.size() == 0) {
                    db.scoreDao().insert(currentScore);
                    scoreUpdated = true;
                } else {
                    int count = 0;
                    for (Score score : scores) {
                        count++;
                        if (!scoreUpdated && currentScore.numScore < score.numScore
                                && currentScore.numScore > 0) {
                            db.scoreDao().insert(currentScore);
                            scoreUpdated = true;
                        }
                        if (scoreUpdated && count >= RANKING_REGISTER_NUM) {
                            db.scoreDao().delete(score);
                        }
                    }
                }
                scoreUpdate();

                handler.post(new Runnable() {
                    @UiThread
                    @Override
                    public void run() {
                        if (scoreUpdated) {
                            Toast.makeText(MainActivity.this, "High Score Updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void scoreUpdate() {
        new Thread(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                allScore = new Score[levelMax][RANKING_REGISTER_NUM];
                db = AppDatabase.getDatabase(MainActivity.this);
                scores = db.scoreDao().getAllOrdered();
                int[] iRank = new int[levelMax];
                for (Score score : scores) {
                    allScore[score.bombNum][iRank[score.bombNum]] = score;
                    iRank[score.bombNum]++;
                }

            }
        }).start();
    }

    public void sendScore() {
        new Thread(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                allScore = new Score[levelMax][RANKING_REGISTER_NUM];
                db = AppDatabase.getDatabase(MainActivity.this);
                scores = db.scoreDao().getAllOrdered();
                int[] iRank = new int[levelMax];
                for (Score score : scores) {
                    allScore[score.bombNum-1][iRank[score.bombNum-1]] = score;
                    iRank[score.bombNum-1]++;
                }

                handler.post(new Runnable() {
                    @UiThread
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, RankingActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }).start();
    }

    public static Score[][] getAllScore() {
        return allScore;
    }
}