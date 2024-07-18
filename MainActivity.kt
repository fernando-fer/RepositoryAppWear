
package com.example.proyectomes1.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.proyectomes1.R
import com.example.proyectomes1.presentation.theme.ProyectoMes1Theme
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Button

class MainActivity : ComponentActivity() { 
    var URL_BASE = "https://sigaemail.host8b.me/"
    var CHANNEL_ID = "new_order_channel"
    var NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            ProyectoMes1Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoginClicked = { username, password ->
                            if (verifyCredentials(username, password)) {
                                navController.navigate("home")
                            }
                        })
                    }

                    composable("home") {
                        val listaPedidos = remember { mutableStateListOf<ReunionesModel>() }
                        val previousSize = remember { mutableStateOf(0) }

                        PedidoList(listaPedidos) {
                            navController.navigate("login")
                        }

                        // Lanzar una corrutina para obtener los datos de la lista
                        LaunchedEffect(Unit) {
                            val retrofit = Retrofit.Builder()
                                .baseUrl(URL_BASE)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()

                            val service = retrofit.create(webService::class.java)

                            while (true) {
                                try {
                                    val response = service.getList()
                                    listaPedidos.clear()
                                    listaPedidos.addAll(response)
                                    response.forEach { pedido ->
                                        Log.d("API_RESPONSE", pedido.toString())
                                    }
                                    if (listaPedidos.size > previousSize.value) {
                                        sendNotification(listaPedidos.last())
                                        previousSize.value = listaPedidos.size
                                    }
                                } catch (e: Exception) {
                                    // Manejar cualquier error aquí
                                    e.printStackTrace()
                                }
                                delay(10000)
                            }
                        }
                    }
                }
            }
        }
    }

    var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Maneja el caso donde el permiso no ha sido concedido
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Order Channel"
            val descriptionText = "Channel for new order notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val userCredentials = mapOf(
        "joox" to "123456789",
        "user1" to "password1",
        "user2" to "password2",
        "user3" to "password3"
    )

    private fun verifyCredentials(username: String, password: String): Boolean {
        return userCredentials[username] == password
    }

    private fun sendNotification(mensaje: ReunionesModel) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notificationText = "${mensaje.titulo} - ${mensaje.fecha}"

            // Intent para lanzar MainActivity cuando se hace clic en la notificación
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.splash_icon) // Reemplaza con tu ícono de notificación
                .setContentTitle("Nueva reunión")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } else {
            // Maneja el caso donde el permiso no ha sido concedido
            println("Permiso de notificaciones no concedido")
        }
    }
}
@Composable
fun LoginScreen(onLoginClicked: (String, String) -> Unit) {
    val scrollState = rememberScrollState()

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Agrega el logo aquí
                Image(
                    painter = painterResource(id = R.drawable.logo3),  // Asegúrate de que el archivo esté en res/drawable
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(50.dp) // Ajusta el tamaño del logo si es necesario
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "Inicio de Sesión",
                    fontSize = 15.sp,  // Tamaño del texto más pequeño
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val username = remember { mutableStateOf("") }
                val password = remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = username.value,
                        onValueChange = { username.value = it },
                        label = {
                            Text(
                                "Usuario",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = 10.sp  // Tamaño de la etiqueta más pequeño
                                )
                            )
                        },
                        textStyle = TextStyle(fontSize = 10.sp, color = Color.Black),  // Tamaño del texto más pequeño
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),  // Altura más pequeña
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = {
                            Text(
                                "Contraseña",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = 10.sp  // Tamaño de la etiqueta más pequeño
                                )
                            )
                        },
                        textStyle = TextStyle(fontSize = 10.sp, color = Color.Black),  // Tamaño del texto más pequeño
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),  // Altura más pequeña
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onLoginClicked(username.value, password.value) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp) // Altura del botón más pequeña
                    ) {
                        Text("Iniciar Sesión", fontSize = 10.sp)  // Tamaño del texto más pequeño
                    }
                }
            }
        }
    }
}
@Composable
fun PedidoList(listaPedidos: List<ReunionesModel>, onLogoutClicked: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo3),  // Asegúrate de que el archivo esté en res/drawable
            contentDescription = "Logo",
            modifier = Modifier
                .size(50.dp) // Ajusta el tamaño del logo si es necesario
                .padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaPedidos) { pedido ->
                val backgroundColor = if (listaPedidos.indexOf(pedido) % 2 == 0) {
                    Color.Black
                } else {
                    Color.Blue
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, MaterialTheme.shapes.medium)
                        .padding(30.dp)
                ) {
                    Text(
                        text = "${pedido.titulo} - ${pedido.fecha}",
                        style = MaterialTheme.typography.body1.copy(fontSize = 8.sp),
                        color = MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        Button(
            onClick = onLogoutClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079FF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),

        ) {
            Text("Cerrar Sesión", fontSize = 11.sp)
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ProyectoMes1Theme {
        LoginScreen { _, _ -> }
    }
}
