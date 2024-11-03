package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Clase de datos para cada elemento del menú
data class MenuItem(val title: String, val iconResId: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var menuRecyclerView: RecyclerView

    // Reemplaza R.drawable.qr, R.drawable.tips, R.drawable.box e R.drawable.icon_inventario
    // con los nombres reales de tus imágenes en la carpeta drawable.
    private val menuItems = listOf(
        MenuItem("Verificador", R.drawable.qr),
//        MenuItem("Caja", R.drawable.tips),
//        MenuItem("Despachador", R.drawable.box),
        MenuItem("Inventario", R.drawable.inventory) // Agregar nuevo ítem "Inventario"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        menuRecyclerView.layoutManager = GridLayoutManager(this, 2)

        val adapter = MenuAdapter(menuItems) { menuItem ->
            when (menuItem.title) {
                "Verificador" -> startActivity(Intent(this, VerificarPersonaActivity::class.java))
                //"Caja" -> startActivity(Intent(this, CajaActivity::class.java)) // Descomentar si la actividad existe
                //"Despachador" -> startActivity(Intent(this, DespachadorActivity::class.java)) // Descomentar si la actividad existe
                "Inventario" -> startActivity(Intent(this, InventarioActivity::class.java)) // Nueva actividad para Inventario
            }
        }
        menuRecyclerView.adapter = adapter
    }
}
