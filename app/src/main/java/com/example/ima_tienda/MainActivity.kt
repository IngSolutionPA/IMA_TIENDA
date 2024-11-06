package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Clase de datos para cada elemento del menú
data class MenuItem(val title: String, val iconResId: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var logoutIcon: ImageView
    private lateinit var feriasSpinner: Spinner
    // Elementos de menú disponibles
    private val allMenuItems = listOf(
        MenuItem("Verificador", R.drawable.qr),
        MenuItem("Inventario", R.drawable.inventory)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        menuRecyclerView.layoutManager = GridLayoutManager(this, 2)
        logoutIcon = findViewById(R.id.logout_icon)
        feriasSpinner = findViewById(R.id.feriasSpinner)

        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val nombre = sharedPreferences.getString("nombre", "No disponible")
        val nivel = sharedPreferences.getInt("nivel", 0)

        val greetingTextView = findViewById<TextView>(R.id.textView2)
        greetingTextView.text = "🇵🇦 Hola, $nombre!"

        val feriasJson = sharedPreferences.getString("ferias", null)
        if (feriasJson != null) {
            val feriasList: List<Feria> = Gson().fromJson(feriasJson, object : TypeToken<List<Feria>>() {}.type)

            // Verifica si la lista de ferias está vacía
            val feriaNombres = if (feriasList.isEmpty()) {
                listOf("No hay ferias asignadas") // Mensaje por defecto si no hay ferias
            } else {
                feriasList.map { it.nombre } // Extrae solo los nombres si hay ferias
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, feriaNombres)
            feriasSpinner.adapter = adapter // Setea el adaptador al Spinner

            feriasSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    if (feriasList.isNotEmpty()) {
                        val selectedFeriaId = feriasList[position].id // Asegúrate de que `id` sea de tipo Int
                        val editor = sharedPreferences.edit()
                        editor.putInt("feria_seleccionada", selectedFeriaId) // Guarda el ID de la feria seleccionada
                        editor.apply()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Aquí puedes manejar el caso cuando no se seleccione nada (opcional)
                }
            })

        } else {
            Log.e("MainActivity", "No se encontraron ferias en SharedPreferences.")
        }


        // Filtrar elementos del menú según el nivel del usuario
        val filteredMenuItems = if (nivel == 1) {
            allMenuItems // Nivel 1: muestra todos los botones
        } else if(nivel == 2) {
            allMenuItems.filter { it.title == "Verificador" }
        } else {
            emptyList()
        }

        // Configurar el adaptador con los elementos de menú filtrados
        val adapter = MenuAdapter(filteredMenuItems) { menuItem ->
            when (menuItem.title) {
                "Verificador" -> startActivity(Intent(this, VerificarPersonaActivity::class.java))
                "Inventario" -> startActivity(Intent(this, InventarioActivity::class.java)) // Solo si el nivel permite
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
