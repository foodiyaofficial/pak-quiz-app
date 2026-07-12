package com.pakquiz.app

import android.content.Context
import org.json.JSONArray

object QuestionBank {

    fun load(context: Context, fileName: String): List<Question> {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val array = JSONArray(jsonString)
        val list = mutableListOf<Question>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val optionsArray = obj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optionsArray.length()) {
                options.add(optionsArray.getString(j))
            }
            list.add(
                Question(
                    question = obj.getString("question"),
                    options = options,
                    correctIndex = obj.getInt("correctIndex")
                )
            )
        }
        return list.shuffled()
    }

    val categories = listOf(
        Category(
            id = "islamiat",
            title = "Islamic Studies",
            subtitle = "Quran, Hadith, Seerat & Islamic History",
            jsonFile = "questions_islamiat.json"
        ),
        Category(
            id = "gk",
            title = "General Knowledge",
            subtitle = "Pak Study, Current Affairs & Test Prep (NTS/PPSC/CSS)",
            jsonFile = "questions_gk.json"
        )
    )
}
