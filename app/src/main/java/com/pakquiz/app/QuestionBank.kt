package com.pakquiz.app

import android.content.Context
import org.json.JSONArray

object QuestionBank {

    const val QUESTIONS_PER_QUIZ = 10
    const val FULL_TEST_SIZE = 100
    private const val FULL_TEST_KEY = "fulltest_all"

    private fun parseJson(context: Context, fileName: String): List<Question> {
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
        return list
    }

    /**
     * Loads a quiz-sized batch of questions for a single subject, applying the
     * Smart Question Rotation System: questions already seen by this user for this
     * subject are excluded until the entire pool has been shown once, at which point
     * the history resets and a fresh rotation cycle begins automatically.
     */
    fun load(context: Context, fileName: String, subjectKey: String, count: Int = QUESTIONS_PER_QUIZ): List<Question> {
        val allQuestions = parseJson(context, fileName)
        val seenIds = SeenQuestionsStore.getSeenIds(context, subjectKey)
        var unseen = allQuestions.filter { it.id !in seenIds }

        if (unseen.size < count) {
            // Pool exhausted (or too small to fill this quiz) - reset and start a fresh cycle
            SeenQuestionsStore.resetSeen(context, subjectKey)
            unseen = allQuestions
        }

        val selected = unseen.shuffled().take(count)
        SeenQuestionsStore.addSeenIds(context, subjectKey, selected.map { it.id })
        return selected
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
            id = "computerscience",
            title = "Computer Science & IT",
            subtitle = "Computers, Programming & Technology Basics",
            jsonFile = "questions_computerscience.json",
            colorRes = R.color.card_computerscience,
            emoji = "\uD83D\uDCBB"
        ),
        Category(
            id = "logicalreasoning",
            title = "Logical Reasoning & IQ",
            subtitle = "Patterns, Series & Analytical Thinking",
            jsonFile = "questions_logicalreasoning.json",
            colorRes = R.color.card_logicalreasoning,
            emoji = "\uD83E\uDDE9"
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

    /**
     * Loads the 100-question Full Test pool, mixing every subject together and applying
     * the same rotation system across the combined pool (separate rotation cycle from
     * individual subject quizzes).
     */
    fun loadFullTest(context: Context, size: Int = FULL_TEST_SIZE): List<Question> {
        val combined = mutableListOf<Question>()
        for (category in categories) {
            combined.addAll(parseJson(context, category.jsonFile))
        }

        val seenIds = SeenQuestionsStore.getSeenIds(context, FULL_TEST_KEY)
        var unseen = combined.filter { it.id !in seenIds }

        if (unseen.size < size) {
            SeenQuestionsStore.resetSeen(context, FULL_TEST_KEY)
            unseen = combined
        }

        val selected = if (unseen.size >= size) {
            unseen.shuffled().take(size)
        } else {
            // Combined pool itself is smaller than requested - cycle through with repeats to fill
            val result = mutableListOf<Question>()
            val shuffledPool = unseen.shuffled()
            var i = 0
            while (result.size < size) {
                result.add(shuffledPool[i % shuffledPool.size])
                i++
            }
            result.shuffled()
        }

        SeenQuestionsStore.addSeenIds(context, FULL_TEST_KEY, selected.map { it.id }.distinct())
        return selected
    }
}
