package com.example.dsmforofire.ui.screens

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dsmforofire.data.ExpenseRepository
import com.example.dsmforofire.data.FirebaseRefs
import com.example.dsmforofire.viewmodel.AuthViewModel
import com.example.dsmforofire.viewmodel.ExpenseViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GastosApp(
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    expenseViewModel: ExpenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val currentUser = authViewModel.currentUser

    if (currentUser == null) {
        AuthGraph(authViewModel)
    } else {
        AppGraph(
            authViewModel = authViewModel,
            expenseViewModel = expenseViewModel,
            userEmail = currentUser.email.orEmpty()
        )
    }
}

@Composable
private fun AuthGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onCreateAccount = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun AppGraph(
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel,
    userEmail: String
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                userEmail = userEmail,
                onControlGastos = { navController.navigate("expense") },
                onHistory = { navController.navigate("history") },
                onLogout = { authViewModel.logout() }
            )
        }
        composable("expense") {
            ExpenseScreen(
                onBack = { navController.popBackStack() },
                expenseViewModel = expenseViewModel
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                expenseViewModel = expenseViewModel
            )
        }
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onCreateAccount: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val repo = remember { ExpenseRepository() }
    var googleError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                googleError = null
                startGoogleSignIn(activity) { error ->
                    googleError = error
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar con Google")
        }

        if (authViewModel.loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        authViewModel.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        googleError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.register(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a login")
        }

        if (authViewModel.loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        authViewModel.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

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
        Text("Bienvenido, $userName", style = MaterialTheme.typography.headlineMedium)
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

@Composable
fun ExpenseScreen(
    onBack: () -> Unit,
    expenseViewModel: ExpenseViewModel
) {
    val user = FirebaseRefs.currentUser
    val uid = user?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Registrar gasto", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del gasto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Fecha (dd/MM/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (uid.isBlank()) return@Button

                expenseViewModel.addExpense(
                    uid = uid,
                    name = name,
                    amountText = amount,
                    category = category,
                    dateText = date
                ) {
                    name = ""
                    amount = ""
                    category = ""
                    date = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar registro")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                name = ""
                amount = ""
                category = ""
                date = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar campos")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        expenseViewModel.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (expenseViewModel.loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    expenseViewModel: ExpenseViewModel
) {
    val user = FirebaseRefs.currentUser
    val uid = user?.uid ?: ""
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

    val filtered = remember(expenseViewModel.expenses, monthFilter) {
        if (monthFilter.isBlank()) {
            expenseViewModel.expenses
        } else {
            expenseViewModel.expenses.filter { it.monthKey == monthFilter.trim() }
        }
    }

    val grouped = remember(filtered) {
        filtered.groupBy { it.monthKey }
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

        if (expenseViewModel.loading) {
            CircularProgressIndicator()
        }

        expenseViewModel.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            grouped.forEach { (monthKey, items) ->
                val total = items.sumOf { it.amount }

                item {
                    Text(
                        text = repo.formatMonthLabel(monthKey),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items.forEach { expense ->
                    item {
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

private fun startGoogleSignIn(
    activity: ComponentActivity,
    onError: (String?) -> Unit
) {
    val credentialManager = CredentialManager.create(activity)
    val auth = FirebaseRefs.auth

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(activity.getString(R.string.default_web_client_id))
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    activity.lifecycleScope.launch {
        try {
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = result.credential

            if (
                credential is CustomCredential &&
                credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCred.idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnFailureListener { e ->
                        onError(e.message ?: "No se pudo iniciar sesión con Google.")
                    }
            } else {
                onError("La credencial recibida no es válida para Google.")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Error al iniciar sesión con Google.")
        }
    }
}