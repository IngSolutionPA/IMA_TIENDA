package com.example.ima_tienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.edit_username)
        passwordEditText = findViewById(R.id.edit_password)
        loginButton = findViewById(R.id.button_login)

        apiService = ApiClient.getApiService() // Inicia tu ApiService

        loginButton.setOnClickListener {
            val cedula = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Realiza la verificación de usuario y contraseña
            loginUsuario(cedula, password)
        }
    }

    private fun loginUsuario(cedula: String, password: String) {
        val loginRequest = LoginRequest(cedula, password)

        // Llama al servicio API para validar login
        apiService.loginUsuario(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse != null) {
                        if (loginResponse.success) {
                            // Guardar sesión del usuario
                            val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("cedula", cedula)
                            editor.putInt("nivel", loginResponse.nivel) // Cambiar a putInt para 'nivel'
                            editor.putString("nombre", loginResponse.nombre)
                            val feriasJson = Gson().toJson(loginResponse.ferias)
                            editor.putString("ferias", feriasJson)
                            editor.apply()

                            // Redirige a la MainActivity
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Respuesta nula del servidor", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
