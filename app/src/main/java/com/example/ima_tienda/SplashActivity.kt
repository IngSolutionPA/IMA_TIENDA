package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Asegúrate de que este archivo XML esté configurado correctamente.

        // Duración del Splash Screen
        val splashScreenDuration = 2000L

        // Inicia la com.example.ima_tienda.MainActivity después de la duración del splash
        android.os.Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finaliza SplashActivity para que no regrese con el botón atrás
        }, splashScreenDuration)
    }
}

