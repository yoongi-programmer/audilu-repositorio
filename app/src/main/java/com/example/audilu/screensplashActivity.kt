package com.example.audilu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class screensplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screensplash) // Use the splash screen layout

        // espera y navega usando Coroutine
        CoroutineScope(Dispatchers.Main).launch {
            delay(700) // Delay por 2seg
            val intent = Intent(this@screensplashActivity, audiluActivity::class.java)
            startActivity(intent)
            finish() // cierra el splash screen activity
        }
    }
}
