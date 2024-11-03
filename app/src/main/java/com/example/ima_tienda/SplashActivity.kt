package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar la pantalla completa
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        setContentView(R.layout.activity_splash)

        // Duración del Splash Screen
        val splashScreenDuration = 2000L

        // Inicia la MainActivity después de la duración del splash
        android.os.Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finaliza SplashActivity para que no regrese con el botón atrás
        }, splashScreenDuration)
    }
}
