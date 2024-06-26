package com.example.clock.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clock.R;
import com.example.clock.adapter.StopwatchAdapter;
import com.example.clock.bean.TimeRecordBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StopwatchFragment extends Fragment implements View.OnClickListener {
    private ImageButton startButton;
    private ImageButton markLeftButton;
    private ImageButton endLeftButton;
    private ImageButton startRightButton;
    private ImageButton stopRightButton;
    private TextView timeView;
    private long startTime;
    private long elapsedTime;
    private long lastRecordMillisecond;
    private boolean isRunning = false;
    private boolean markStatus = false;
    private Timer timer;
    private RecyclerView recyclerView;
    private ConstraintLayout parentLayout;
    private int serialNum = 0;
    private StopwatchAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        startButton = rootView.findViewById(R.id.button_start);
        markLeftButton = rootView.findViewById(R.id.button_markLeft);
        endLeftButton = rootView.findViewById(R.id.button_endLeft);
        startRightButton = rootView.findViewById(R.id.button_startRight);
        stopRightButton = rootView.findViewById(R.id.button_stopRight);
        timeView = rootView.findViewById(R.id.time);
        parentLayout = rootView.findViewById(R.id.stopwatchParent);

        startButton.setOnClickListener(this);
        startRightButton.setOnClickListener(this);
        stopRightButton.setOnClickListener(this);
        markLeftButton.setOnClickListener(this);
        endLeftButton.setOnClickListener(this);

        recyclerView = rootView.findViewById(R.id.recycleView);
        List<TimeRecordBean> list = new ArrayList<>();
        adapter = new StopwatchAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_start) {
            startButton.setVisibility(View.GONE);
            markLeftButton.setVisibility(View.VISIBLE);
            stopRightButton.setVisibility(View.VISIBLE);
            startLoadTime();
        } else if (view.getId() == R.id.button_startRight) {   //右边开始按钮
            startLoadTime();
            startRightButton.setVisibility(View.GONE);
            stopRightButton.setVisibility(View.VISIBLE);
            markLeftButton.setVisibility(View.VISIBLE);
            endLeftButton.setVisibility(View.GONE);
        } else if (view.getId() == R.id.button_stopRight) {     //右边暂停按钮
            stopLoadTime();
            startRightButton.setVisibility(View.VISIBLE);
            stopRightButton.setVisibility(View.GONE);
            markLeftButton.setVisibility(View.GONE);
            endLeftButton.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.button_markLeft) {       //左边标记按钮
            if (!markStatus) {
                changeStatusToMark();
                markStatus = true;
            }
            //往recyclerview塞数据
            String serialNumTemp = ++serialNum < 9 ? "0" + serialNum : String.valueOf(serialNum);
            String extraTimeTemp = "+" + millisecondFormat(elapsedTime - lastRecordMillisecond);
            lastRecordMillisecond = elapsedTime;
            String recordTimeTemp = millisecondFormat(elapsedTime);
            adapter.addItem(new TimeRecordBean(serialNumTemp, extraTimeTemp, recordTimeTemp));
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        } else if (view.getId() == R.id.button_endLeft) {       //左边结束按钮,结束计时
            endLoadTIme();
        }
    }

    public void changeStatusToMark() {
        if (!markStatus) {
            recyclerView.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) timeView.getLayoutParams();
            layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.startToStart = R.id.stopwatchParent;
            layoutParams.topToTop = R.id.stopwatchParent;
            layoutParams.endToEnd = R.id.stopwatchParent;
            layoutParams.topMargin = 100;
            timeView.setLayoutParams(layoutParams);
        } else {
            recyclerView.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) timeView.getLayoutParams();
            layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.startToStart = R.id.stopwatchParent;
            layoutParams.topToTop = R.id.stopwatchParent;
            layoutParams.endToEnd = R.id.stopwatchParent;
            layoutParams.bottomToBottom = R.id.stopwatchParent;
            layoutParams.bottomMargin = dp2px(getContext(), 200);
            timeView.setLayoutParams(layoutParams);
        }
    }

    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 秒表开始
     */
    public void startLoadTime() {
        if (isRunning) return;
        startTime = System.currentTimeMillis() - elapsedTime;
        isRunning = true;
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                elapsedTime = System.currentTimeMillis() - startTime;
                timeView.post(new Runnable() {
                    @Override
                    public void run() {
                        timeView.setText(millisecondFormat(elapsedTime));
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 10);
    }

    //暂停计时
    private void stopLoadTime() {
        if (!isRunning) return;
        isRunning = false;
        elapsedTime = System.currentTimeMillis() - startTime;
        timer.cancel();
    }

    // 结束计时
    private void endLoadTIme() {
        stopLoadTime();
        startButton.setVisibility(View.VISIBLE);
        markLeftButton.setVisibility(View.GONE);
        endLeftButton.setVisibility(View.GONE);
        stopRightButton.setVisibility(View.GONE);
        startRightButton.setVisibility(View.GONE);
        if (markStatus) {
            changeStatusToMark();
            markStatus = false;
        }
        elapsedTime = 0;
        serialNum = 0;
        timeView.setText(millisecondFormat(0));
        adapter.cleanList();
    }

    public String millisecondFormat(long elapsed) {
        int hour, milli, second, minute;

        milli = (int) (elapsed % 1000);
        elapsed = elapsed / 1000;
        second = (int) (elapsed % 60);
        elapsed = elapsed / 60;
        minute = (int) (elapsed % 60);
        elapsed = elapsed / 60;
        hour = (int) (elapsed % 60);

        String milliStr = String.valueOf(milli);

        if (milliStr.length() >= 2) {
            milliStr = milliStr.substring(0, 2);
        } else {
            milliStr = "0" + milliStr;
        }
        return String.format("%02d:%02d.%s", minute, second, milliStr);
    }
}
