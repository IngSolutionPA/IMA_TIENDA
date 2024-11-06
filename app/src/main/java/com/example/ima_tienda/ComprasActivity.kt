

package com.example.ima_tienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComprasActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var comprasAdapter: ComprasAdapter
    private lateinit var clienteTextView: TextView
    private lateinit var loadingAnimation: LottieAnimationView
    private var cedula: String? = null // Almacenar la cédula del cliente
    private lateinit var logoutIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras)

        // Iniciar ApiService
        apiService = ApiClient.getApiService()
        logoutIcon = findViewById(R.id.logout_icon)
        clienteTextView = findViewById(R.id.text_cliente)
        // Configuración de RecyclerView para mostrar las compras
        recyclerView = findViewById(R.id.recycler_compras)
        recyclerView.layoutManager = LinearLayoutManager(this)
        comprasAdapter = ComprasAdapter(emptyMap()) // Inicializa con un Map vacío
        recyclerView.adapter = comprasAdapter

        // Obtener la cédula desde la sesión o intent
        cedula = intent.getStringExtra("CEDULA") ?: ""
        clienteTextView.text = "Cliente: ${cedula}"
        loadingAnimation = findViewById(R.id.loadingAnimation)
        // Cargar las compras del cliente
        loadCompras()

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


    private fun loadCompras() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        if (cedula != null) {
            apiService.obtenerComprasPorCedula(cedula!!).enqueue(object : Callback<List<Compra>> {
                override fun onResponse(call: Call<List<Compra>>, response: Response<List<Compra>>) {
                    if (response.isSuccessful) {
                        val compras = response.body()
                        if (compras != null) {
                            // Agrupar las compras por id
                            val comprasAgrupadas = compras.groupBy { it.id }
                            // Actualizar el adapter con las compras agrupadas
                            comprasAdapter.updateData(comprasAgrupadas)
                        } else {
                            Toast.makeText(this@ComprasActivity, "No se encontraron compras", Toast.LENGTH_SHORT).show()
                        }
                        loadingAnimation.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@ComprasActivity, "Error al cargar compras", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Compra>>, t: Throwable) {
                    Toast.makeText(this@ComprasActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    loadingAnimation.visibility = View.GONE
                }
            })
        }
    }

}
