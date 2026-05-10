package com.example.dsmforofire.data

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseRepository {

    private val db = FirebaseRefs.firestore

    fun createOrUpdateUserProfile(
        uid: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val name = email.substringBefore("@").ifBlank { "Usuario" }

        val data = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to name
        )

        db.collection("users")
            .document(uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "No se pudo guardar el perfil") }
    }

    fun saveExpense(
        uid: String,
        expense: Expense,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "No se pudo guardar el gasto") }
    }

    fun listenExpenses(
        uid: String,
        onData: (List<Expense>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection("users")
            .document(uid)
            .collection("expenses")
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Error al leer gastos")
                    return@addSnapshotListener
                }

                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                }

                onData(items)
            }
    }

    fun parseDateToMillis(dateText: String): Long? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(dateText.trim())
            date?.time
        } catch (_: Exception) {
            null
        }
    }

    fun formatMonthKey(dateMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date(dateMillis))
    }

    fun formatMonthLabel(monthKey: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val output = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            val date = input.parse(monthKey)
            if (date != null) output.format(date).replaceFirstChar { it.uppercase() } else monthKey
        } catch (_: Exception) {
            monthKey
        }
    }

    fun formatDate(dateMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(dateMillis))
    }
}