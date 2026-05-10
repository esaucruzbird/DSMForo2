package com.example.dsmforofire.ui.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.dsmforofire.data.FirebaseRefs
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider

suspend fun signInWithGoogle(
    activity: ComponentActivity,
    webClientId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(activity)

    val googleOption = GetSignInWithGoogleOption.Builder(
        serverClientId = webClientId
    ).build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCred = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (_: GoogleIdTokenParsingException) {
                onError("La respuesta de Google no es válida.")
                return
            }

            val firebaseCredential = GoogleAuthProvider.getCredential(googleCred.idToken, null)

            FirebaseRefs.auth.signInWithCredential(firebaseCredential)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    onError(e.message ?: "No se pudo autenticar con Firebase.")
                }
        } else {
            onError("La credencial devuelta no corresponde a Google.")
        }
    } catch (e: Exception) {
        onError(e.message ?: "No se pudo iniciar sesión con Google.")
    }
}