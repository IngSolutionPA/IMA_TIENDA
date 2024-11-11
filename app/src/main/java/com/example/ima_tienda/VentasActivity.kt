package com.example.ima_tienda

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VentasActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var pedidosAdapter: PedidosAdapter
    private lateinit var loadingAnimation: LottieAnimationView

    // Variable para almacenar el ID de la feria seleccionada
    private var feriaSeleccionadaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)
        window.statusBarColor = Color.parseColor("#1b914b")

        // Inicializar vistas
        apiService = ApiClient.getApiService()
        loadingAnimation = findViewById(R.id.loadingAnimation)

        // Configuración del RecyclerView
        recyclerView = findViewById(R.id.recycler_compras)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Obtener el ID de la feria seleccionada desde SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        feriaSeleccionadaId = sharedPreferences.getInt("feria_seleccionada", -1)

        // Llamar al método para cargar las compras filtradas por feria
        loadPedidos()
    }

    private fun loadPedidos() {
        // Mostrar la animación de carga y configurar el RecyclerView
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador de pedidos con una lista vacía y un listener para cancelar pedidos
        pedidosAdapter = PedidosAdapter(emptyList()) { pedido ->
            cancelarPedido(pedido) // Llama a cancelarPedido cuando se hace clic en cancelar
        }
        recyclerView.adapter = pedidosAdapter

        // Verificar que haya un ID de feria válido
        if (feriaSeleccionadaId != -1) {
            apiService.obtenerPedidosPorFeria(feriaSeleccionadaId).enqueue(object : Callback<List<Pedidos_pendientes>> {
                override fun onResponse(call: Call<List<Pedidos_pendientes>>, response: Response<List<Pedidos_pendientes>>) {
                    if (response.isSuccessful) {
                        val pedidos = response.body()
                        if (pedidos != null) {
                            val pedidosAgrupados = groupPedidos(pedidos)
                            pedidosAdapter.updateData(pedidosAgrupados)
                        } else {
                            Toast.makeText(this@VentasActivity, "No se encontraron pedidos", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@VentasActivity, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                    }
                    loadingAnimation.visibility = View.GONE

                }

                override fun onFailure(call: Call<List<Pedidos_pendientes>>, t: Throwable) {

                    loadingAnimation.visibility = View.GONE
                }
            })
        } else {
            Toast.makeText(this@VentasActivity, "Feria no seleccionada", Toast.LENGTH_SHORT).show()
            loadingAnimation.visibility = View.GONE
        }
    }
    private fun cancelarPedido(pedido: PedidoAgrupado) {
        // Mostrar animación de carga
        loadingAnimation.visibility = View.VISIBLE

        apiService.cancelarPedido(pedido.pedido_id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VentasActivity, "Pedido cancelado correctamente", Toast.LENGTH_SHORT).show()
                    loadPedidos() // Recargar la lista de pedidos
                } else {
                    Toast.makeText(this@VentasActivity, "Error al cancelar el pedido", Toast.LENGTH_SHORT).show()
                }
                loadingAnimation.visibility = View.GONE

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@VentasActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                loadingAnimation.visibility = View.GONE

            }
        })
    }

}
