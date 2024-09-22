package com.sliit.taskdb

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Vibrator
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Log when the alarm is received
        Log.d("Alarm", "Alarm received!")

        val taskTitle = intent.getStringExtra("taskTitle") ?: "Unnamed Task"

        // Vibrate the device
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)  // Vibrate for 1 second

        // Play the default alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Create a notification with sound and vibration
        val notification = NotificationCompat.Builder(context, "taskReminderChannel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Reminder")
            .setContentText("Your task '$taskTitle' is due soon!")
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 1000))  // Vibration pattern
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // Automatically remove the notification when clicked
            .build()

        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider requesting the missing permissions
            return
        }
        NotificationManagerCompat.from(context).notify(taskTitle.hashCode(), notification)
    }
}
