package com.example.dsmforofire.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dsmforofire.data.ExpenseRepository
import com.example.dsmforofire.data.FirebaseRefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseRefs.auth
    private val repo = ExpenseRepository()

    var currentUser by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private val listener = FirebaseAuth.AuthStateListener {
        currentUser = it.currentUser
    }

    init {
        auth.addAuthStateListener(listener)
    }

    fun login(email: String, password: String) {
        error = null

        if (email.isBlank() || password.isBlank()) {
            error = "Completa correo y contraseña."
            return
        }

        loading = true
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                loading = false
                currentUser = auth.currentUser
            }
            .addOnFailureListener { e ->
                loading = false
                error = e.message ?: "No se pudo iniciar sesión."
            }
    }

    fun register(email: String, password: String) {
        error = null

        if (email.isBlank() || password.isBlank()) {
            error = "Completa correo y contraseña."
            return
        }

        if (password.length < 6) {
            error = "La contraseña debe tener al menos 6 caracteres."
            return
        }

        loading = true
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                val user = auth.currentUser
                if (user != null) {
                    repo.createOrUpdateUserProfile(
                        uid = user.uid,
                        email = user.email.orEmpty(),
                        onSuccess = {
                            loading = false
                            currentUser = auth.currentUser
                        },
                        onError = { msg ->
                            loading = false
                            error = msg
                        }
                    )
                } else {
                    loading = false
                    error = "No se pudo obtener el usuario creado."
                }
            }
            .addOnFailureListener { e ->
                loading = false
                error = e.message ?: "No se pudo registrar el usuario."
            }
    }

    fun logout() {
        auth.signOut()
        currentUser = null
    }

    override fun onCleared() {
        auth.removeAuthStateListener(listener)
        super.onCleared()
    }
}