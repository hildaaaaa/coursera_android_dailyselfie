package android.coursera.dailyselfie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hildachung on 27/2/2018.
 */

public class AlarmNotificationReceiver extends BroadcastReceiver {

    private final static int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent clickIntent = new Intent(context, HomeActivity.class);
        if (!HomeActivity.active) {
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    HomeActivity.REQUEST_CODE_NOTIFICATION,
                    clickIntent,
                    PendingIntent.FLAG_ONE_SHOT
            );
            Notification.Builder notificationBuilder = new Notification.Builder(context)
                    .setTicker("Ready to come back?")
                    .setSmallIcon(android.R.drawable.ic_menu_camera)
                    .setAutoCancel(true)
                    .setContentTitle("Reminder")
                    .setContentText("Take your selfie today")
                    .setContentIntent(contentIntent);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
            System.out.println("Notification fired");
        }
    }
}
