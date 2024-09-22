package com.sliit.taskdb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class EditTaskActivity : AppCompatActivity() {
    private lateinit var taskTitleEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // Get references to UI elements
        taskTitleEditText = findViewById(R.id.taskTitleEditText)
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText)
        saveButton = findViewById(R.id.saveButton)

        // Get task details passed from TaskDetailActivity
        val taskId = intent.getStringExtra("taskId")
        val taskTitle = intent.getStringExtra("taskTitle")
        val taskDescription = intent.getStringExtra("taskDescription")

        // Populate fields with existing task data
        taskTitleEditText.setText(taskTitle)
        taskDescriptionEditText.setText(taskDescription)

        // Handle Save Button click
        saveButton.setOnClickListener {
            val updatedTitle = taskTitleEditText.text.toString()
            val updatedDescription = taskDescriptionEditText.text.toString()

            // Update task in Firebase
            val taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(taskId!!)
            taskRef.child("title").setValue(updatedTitle)
            taskRef.child("description").setValue(updatedDescription)

            Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show()
            finish()  // Close the activity after saving
        }
    }
}
