package com.pakquiz.app

import java.io.Serializable

data class Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    var selectedIndex: Int = -1
) : Serializable {
    // Stable id derived from the question text itself - no need to hand-maintain IDs
    // in the JSON files. Used by the rotation system to remember which questions a
    // user has already seen for a given subject.
    val id: String get() = question.hashCode().toString()
}

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val jsonFile: String,
    val colorRes: Int,
    val emoji: String
)
