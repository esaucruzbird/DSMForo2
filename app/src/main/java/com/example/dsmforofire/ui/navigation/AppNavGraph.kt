package com.example.dsmforofire.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dsmforofire.ui.screens.auth.LoginScreen
import com.example.dsmforofire.ui.screens.auth.RegisterScreen
import com.example.dsmforofire.ui.screens.expense.ExpenseScreen
import com.example.dsmforofire.ui.screens.history.HistoryScreen
import com.example.dsmforofire.ui.screens.home.HomeScreen
import com.example.dsmforofire.viewmodel.AuthViewModel
import com.example.dsmforofire.viewmodel.ExpenseViewModel

@Composable
fun ControlGastosApp(
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    if (authViewModel.currentUser == null) {
        AuthGraph(authViewModel)
    } else {
        MainGraph(authViewModel, expenseViewModel)
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
private fun MainGraph(
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                userEmail = authViewModel.currentUser?.email.orEmpty(),
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