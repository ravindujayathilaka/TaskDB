package com.sliit.taskdb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class CreateTaskActivity : AppCompatActivity() {
    private lateinit var taskTitle: EditText
    private lateinit var taskDescription: EditText
    private lateinit var startTimePicker: TimePicker
    private lateinit var endTimePicker: TimePicker
    private lateinit var saveTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        taskTitle = findViewById(R.id.taskTitle)
        taskDescription = findViewById(R.id.taskDescription)
        startTimePicker = findViewById(R.id.startTimePicker)
        endTimePicker = findViewById(R.id.endTimePicker)
        saveTaskButton = findViewById(R.id.saveTaskButton)

        saveTaskButton.setOnClickListener {
            saveTaskToFirebase()
        }
    }

    private fun saveTaskToFirebase() {
        val title = taskTitle.text.toString()
        val description = taskDescription.text.toString()
        val startHour = startTimePicker.hour
        val startMinute = startTimePicker.minute
        val endHour = endTimePicker.hour
        val endMinute = endTimePicker.minute

        // Save the task to Firebase
        val task = Task(title, description, startHour, startMinute, endHour, endMinute)
        val taskRef = FirebaseDatabase.getInstance().getReference("tasks").push()
        taskRef.setValue(task)

        // Set the task reminder time
        val taskTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
        }

        // Set the reminder alarm 15 minutes before the task start time
        setTaskReminder(this, taskRef.key!!, taskTime)

        Toast.makeText(this, "Task saved and reminder set!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setTaskReminder(context: Context, taskId: String, taskTime: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create an Intent to trigger the AlarmReceiver
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("taskTitle", taskTitle.text.toString())  // Pass task details

        // Create a PendingIntent to be triggered when the alarm goes off
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm to go off 15 minutes before the task start time
        val triggerTime = taskTime.timeInMillis - (15 * 60 * 1000)  // 15 minutes before

        // Log the alarm setting
        Log.d("Alarm", "Setting alarm for: $triggerTime")

        // Set the alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
}

data class Task(
    val title: String = "",
    val description: String = "",
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 0,
    val endMinute: Int = 0
)
