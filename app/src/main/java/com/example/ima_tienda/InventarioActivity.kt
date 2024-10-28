package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class InventarioActivity : AppCompatActivity() {

    private lateinit var menuRecyclerView: RecyclerView

    // Reemplaza R.drawable.product y R.drawable.fair con los nombres reales de tus imágenes en la carpeta drawable.
    private val menuItems = listOf(
        MenuItem("Gestión de Productos", R.drawable.productos),
        MenuItem("Gestión de Ferias", R.drawable.ferias)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        menuRecyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = MenuAdapter(menuItems) { menuItem ->
            when (menuItem.title) {
                "Gestión de Productos" -> startActivity(Intent(this, GestionarProductosActivity::class.java))
                "Gestión de Ferias" -> startActivity(Intent(this, GestionarFeriasActivity::class.java))
            }
        }
        menuRecyclerView.adapter = adapter
    }
}
