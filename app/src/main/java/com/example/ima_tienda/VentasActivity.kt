package com.example.ima_tienda

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
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
    private lateinit var searchView: SearchView

    private var feriaSeleccionadaId: Int = -1
    private var listaPedidos: List<Pedidos_pendientes> = emptyList()

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
        searchView = findViewById(R.id.searchView)

        // Obtener el ID de la feria seleccionada
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        feriaSeleccionadaId = sharedPreferences.getInt("feria_seleccionada", -1)

        // Llamar al método para cargar los pedidos
        loadPedidos()

        // Configurar SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No se hace nada al enviar la búsqueda
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrar los pedidos según el texto ingresado
                val filteredList = listaPedidos.filter {
                    it.cedula_cliente.contains(newText ?: "", ignoreCase = true)
                }
                val pedidosAgrupados = groupPedidos(filteredList)
                pedidosAdapter.updateData(pedidosAgrupados)
                return true
            }
        })
    }

    private fun loadPedidos() {
        loadingAnimation.visibility = View.VISIBLE

        recyclerView.layoutManager = LinearLayoutManager(this)

        pedidosAdapter = PedidosAdapter(emptyList(), onCancelarClick = { pedido ->
            cancelarPedido(pedido)
        }, onPagarClick = { pedido ->
            procesarPago(pedido) // Procesar el pago al hacer clic en pagar
        })
        recyclerView.adapter = pedidosAdapter

        if (feriaSeleccionadaId != -1) {
            apiService.obtenerPedidosPorFeria(feriaSeleccionadaId).enqueue(object : Callback<List<Pedidos_pendientes>> {
                override fun onResponse(call: Call<List<Pedidos_pendientes>>, response: Response<List<Pedidos_pendientes>>) {
                    if (response.isSuccessful) {
                        val pedidos = response.body()
                        if (pedidos != null) {
                            listaPedidos = pedidos // Guardamos la lista completa de pedidos
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
        loadingAnimation.visibility = View.VISIBLE

        apiService.cancelarPedido(pedido.pedido_id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VentasActivity, "Pedido cancelado correctamente", Toast.LENGTH_SHORT).show()
                    loadPedidos()
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

    private fun procesarPago(pedido: PedidoAgrupado) {
        loadingAnimation.visibility = View.VISIBLE

        // Aquí deberías agregar la lógica para procesar el pago
        // Ejemplo: llamar a un servicio de pago o redirigir a una pantalla de pago
        apiService.procesarPago(pedido.pedido_id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VentasActivity, "Pago procesado correctamente", Toast.LENGTH_SHORT).show()
                    loadPedidos() // Recargar los pedidos
                } else {
                    Toast.makeText(this@VentasActivity, "Error al procesar el pago", Toast.LENGTH_SHORT).show()
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
