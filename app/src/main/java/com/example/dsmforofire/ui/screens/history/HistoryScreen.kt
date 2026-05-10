package com.example.dsmforofire.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dsmforofire.data.ExpenseRepository
import com.example.dsmforofire.data.FirebaseRefs
import com.example.dsmforofire.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    expenseViewModel: ExpenseViewModel
) {
    val uid = FirebaseRefs.currentUser?.uid.orEmpty()
    val repo = remember { ExpenseRepository() }

    var monthFilter by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            expenseViewModel.startListening(uid)
        }
    }

    DisposableEffect(Unit) {
        onDispose { expenseViewModel.stopListening() }
    }

    val visibleExpenses = remember(expenseViewModel.expenses, monthFilter) {
        if (monthFilter.isBlank()) {
            expenseViewModel.expenses
        } else {
            expenseViewModel.expenses.filter { it.monthKey == monthFilter.trim() }
        }
    }

    val grouped = remember(visibleExpenses) {
        visibleExpenses
            .groupBy { it.monthKey }
            .toSortedMap(compareByDescending { it })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Historial de gastos", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = monthFilter,
            onValueChange = { monthFilter = it },
            label = { Text("Filtrar por mes (YYYY-MM)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { monthFilter = monthFilter.trim() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Filtrar")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { monthFilter = "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Quitar filtro")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        Spacer(Modifier.height(16.dp))

        expenseViewModel.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (expenseViewModel.loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (grouped.isEmpty()) {
                item {
                    Text("No hay gastos para mostrar.")
                }
            } else {
                grouped.forEach { (monthKey, monthExpenses) ->
                    val total = monthExpenses.sumOf { it.amount }

                    item {
                        Text(
                            text = repo.formatMonthLabel(monthKey),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    items(monthExpenses.size) { index ->
                        val expense = monthExpenses[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Gasto: ${expense.name}")
                                Text("Monto: ${String.format(Locale.getDefault(), "%.2f", expense.amount)}")
                                Text("Categoría: ${expense.category}")
                                Text("Fecha: ${repo.formatDate(expense.dateMillis)}")
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Total del mes: ${String.format(Locale.getDefault(), "%.2f", total)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}