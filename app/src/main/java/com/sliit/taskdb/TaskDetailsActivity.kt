package com.sliit.taskdb

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var taskTitleTextView: TextView
    private lateinit var taskDescriptionTextView: TextView
    private lateinit var taskStartTimeTextView: TextView
    private lateinit var taskEndTimeTextView: TextView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        // Initialize UI components
        taskTitleTextView = findViewById(R.id.taskTitleTextView)
        taskDescriptionTextView = findViewById(R.id.taskDescriptionTextView)
        taskStartTimeTextView = findViewById(R.id.taskStartTimeTextView)
        taskEndTimeTextView = findViewById(R.id.taskEndTimeTextView)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)

        // Get taskId from Intent
        val taskId = intent.getStringExtra("taskId")

        if (taskId != null) {
            // Retrieve task from Firebase using taskId
            val taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(taskId)
            taskRef.get().addOnSuccessListener { snapshot ->
                val task = snapshot.getValue(Task::class.java)

                if (task != null) {
                    // Set task details in TextViews
                    taskTitleTextView.text = task.title
                    taskDescriptionTextView.text = task.description

                    // Safely format start and end times to a readable format (HH:MM)
                    val startTime = formatTime(task.startHour, task.startMinute)
                    val endTime = formatTime(task.endHour, task.endMinute)

                    taskStartTimeTextView.text = "Start Time: $startTime"
                    taskEndTimeTextView.text = "End Time: $endTime"
                } else {
                    Log.e("TaskDetailsActivity", "Task not found in Firebase")
                    Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show()
                    finish() // Close activity if task is not found
                }
            }.addOnFailureListener { exception ->
                Log.e("TaskDetailsActivity", "Failed to retrieve task", exception)
                Toast.makeText(this, "Failed to load task", Toast.LENGTH_SHORT).show()
                finish() // Close activity on error
            }
        } else {
            Toast.makeText(this, "Invalid task ID", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no taskId is passed
        }

        // Handle Edit Button click
        editButton.setOnClickListener {
            val editIntent = Intent(this, EditTaskActivity::class.java).apply {
                putExtra("taskId", taskId)
                putExtra("taskTitle", taskTitleTextView.text.toString())
                putExtra("taskDescription", taskDescriptionTextView.text.toString())
                // Extract times if available
                putExtra("taskStartHour", extractHourFromTime(taskStartTimeTextView.text.toString()))
                putExtra("taskStartMinute", extractMinuteFromTime(taskStartTimeTextView.text.toString()))
                putExtra("taskEndHour", extractHourFromTime(taskEndTimeTextView.text.toString()))
                putExtra("taskEndMinute", extractMinuteFromTime(taskEndTimeTextView.text.toString()))
            }
            Log.d("TaskDetailsActivity", "Starting EditTaskActivity with - Start Hour: ${extractHourFromTime(taskStartTimeTextView.text.toString())}, Start Minute: ${extractMinuteFromTime(taskStartTimeTextView.text.toString())}")
            startActivity(editIntent)
        }

        // Handle Delete Button click
        deleteButton.setOnClickListener {
            taskId?.let { id -> showDeleteConfirmation(id) }
        }
    }

    private fun formatTime(hour: Int?, minute: Int?): String {
        return if (hour != null && minute != null) {
            String.format("%02d:%02d", hour, minute)
        } else {
            "N/A"
        }
    }

    private fun showDeleteConfirmation(taskId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the task from Firebase
                FirebaseDatabase.getInstance().getReference("tasks").child(taskId).removeValue()
                Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity after deleting the task
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }  // Close the dialog without doing anything
            .create()
            .show()
    }

    // Utility functions to extract time components from TextView strings
    private fun extractHourFromTime(time: String): Int {
        val parts = time.split(":")
        return if (parts.size == 2) parts[0].toIntOrNull() ?: 0 else 0
    }

    private fun extractMinuteFromTime(time: String): Int {
        val parts = time.split(":")
        return if (parts.size == 2) parts[1].toIntOrNull() ?: 0 else 0
    }
}
