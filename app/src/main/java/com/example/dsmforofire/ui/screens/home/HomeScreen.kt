package com.example.dsmforofire.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    userEmail: String,
    onControlGastos: () -> Unit,
    onHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val userName = userEmail.substringBefore("@").ifBlank { "Usuario" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido, $userName",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onControlGastos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Control de gastos")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Historial de gastos")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}