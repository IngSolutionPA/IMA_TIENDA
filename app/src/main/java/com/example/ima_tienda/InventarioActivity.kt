package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class InventarioActivity : AppCompatActivity() {

    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var logoutIcon: ImageView

    // Reemplaza R.drawable.product y R.drawable.fair con los nombres reales de tus imágenes en la carpeta drawable.
    private val menuItems = listOf(
        MenuItem("Gestión de Productos", R.drawable.productos),
        MenuItem("Gestión de Ferias", R.drawable.ferias)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)
        logoutIcon = findViewById(R.id.logout_icon)
        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        menuRecyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = MenuAdapter(menuItems) { menuItem ->
            when (menuItem.title) {
                "Gestión de Productos" -> startActivity(Intent(this, GestionarProductosActivity::class.java))
                "Gestión de Ferias" -> startActivity(Intent(this, GestionarFeriasActivity::class.java))
            }
        }
        menuRecyclerView.adapter = adapter
        // Configuración del botón de logout
        logoutIcon.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Eliminar datos de sesión de SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Regresar al LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Finaliza MainActivity
    }
}
