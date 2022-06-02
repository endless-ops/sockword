package cn.dreamchase.android.sockword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import cn.dreamchase.android.greendao.entity.CET4Entity;
import cn.dreamchase.android.greendao.entity.CET4EntityDao;
import cn.dreamchase.android.greendao.entity.DaoMaster;
import cn.dreamchase.android.greendao.entity.DaoSession;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, SynthesizerListener {

    private TextView timeText, dateText, wordText, englishText;
    private ImageView playVoice;
    private String mMonth, mDay, mWay, mHours, mMinute;
    private SpeechSynthesizer speechSynthesizer;  // 合成对象

    // 锁屏
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private RadioGroup radioGroup;
    private RadioButton radioOne, radioTwo, radioThree;
    private SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor = null;
    int j = 0;
    List<Integer> list; // 判断题的数目
    List<CET4Entity> datas; // 用于从数据库读取相应的词库
    int k;


    float x1 = 0;
    float y1 = 0;

    float x2 = 0;
    float y2 = 0;

    private SQLiteDatabase db;
    private DaoMaster mDaoMaster, dbMaster;
    private DaoSession mDaoSession, dbSession;

    private CET4EntityDao questionDao, dbDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);


        init();
    }

    public void init() {
        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        list = new ArrayList<>();

        Random r = new Random();
        int i;
        while (list.size() < 10) {
            i = r.nextInt();
            if (!list.contains(i)) {
                list.add(i);
            }
        }

        // 得到键盘锁管理对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");

        AssetsDatabaseManager.initManager(this);// 初始化，只需要调用一次

        // 数据库需要通过管理对象才能获取
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();

        SQLiteDatabase db1 = mg.getDatabase("word.db");

        mDaoMaster = new DaoMaster(db1);
        mDaoSession = mDaoMaster.newSession();
        questionDao = mDaoSession.getCET4EntityDao();

        /**
         * 此 DevOpenHelper 类继承自 SQLiteOpenHelper，
         * 第一个参数是Context，第二个参数数据库名字，第三个参数 CursorFactory
         */
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wrong.db", null);

        db = helper.getWritableDatabase();
        dbMaster = new DaoMaster(db);
        dbSession = dbMaster.newSession();
        dbDao = dbSession.getCET4EntityDao();

        timeText = findViewById(R.id.time_text);
        dateText = findViewById(R.id.date_text);
        wordText = findViewById(R.id.word_text);
        englishText = findViewById(R.id.english_text);

        playVoice = findViewById(R.id.play_voice);
        playVoice.setOnClickListener(this);
        radioGroup = findViewById(R.id.choose_group);
        radioOne = findViewById(R.id.choose_btn_one);
        radioTwo = findViewById(R.id.choose_btn_two);
        radioThree = findViewById(R.id.choose_btn_three);
        radioGroup.setOnCheckedChangeListener(this);

        setParam();

        SpeechUser.getUser().login(MainActivity.this, null, null, "appid=573a7bf0", listener);
    }

    // 同步手机系统时间
    @Override
    protected void onStart() {
        super.onStart();
        Calendar calendar = Calendar.getInstance();
        mMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        mDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        mWay = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK)); // 星期

        if (calendar.get(Calendar.HOUR) < 10) {
            mHours = "0" + calendar.get(Calendar.HOUR);
        } else {
            mHours = String.valueOf(calendar.get(Calendar.HOUR));
        }

        if (calendar.get(Calendar.MINUTE) < 10) {
            mMinute = "0" + calendar.get(Calendar.MINUTE);
        } else {
            mMinute = String.valueOf(calendar.get(Calendar.MINUTE));
        }

        switch (mWay) {
            case "1":
                mWay = "天";
                break;
            case "2":
                mWay = "一";
                break;
            case "3":
                mWay = "二";
                break;
            case "4":
                mWay = "三";
                break;
            case "5":
                mWay = "Ⅳ";
                break;
            case "6":
                mWay = "五";
                break;
            case "7":
                mWay = "六";
                break;
        }

        timeText.setText(mHours + ":" + mMinute);
        dateText.setText(mMonth + "月" + mDay + "日" + "     星期" + mWay);
        getDBData();

        BaseApplication.addDestroyActivity(this,"mainActivity");
    }

    /**
     * -将错题存到数据库
     */
    private void saveWrongData() {
        String word = datas.get(k).getWord();
        String english = datas.get(k).getEnglish();
        String china = datas.get(k).getChina();
        String sign = datas.get(k).getSign();
        CET4Entity data = new CET4Entity(Long.valueOf(dbDao.count()), word, english, china, sign);
        dbDao.insertOrReplace(data);
    }

    private void btnGetText(String msg, RadioButton btn) {
        if (msg.equals(datas.get(k).getChina())) {
            wordText.setTextColor(Color.GREEN);
            englishText.setTextColor(Color.GREEN);
            btn.setTextColor(Color.GREEN);
        } else {
            wordText.setTextColor(Color.RED);
            englishText.setTextColor(Color.RED);
            btn.setTextColor(Color.RED);
        }

        saveWrongData();

        int wrong = sharedPreferences.getInt("wrong", 0);
        editor.putInt("wrong", wrong + 1);
        editor.putString("wrongId", "，" + datas.get(j).getId());
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_voice:
                String text = wordText.getText().toString();
                speechSynthesizer.startSpeaking(text, this);
                break;
        }
    }

    @Override
    public void onSpeakBegin() {

    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    /**
     * 初始化语言并设置回调
     *
     * @param speechError
     */
    @Override
    public void onCompleted(SpeechError speechError) {

    }

    /**
     * -调用回调接口
     */
    private SpeechListener listener = new SpeechListener() {
        @Override
        public void onEvent(int i, Bundle bundle) {

        }

        @Override
        public void onData(byte[] bytes) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }
    };

    /**
     * -初始化语言播报
     */
    public void setParam() {
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(this);
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        radioGroup.setClickable(false);
        // radioGroup.setSelected(false);

        switch (checkedId) {
            case R.id.choose_btn_one:
                String msg = radioOne.getText().toString().substring(3);
                btnGetText(msg, radioOne);
                break;

            case R.id.choose_btn_two:
                String msg1 = radioTwo.getText().toString().substring(3);
                btnGetText(msg1, radioTwo);
                break;

            case R.id.choose_btn_three:
                String msg2 = radioThree.getText().toString().substring(3);
                btnGetText(msg2, radioThree);
                break;
        }
    }

    private void setTextColor() {
        // 还原单词选项的颜色
        radioOne.setChecked(false);
        radioTwo.setChecked(false);
        radioThree.setChecked(false);

        radioOne.setTextColor(Color.parseColor("#FFFFFF"));
        radioTwo.setTextColor(Color.parseColor("#FFFFFF"));
        radioThree.setTextColor(Color.parseColor("#FFFFFF"));
        wordText.setTextColor(Color.parseColor("#FFFFFF"));
        englishText.setTextColor(Color.parseColor("#FFFFFF"));
    }

    /**
     * 解锁方法
     */
    private void unLocked() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        kl.disableKeyguard();
        finish();
    }

    private void setChina(List<CET4Entity> datas, int j) {
        Random r = new Random();

        List<Integer> listInt = new ArrayList<>();
        int i;
        while (listInt.size() < 4) {

            i = r.nextInt(20);
            if (!listInt.contains(i)) {
                listInt.add(i);
            }
        }

        if (listInt.get(0) < 7) {
            radioOne.setText("A:   " + datas.get(k).getChina());
            if (k - 1 >= 0) {
                radioTwo.setText("B:  " + datas.get(k - 1).getChina());
            } else {
                radioTwo.setText("B:  " + datas.get(k + 2).getChina());
            }

            if (k + 1 < 20) {
                radioThree.setText("C:   " + datas.get(k + 1).getChina());
            } else {
                radioThree.setText("C:   " + datas.get(k - 1).getChina());
            }
        } else if (listInt.get(0) < 14) {
            radioTwo.setText("B:   " + datas.get(k).getChina());
            if (k - 1 >= 0) {
                radioOne.setText("A:  " + datas.get(k - 1).getChina());
            } else {
                radioOne.setText("A:  " + datas.get(k + 2).getChina());
            }

            if (k + 1 < 20) {
                radioThree.setText("C:   " + datas.get(k + 1).getChina());
            } else {
                radioThree.setText("C:   " + datas.get(k - 1).getChina());
            }
        } else {
            radioThree.setText("C:   " + datas.get(k).getChina());
            if (k - 1 >= 0) {
                radioTwo.setText("B:  " + datas.get(k - 1).getChina());
            } else {
                radioTwo.setText("B:  " + datas.get(k + 2).getChina());
            }

            if (k + 1 < 20) {
                radioOne.setText("A:   " + datas.get(k + 1).getChina());
            } else {
                radioOne.setText("A:   " + datas.get(k - 1).getChina());
            }
        }
    }

    private void getDBData() {
        datas = questionDao.queryBuilder().list();
        k = list.get(j);
        wordText.setText(datas.get(k).getWord());
        englishText.setText(datas.get(k).getEnglish());
        setChina(datas, k);
    }

    private void getNextData() {
        j++;
        int i = sharedPreferences.getInt("allNum", 2);
        if (i > j) {
            getDBData();
            setTextColor();
            int num = sharedPreferences.getInt("alreadyStudy", 0) + 1;
            editor.putInt("alreadyStudy", num);
            editor.commit();
        } else {
            unLocked();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();
            if (y1 - y2 > 200) {
                int num = sharedPreferences.getInt("alreadyMastered", 0) + 1;
                editor.putInt("alreadyMastered", num);
                editor.commit();
                Toast.makeText(this, "已掌握", Toast.LENGTH_SHORT).show();
                getNextData();
            } else if (y2 - y1 > 200) {
                Toast.makeText(this, "待加功能", Toast.LENGTH_SHORT).show();
            } else if (x1 - x2 > 200) {
                getNextData();
            } else {
                unLocked();
            }
        }
        return super.onTouchEvent(event);
    }
}