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
            subtitle = "Quran, Hadith, Seerat & History",
            jsonFile = "questions_islamiat.json",
            colorRes = R.color.card_islamiat,
            emoji = "\u262A\uFE0F"
        ),
        Category(
            id = "gk",
            title = "General Knowledge",
            subtitle = "Pak Study, Current Affairs & Test Prep",
            jsonFile = "questions_gk.json",
            colorRes = R.color.card_gk,
            emoji = "\uD83C\uDF0D"
        ),
        Category(
            id = "science",
            title = "General Science",
            subtitle = "Everyday Science for All Tests",
            jsonFile = "questions_science.json",
            colorRes = R.color.card_science,
            emoji = "\uD83D\uDD2C"
        ),
        Category(
            id = "physics",
            title = "Physics",
            subtitle = "Mechanics, Electricity & More",
            jsonFile = "questions_physics.json",
            colorRes = R.color.card_physics,
            emoji = "\u269B\uFE0F"
        ),
        Category(
            id = "chemistry",
            title = "Chemistry",
            subtitle = "Elements, Reactions & Compounds",
            jsonFile = "questions_chemistry.json",
            colorRes = R.color.card_chemistry,
            emoji = "\uD83E\uDDEA"
        ),
        Category(
            id = "biology",
            title = "Biology",
            subtitle = "Human Body, Genetics & Life Science",
            jsonFile = "questions_biology.json",
            colorRes = R.color.card_biology,
            emoji = "\uD83E\uDDEC"
        ),
        Category(
            id = "english",
            title = "English",
            subtitle = "Grammar, Vocabulary & Usage",
            jsonFile = "questions_english.json",
            colorRes = R.color.card_english,
            emoji = "\uD83D\uDCDA"
        ),
        Category(
            id = "math",
            title = "Mathematics",
            subtitle = "Arithmetic, Algebra & Reasoning",
            jsonFile = "questions_math.json",
            colorRes = R.color.card_math,
            emoji = "\u2795"
        )
    )
}
