package com.pakquiz.app

data class Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val jsonFile: String
)
