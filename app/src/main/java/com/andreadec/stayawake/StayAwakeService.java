/*
 * Copyright 2015 Andrea De Cesare
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andreadec.stayawake;

import android.app.*;
import android.content.*;
import android.os.*;
import android.os.PowerManager.*;

public class StayAwakeService extends Service {
    private final static int ID = 33;
    private final static String ACTION_STOP = "com.andreadec.stayawake.stop";
    private final static String WAKELOCK_TAG = "StayAwakeWakeLock";

    private WakeLock wakeLock;
    private BroadcastReceiver broadcastReceiver;
    private PowerManager powerManager;
    private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        powerManager = (PowerManager)getSystemService(POWER_SERVICE);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_STOP), 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.ic_wb_sunny_black_24dp);
        builder.setContentTitle(getString(R.string.running));
        builder.setContentText(getString(R.string.running));
        builder.setContentIntent(notificationPendingIntent);
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), stopPendingIntent);
        notification = builder.build();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STOP);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction()) {
                    case ACTION_STOP:
                        stopSelf();
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        //System.out.println("CREATE");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(wakeLock!=null && wakeLock.isHeld()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKELOCK_TAG);
        wakeLock.acquire();

        //System.out.println("START");

        startForeground(ID, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        unregisterReceiver(broadcastReceiver);
        stopForeground(true);
        //System.out.println("DESTROY");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
