package com.sliit.taskdb

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MainActivity : AppCompatActivity() {
    private lateinit var taskListView: ListView
    private lateinit var createTaskButton: Button

    private val POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 1
    private val taskList = mutableListOf<TaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskListView = findViewById(R.id.taskListView)
        createTaskButton = findViewById(R.id.createTaskButton)

        // Navigate to CreateTaskActivity when the button is clicked
        createTaskButton.setOnClickListener {
            startActivity(Intent(this, CreateTaskActivity::class.java))
        }

        // Load tasks from Firebase and update the ListView
        loadTasksFromFirebase()

        // Handle task item click
        taskListView.setOnItemClickListener { _, _, position, _ ->
            val selectedTask = taskList[position]
            // Start TaskDetailsActivity and pass task details using Intent
            val intent = Intent(this, TaskDetailsActivity::class.java).apply {
                putExtra("taskId", selectedTask.id)  // Pass Firebase key for edit/delete
                putExtra("taskTitle", selectedTask.title)
                putExtra("taskDescription", selectedTask.description)
            }
            startActivity(intent)
        }

        // Create notification channel for Android 8.0+
        createNotificationChannel()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }
    }

    // Request notification permission for Android 13+
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE) {
            val message = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                "Notification permission granted"
            } else {
                "Notification permission denied"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTasksFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("tasks")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()  // Clear the list before updating

                for (taskSnapshot in snapshot.children) {
                    val id = taskSnapshot.key
                    val title = taskSnapshot.child("title").getValue(String::class.java)
                    val description = taskSnapshot.child("description").getValue(String::class.java)

                    if (id != null && title != null && description != null) {
                        // Create a TaskItem object and add it to the taskList
                        val task = TaskItem(id, title, description)
                        taskList.add(task)
                    }
                }

                // Update the ListView with task titles
                updateTaskListView()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load tasks.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTaskListView() {
        val taskTitles = taskList.map { it.title }
        taskListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskTitles)
    }

    // Create notification channel for Android 8.0+
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "taskReminderChannel",
                "Task Reminder Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Task Reminder Notifications"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

data class TaskItem(val id: String, val title: String, val description: String)
