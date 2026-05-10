package com.example.dsmforofire.data

data class Expense(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val dateMillis: Long = 0L,
    val monthKey: String = "" // yyyy-MM
)