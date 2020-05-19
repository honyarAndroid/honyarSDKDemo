package com.example.honyarexample.view;

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;

public class BaseActivity extends Activity {
    private Timer mCheckDelayTimer;
    public void timer_start(int delaytime,boolean period){
        if(mCheckDelayTimer ==  null){
            mCheckDelayTimer = new Timer();
            if(period){
                mCheckDelayTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        timer_task();
                    }
                }, 3000, delaytime*1000);
            }else {
                mCheckDelayTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        timer_task();
                    }
                },delaytime*1000);
            }

        }
    }
    public void timer_pause(){
        if(mCheckDelayTimer != null){
            mCheckDelayTimer.cancel();
            mCheckDelayTimer = null;
        }
    }

    public void timer_task(){

    }
}
