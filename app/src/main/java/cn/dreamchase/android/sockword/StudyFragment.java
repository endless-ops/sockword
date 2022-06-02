package cn.dreamchase.android.sockword;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.assetsbasedata.AssetsDatabaseManager;

import java.util.List;
import java.util.Random;

import cn.dreamchase.android.greendao.entity.DaoMaster;
import cn.dreamchase.android.greendao.entity.DaoSession;
import cn.dreamchase.android.greendao.entity.WisdomEntity;
import cn.dreamchase.android.greendao.entity.WisdomEntityDao;

public class StudyFragment extends Fragment {

    private TextView difficultyTv,
            wisdomEnglish,
            wisdomChina,
            alreadyStudyText,
            alreadyMasteredText,
            wrongText;

    private SharedPreferences sharedPreferences;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    private WisdomEntityDao questionDao;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.study_fragment, null);
        sharedPreferences = getActivity().getSharedPreferences("share", Context.MODE_PRIVATE);

        difficultyTv = view.findViewById(R.id.difficulty_text);
        wisdomEnglish = view.findViewById(R.id.wisdom_english);
        wisdomChina = view.findViewById(R.id.wisdom_china);

        alreadyStudyText = view.findViewById(R.id.already_study);
        alreadyMasteredText = view.findViewById(R.id.already_mastered);
        wrongText = view.findViewById(R.id.wrong_text);

        AssetsDatabaseManager.initManager(getActivity());


        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        SQLiteDatabase db1 = mg.getDatabase("wisdom.db");

        daoMaster = new DaoMaster(db1);
        daoSession = daoMaster.newSession();
        questionDao = daoSession.getWisdomEntityDao();


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        difficultyTv.setText(sharedPreferences.getString("difficulty", "四级") + "英语");
        List<WisdomEntity> datas = questionDao.queryBuilder().list();
        Random random = new Random();
        int i = random.nextInt(10);

        wisdomEnglish.setText(datas.get(i).getEnglish());
        wisdomChina.setText(datas.get(i).getChina());

        setText();
    }

    private void setText() {
        alreadyMasteredText.setText(sharedPreferences.getInt("alreadyMastered", 0) + "");
        alreadyStudyText.setText(sharedPreferences.getInt("alreadyStudy", 0) + "");
        wrongText.setText(sharedPreferences.getInt("wrong", 0));
    }
}
