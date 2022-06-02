package cn.dreamchase.android.sockword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ScreenListener screenListener;
    private SharedPreferences sharedPreferences;
    private FragmentTransaction transaction;
    private StudyFragment studyFragment;
    private SetFragment setFragment;
    private Button wrongBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.home_layout);
        init();
    }

    private void init() {
        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);

        wrongBtn = findViewById(R.id.wrong_btn);

        wrongBtn.setOnClickListener(this);

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                if (sharedPreferences.getBoolean("btnTf",false)) {
                    if (sharedPreferences.getBoolean("tf",false)) {
                        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onScreenOff() {
                editor.putBoolean("tf",true);
                editor.commit();
                BaseApplication.destroyActivity("mainActivity");
            }

            @Override
            public void onUserPresent() {
                editor.putBoolean("tf",false);
                editor.commit();
            }
        });

        studyFragment = new StudyFragment();
        setFragment(studyFragment);
    }


    public void setFragment(Fragment fragment) {
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout,fragment);
        transaction.commit();
    }

    public void study(View v) {
        if (studyFragment == null) {
            studyFragment = new StudyFragment();
        }
        setFragment(studyFragment);
    }

    public void set(View v) {
        if (setFragment == null) {
            setFragment = new SetFragment();
        }
        setFragment(setFragment);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wrong_btn:
                Toast.makeText(this,"跳转道错题界面",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this,WrongActivity.class);    //启动WrongActivity
                startActivity(i);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        screenListener.unregisterListener();
    }
}
