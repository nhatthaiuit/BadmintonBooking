package com.example.badmintonbooking;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

public class SyncService extends Service {
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Simulate a long-running background task in a new Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate 3 seconds of network syncing delay
                    Thread.sleep(3000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Switch back to Main Thread to show a UI Toast
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Data synced successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
                
                // Stop the service automatically after work is done
                stopSelf();
            }
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding in this simple service
        return null;
    }
}
