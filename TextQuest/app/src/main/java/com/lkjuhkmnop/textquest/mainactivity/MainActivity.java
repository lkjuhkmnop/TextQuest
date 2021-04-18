package com.lkjuhkmnop.textquest.mainactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.lkjuhkmnop.textquest.R;
import com.lkjuhkmnop.textquest.tools.PopupsManager;
import com.lkjuhkmnop.textquest.tools.Tools;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView playBtn, addBtn, libBtn;
    private Button button;
    private int popupCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TextQuest);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PopupsManager.setCurrentMainView(findViewById(R.id.main_activity_layout));

        playBtn = findViewById(R.id.play_btn);
        addBtn = findViewById(R.id.add_btn);
        libBtn = findViewById(R.id.lib_btn);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> {
            popupCount++;
            Tools.getPopupsManager().addPopup("popup " + popupCount, Snackbar.LENGTH_SHORT);
        });

        playBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        libBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == playBtn.getId()) {
            Tools.startGamesActivity(this, v);
        } else if (v.getId() == addBtn.getId()) {
            Tools.startQuestManageActivityToAddQuest(this, v);
        } else if (v.getId() == libBtn.getId()) {
            Tools.startLibraryActivity(this, v);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PopupsManager.setCurrentMainView(findViewById(R.id.main_activity_layout));
    }
}