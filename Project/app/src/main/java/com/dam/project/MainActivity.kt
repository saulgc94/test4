package com.dam.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //inicializar la bbdd en el nodo de users
        database = Firebase.database.getReference("Users")
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener{
            login()
        }
    }
    fun login(){
        //recoger las variables
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        //disponer a la bbd de un listener
        database.get().addOnSuccessListener { datasnap ->
            val users = datasnap.value as? HashMap<String, Any>
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }else if (users != null) {
                if(!users.containsKey(username)){
                    Toast.makeText(this, "No existe el usuario escrito", Toast.LENGTH_SHORT).show()

                } else if (users.containsKey(username) && users[username] == password) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    // Aquí puedes iniciar la siguiente actividad o realizar otras acciones

                    // Obtén una referencia al objeto SharedPreferences
                    val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)

                    // Obtiene un editor para realizar cambios en SharedPreferences
                    val editor = sharedPreferences.edit()

                    // Guarda el nombre de usuario en SharedPreferences
                    editor.putString("username", username)

                    // Aplica los cambios
                    editor.apply()

                    val intent = Intent(this, ReservaSalaActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }



    }
}




