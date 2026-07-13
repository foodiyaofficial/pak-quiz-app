package com.pakquiz.app

import android.content.Context
import org.json.JSONArray

object QuestionBank {

    private const val QUESTIONS_PER_QUIZ = 10

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
        return list.shuffled().take(QUESTIONS_PER_QUIZ)
    }

    const val FULL_TEST_SIZE = 100

    fun loadFullTest(context: Context): List<Question> {
        val combined = mutableListOf<Question>()
        for (category in categories) {
            val jsonString = context.assets.open(category.jsonFile).bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optionsArray = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }
                combined.add(
                    Question(
                        question = obj.getString("question"),
                        options = options,
                        correctIndex = obj.getInt("correctIndex")
                    )
                )
            }
        }
        val shuffled = combined.shuffled()
        return if (shuffled.size >= FULL_TEST_SIZE) {
            shuffled.take(FULL_TEST_SIZE)
        } else {
            // Not enough unique questions yet to fill 100 without repeats - cycle through until we hit 100
            val result = mutableListOf<Question>()
            var i = 0
            while (result.size < FULL_TEST_SIZE) {
                result.add(shuffled[i % shuffled.size])
                i++
            }
            result.shuffled()
        }
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
            id = "pakstudies",
            title = "Pakistan Studies",
            subtitle = "Constitution, Politics & National History",
            jsonFile = "questions_pakstudies.json",
            colorRes = R.color.card_pakstudies,
            emoji = "\uD83C\uDDF5\uD83C\uDDF0"
        ),
        Category(
            id = "worldknowledge",
            title = "World Knowledge",
            subtitle = "World Geography, History & Culture",
            jsonFile = "questions_worldknowledge.json",
            colorRes = R.color.card_worldknowledge,
            emoji = "\uD83C\uDF10"
        ),
        Category(
            id = "nuclearphysics",
            title = "Nuclear Physics",
            subtitle = "Radioactivity, Fission, Fusion & Atomic Structure",
            jsonFile = "questions_nuclearphysics.json",
            colorRes = R.color.card_nuclearphysics,
            emoji = "\u2622\uFE0F"
        ),
        Category(
            id = "electronics",
            title = "Electronics",
            subtitle = "Circuits, Semiconductors & Digital Logic",
            jsonFile = "questions_electronics.json",
            colorRes = R.color.card_electronics,
            emoji = "\uD83D\uDD0C"
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
