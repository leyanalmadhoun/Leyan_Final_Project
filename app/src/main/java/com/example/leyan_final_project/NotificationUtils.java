package com.example.leyan_final_project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
public final class NotificationUtils {

    public static final String CHANNEL_ID = "orders";

    private static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    ctx.getString(R.string.channel_orders),
                    NotificationManager.IMPORTANCE_HIGH
            );
            ch.enableVibration(true);
            ch.enableLights(true);
            nm.createNotificationChannel(ch);
        }
    }

    public static void showOrderPlaced(Context ctx, long orderId) {
        ensureChannel(ctx);

        Intent i = new Intent(ctx, MainActivity.class);
        i.putExtra("open_orders", true);
        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_shopping_cart_24)
                .setContentTitle(ctx.getString(R.string.notif_title_order))
                .setContentText(ctx.getString(R.string.notif_text_order, String.valueOf(orderId)))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS);

        NotificationManagerCompat.from(ctx).notify((int) orderId, nb.build());
    }
}
