package com.patrickmichaelsen.livebasketball;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Patrick on 7/12/2017.
 */

public class NotificationsListenerService extends GcmListenerService {

    private static int nextNotificationID = 0;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String body = data.getString("body");
        String title = data.getString("title");
        Log.e("body","msg: " + body);
        Log.e("title","title: " + title);
        sendNotification(title, body);
    }

    private void sendNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_feedback_white_48dp);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.flashscore.com/basketball/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_feedback_black_48dp));
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        builder.setSound(alarmSound);
        builder.setLights(Color.BLUE, 500, 500);
        long[] pattern = {
                500,500,500,500,500,500,500,500,500,500,
                500,500,500,500,500,500,500,500,500,500,
                500,500,500,500,500,500,500,500,500,500,
                500,500,500,500,500,500,500,500,500,500,
                500,500,500,500,500,500,500,500,500,500,
                500,500,500,500,500,500,500,500,500,500,
        };
        builder.setVibrate(pattern);
        builder.setContentTitle(title);
        builder.setContentText(body);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(nextNotificationID++, builder.build());
    }
}
