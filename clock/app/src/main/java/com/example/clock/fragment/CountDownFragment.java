package com.example.clock.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.clock.R;
import com.example.clock.View.SelectView;
import com.example.clock.View.SelectViewAdapter;

public class CountDownFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private SelectView hourSelectView;

    private SelectView minuteSelectView;

    private SelectView secondSelectView;

    private ImageButton startButton;

    private ImageButton endButton;

    private ImageButton stopButton;

    private ImageButton continueButton;
    private ImageButton settingButton;

    private long second;

    private Timer timer;

    private ConstraintLayout selectContainer;

    private RelativeLayout timeContainer;

    private TextView timeView;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private int selectedSoundResource;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_count_down, container, false);

        initSelectView();
        initMediaPlayer();
        initView();
        initVibrator();

        return rootView;
    }

    private void initView() {
        startButton = rootView.findViewById(R.id.button_start);
        endButton = rootView.findViewById(R.id.button_end);
        stopButton = rootView.findViewById(R.id.button_stop);
        selectContainer = rootView.findViewById(R.id.select_container);
        timeContainer = rootView.findViewById(R.id.time_container);
        timeView = rootView.findViewById(R.id.time);
        continueButton = rootView.findViewById(R.id.button_continue);
        settingButton=rootView.findViewById(R.id.button_settings);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);
        endButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);

    }

    private void initSelectView() {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add("");
        for(int i = 0; i <= 23; i++){
            list.add(i < 10 ? "0" + i : ""+i);
        }
        hourSelectView = rootView.findViewById(R.id.hour_select);
        SelectViewAdapter adapter = new SelectViewAdapter(rootView.getContext(), list);
        hourSelectView.setAdapter(adapter);
        list = new ArrayList<>();
        list.add("");
        list.add("");
        for(int i = 0; i <= 60; i++){
            list.add(i < 10 ? "0" + i : ""+i);
        }
        adapter = new SelectViewAdapter(rootView.getContext(), list);
        minuteSelectView = rootView.findViewById(R.id.minute_select);
        minuteSelectView.setAdapter(adapter);
        secondSelectView = rootView.findViewById(R.id.second_select);
        secondSelectView.setAdapter(adapter);


        LinearLayoutManager hourLayoutManager = new LinearLayoutManager(rootView.getContext());
        hourSelectView.setLayoutManager(hourLayoutManager);
        LinearLayoutManager minuteLayoutManager = new LinearLayoutManager(rootView.getContext());
        minuteSelectView.setLayoutManager(minuteLayoutManager);
        LinearLayoutManager secondLayoutManager = new LinearLayoutManager(rootView.getContext());
        secondSelectView.setLayoutManager(secondLayoutManager);

        //对齐的一种方式，但是不能丝滑滑动
        PagerSnapHelper hourPagerHelper = new PagerSnapHelper();
        hourPagerHelper.attachToRecyclerView(hourSelectView);

        PagerSnapHelper minutePagerHelper = new PagerSnapHelper();
        minutePagerHelper.attachToRecyclerView(minuteSelectView);

        PagerSnapHelper secondPagerHelper = new PagerSnapHelper();
        secondPagerHelper.attachToRecyclerView(secondSelectView);
    }
    private void initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(rootView.getContext(), R.raw.chinese);
        selectedSoundResource = R.raw.chinese;
    }
    private void initVibrator() {
        vibrator = (Vibrator) rootView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_start){
            second = 0;
            second += (Long.parseLong(hourSelectView.getValue()) * 60 * 60);
            second += (Long.parseLong(minuteSelectView.getValue()) * 60);
            second += (Long.parseLong(secondSelectView.getValue()));
            timeView.setText(secondFormat(second));
            selectContainer.setVisibility(View.GONE);
            timeContainer.setVisibility(View.VISIBLE);
            startLoadTime();
            endButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.GONE);
            settingButton.setVisibility(View.GONE);
        }else if (view.getId() == R.id.button_stop){
            stopLoadTime();
            stopButton.setVisibility(View.GONE);
            continueButton.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.button_continue) {
            startLoadTime();
            stopButton.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.GONE);
        } else if (view.getId() == R.id.button_end) {
            startButton.setVisibility(View.VISIBLE);
            settingButton.setVisibility(View.VISIBLE);
            endButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
            continueButton.setVisibility(View.GONE);
            selectContainer.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.GONE);

            stopLoadTime();
        }else if(view.getId() == R.id.button_settings){
            showSettingsDialog();
        }
    }
    private String secondFormat(long second){
        int hour = (int) (second / 3600);
        second -= hour * 3600;
        int minute = (int) (second / 60);
        second -= minute * 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private void startLoadTime() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                second -= 1;
                timeView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (second <= 0) {
                            second = 0;
                            timeView.setText(secondFormat(second));
                            stopLoadTime();
                            playAlertSound();
                        } else {
                            timeView.setText(secondFormat(second));
                        }
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);
    }

    private void stopLoadTime() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void playAlertSound() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        String alertMethod = preferences.getString("alert_method", "sound_only");

        if (alertMethod.equals("sound_only") || alertMethod.equals("both")) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(rootView.getContext(), selectedSoundResource);
            mediaPlayer.start();
        }
        if (alertMethod.equals("vibration_only") || alertMethod.equals("both")) {
            if (vibrator != null) {
                vibrator.vibrate(1000); // 震动1秒
            }
        }
    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        builder.setTitle("音效设置");

        // 自定义对话框布局
        View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        builder.setView(view);

        // 提醒方式选择
        RadioGroup alertMethodGroup = view.findViewById(R.id.radioGroup_alert_method);
        RadioButton soundOnlyButton = view.findViewById(R.id.radio_sound_only);
        RadioButton vibrationOnlyButton = view.findViewById(R.id.radio_vibration_only);
        RadioButton bothButton = view.findViewById(R.id.radio_both);

        // 音效选择
        Spinner soundSpinner = view.findViewById(R.id.spinner_sound);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(rootView.getContext(),
                R.array.sound_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSpinner.setAdapter(adapter);

        // 读取并显示已保存的设置
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
        String alertMethod = preferences.getString("alert_method", "sound_only");
        switch (alertMethod) {
            case "sound_only":
                soundOnlyButton.setChecked(true);
                break;
            case "vibration_only":
                vibrationOnlyButton.setChecked(true);
                break;
            case "both":
                bothButton.setChecked(true);
                break;
        }

        String soundOption = preferences.getString("sound_option", "default");
        int spinnerPosition = adapter.getPosition(soundOption);
        soundSpinner.setSelection(spinnerPosition);

        // 保存设置
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = preferences.edit();

                int selectedMethodId = alertMethodGroup.getCheckedRadioButtonId();
                String selectedMethod = "sound_only";
                if (selectedMethodId == R.id.radio_vibration_only) {
                    selectedMethod = "vibration_only";
                } else if (selectedMethodId == R.id.radio_both) {
                    selectedMethod = "both";
                }
                editor.putString("alert_method", selectedMethod);

                String selectedSound = soundSpinner.getSelectedItem().toString();
                editor.putString("sound_option", selectedSound);

                // 根据用户选择更新 selectedSoundResource
                if (selectedSound.equals("中文")) {
                    selectedSoundResource = R.raw.chinese;
                } else if (selectedSound.equals("日文")) {
                    selectedSoundResource = R.raw.alert_sound;
                } else if (selectedSound.equals("英文")) {
                    selectedSoundResource = R.raw.english;
                } else if (selectedSound.equals("六合文")) {
                    selectedSoundResource = R.raw.sixhe;
                }

                editor.apply();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }
}