package cn.dreamchase.android.sockword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ScreenListener {

    private Context context;
    private ScreenBroadcastReceiver mScreenReceiver;
    private ScreenStateListener mScreenStateListener;

    public ScreenListener(Context context) {
        this.context = context;
        mScreenReceiver = new ScreenBroadcastReceiver();
    }


    public interface ScreenStateListener {

        void onScreenOn();

        void onScreenOff();

        void onUserPresent();
    }


    private void getScreenState() {
        PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (manager.isScreenOn()) {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOn();
            }
        } else {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOff();
            }
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenStateListener.onScreenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenStateListener.onScreenOff();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mScreenStateListener.onUserPresent();  // 解锁时操作
            }
        }
    }

    public void begin(ScreenStateListener listener) {
        mScreenStateListener = listener;
        registerListener();
        getScreenState();
    }

    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(mScreenReceiver, filter);
    }

    public void unregisterListener() {
        context.unregisterReceiver(mScreenReceiver);
    }

}
