package com.pakquiz.app

import java.io.Serializable

data class Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    var selectedIndex: Int = -1
) : Serializable

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val jsonFile: String,
    val colorRes: Int,
    val emoji: String
)
