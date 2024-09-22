package com.sliit.taskdb

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val splashLogo: ImageView = findViewById(R.id.splashLogo)
        val appName: TextView = findViewById(R.id.appName)

        // Load and apply the spinning and zoom-out animation
        val spinZoomOutAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_zoom_out)
        splashLogo.startAnimation(spinZoomOutAnimation)

        // Delay for splash screen transition
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3 seconds
    }
}
