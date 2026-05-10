package com.example.dsmforofire.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dsmforofire.data.Expense
import com.example.dsmforofire.data.ExpenseRepository
import com.example.dsmforofire.data.FirebaseRefs
import com.google.firebase.firestore.ListenerRegistration

class ExpenseViewModel : ViewModel() {

    private val repo = ExpenseRepository()
    private var listenerRegistration: ListenerRegistration? = null

    var expenses by mutableStateOf<List<Expense>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun startListening(uid: String) {
        stopListening()
        loading = true
        error = null

        listenerRegistration = repo.listenExpenses(
            uid = uid,
            onData = {
                expenses = it
                loading = false
            },
            onError = {
                error = it
                loading = false
            }
        )
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun addExpense(
        uid: String,
        name: String,
        amountText: String,
        category: String,
        dateText: String,
        onSuccess: () -> Unit
    ) {
        error = null

        if (name.isBlank() || amountText.isBlank() || category.isBlank() || dateText.isBlank()) {
            error = "Completa todos los campos del gasto."
            return
        }

        val amount = amountText.replace(",", ".").toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            error = "El monto debe ser un número mayor que 0."
            return
        }

        val dateMillis = repo.parseDateToMillis(dateText)
        if (dateMillis == null) {
            error = "La fecha debe tener formato dd/MM/yyyy."
            return
        }

        val expense = Expense(
            name = name.trim(),
            amount = amount,
            category = category.trim(),
            dateMillis = dateMillis,
            monthKey = repo.formatMonthKey(dateMillis)
        )

        loading = true
        repo.saveExpense(
            uid = uid,
            expense = expense,
            onSuccess = {
                loading = false
                onSuccess()
            },
            onError = { msg ->
                loading = false
                error = msg
            }
        )
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}