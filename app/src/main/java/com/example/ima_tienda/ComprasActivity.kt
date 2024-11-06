

package com.example.ima_tienda

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComprasActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var comprasAdapter: ComprasAdapter
    private lateinit var clienteTextView: TextView
    private var cedula: String? = null // Almacenar la cédula del cliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras)

        // Iniciar ApiService
        apiService = ApiClient.getApiService()
        clienteTextView = findViewById(R.id.text_cliente)
        // Configuración de RecyclerView para mostrar las compras
        recyclerView = findViewById(R.id.recycler_compras)
        recyclerView.layoutManager = LinearLayoutManager(this)
        comprasAdapter = ComprasAdapter(emptyList()) // Inicialmente vacío
        recyclerView.adapter = comprasAdapter

        // Obtener la cédula desde la sesión o intent
        cedula = intent.getStringExtra("CEDULA") ?: ""
        clienteTextView.text = "Cliente: ${cedula}"
        clienteTextView = findViewById(R.id.text_cliente)

        // Cargar las compras del cliente
        loadCompras()

        // Botón para volver
        val backButton: Button = findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish() // Regresar a la actividad anterior
        }
    }


    private fun loadCompras() {
        if (cedula != null) {
            apiService.obtenerComprasPorCedula(cedula!!).enqueue(object : Callback<List<Compra>> {
                override fun onResponse(call: Call<List<Compra>>, response: Response<List<Compra>>) {
                    if (response.isSuccessful) {
                        val compras = response.body()
                        if (compras != null) {
                            // Actualizar el adapter con las compras
                            comprasAdapter.updateData(compras)
                        } else {
                            Toast.makeText(this@ComprasActivity, "No se encontraron compras", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ComprasActivity, "Error al cargar compras", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Compra>>, t: Throwable) {
                    Toast.makeText(this@ComprasActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
