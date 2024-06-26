package com.example.clock.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.example.clock.R;

public class ClockFragment extends Fragment {

    private TextView nowTimeView;

    private TextView localTimeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clock, container, false);

        nowTimeView = view.findViewById(R.id.nowTime);

        localTimeView = view.findViewById(R.id.localTime);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("本地时间：yyyy年MM月dd日");
        String localTime = simpleDateFormat.format(date);
        LocalTime currentTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentTime = LocalTime.now();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (currentTime.isBefore(LocalTime.NOON)) {
                localTime = localTime + " 上午";
            } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
                localTime = localTime + " 下午";
            } else {
                localTime = localTime + " 晚上";
            }
        }
        localTimeView.setText(localTime);

        taskUpdateTime();
        return view;
    }

    public void taskUpdateTime(){
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                String time = dateFormat.format(date);
                nowTimeView.post(new TimerTask() {
                    @Override
                    public void run() {
                        nowTimeView.setText(time);
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(task,5,1000);
    }
}