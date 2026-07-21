package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.pakquiz.app.databinding.ActivityQuizBinding
import java.io.Serializable

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var questions: MutableList<Question> = mutableListOf()
    private var currentIndex = 0
    private var score = 0
    private var answered = false
    private lateinit var categoryId: String
    private lateinit var categoryTitle: String
    private lateinit var categoryJson: String

    private lateinit var optionCards: List<MaterialCardView>
    private lateinit var optionTexts: List<android.widget.TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        optionCards = listOf(binding.optionCard0, binding.optionCard1, binding.optionCard2, binding.optionCard3)
        optionTexts = listOf(binding.option0, binding.option1, binding.option2, binding.option3)

        categoryId = intent.getStringExtra("category_id") ?: "islamiat"
        categoryTitle = intent.getStringExtra("category_title") ?: "Quiz"
        categoryJson = intent.getStringExtra("category_json") ?: "questions_islamiat.json"
        val questionCount = intent.getIntExtra("question_count", QuestionBank.QUESTIONS_PER_QUIZ)

        questions = QuestionBank.load(this, categoryJson, categoryId, questionCount).toMutableList()
        title = categoryTitle

        showQuestion()

        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener { selectOption(index) }
        }

        binding.nextButton.setOnClickListener {
            advance()
        }
    }

    private fun showQuestion() {
        answered = false
        val q = questions[currentIndex]
        binding.progressText.text = "Question ${currentIndex + 1} of ${questions.size}  \u2022  Score: $score"
        binding.progressBar.progress = ((currentIndex.toFloat() / questions.size) * 100).toInt()
        binding.questionText.text = q.question
        binding.answerFeedback.visibility = android.view.View.INVISIBLE
        binding.reportButton.visibility = android.view.View.GONE
        binding.nextButton.isEnabled = false
        binding.nextButton.text = if (currentIndex == questions.size - 1) getString(R.string.finish) else getString(R.string.next)

        optionTexts.forEachIndexed { i, tv ->
            if (i < q.options.size) {
                tv.text = q.options[i]
                tv.visibility = android.view.View.VISIBLE
                optionCards[i].visibility = android.view.View.VISIBLE
            } else {
                optionCards[i].visibility = android.view.View.GONE
            }
            resetCardStyle(i)
        }
    }

    private fun resetCardStyle(index: Int) {
        optionCards[index].setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface))
        optionTexts[index].setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        optionCards[index].strokeColor = android.graphics.Color.parseColor("#E0DED4")
    }

    private fun selectOption(selectedIndex: Int) {
        if (answered) return
        answered = true
        val q = questions[currentIndex]
        q.selectedIndex = selectedIndex

        val isCorrect = selectedIndex == q.correctIndex
        if (isCorrect) score++

        // Highlight the correct answer green always
        optionCards[q.correctIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct))
        optionTexts[q.correctIndex].setTextColor(ContextCompat.getColor(this, R.color.white))

        // If user picked wrong, highlight their pick red
        if (!isCorrect) {
            optionCards[selectedIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.wrong))
            optionTexts[selectedIndex].setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.answerFeedback.visibility = android.view.View.VISIBLE
        if (isCorrect) {
            binding.answerFeedback.text = "\u2713 Correct!"
            binding.answerFeedback.setTextColor(ContextCompat.getColor(this, R.color.correct))
            binding.reportButton.visibility = android.view.View.GONE
        } else {
            binding.answerFeedback.text = getString(R.string.correct_answer_prefix) + q.options[q.correctIndex]
            binding.answerFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong))
            binding.reportButton.visibility = android.view.View.VISIBLE
            binding.reportButton.setOnClickListener { reportQuestion(q) }
        }

        binding.nextButton.isEnabled = true
    }

    private fun reportQuestion(question: Question) {
        val body = buildString {
            append("Subject: $categoryTitle\n\n")
            append("Question: ${question.question}\n\n")
            append("Options:\n")
            question.options.forEachIndexed { i, opt -> append("${i + 1}. $opt\n") }
            append("\nApp marks correct answer as: ${question.options[question.correctIndex]}\n\n")
            append("What's wrong with this question: ")
        }
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = android.net.Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_SUBJECT, "PakQuiz - Wrong Answer Report")
        intent.putExtra(Intent.EXTRA_TEXT, body)
        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(this, "No email app found on this device", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun advance() {
        if (currentIndex == questions.size - 1) {
            val intent = Intent(this, InterstitialAdActivity::class.java)
            intent.putExtra("score", score)
            intent.putExtra("total", questions.size)
            intent.putExtra("category_id", categoryId)
            intent.putExtra("category_title", categoryTitle)
            intent.putExtra("category_json", categoryJson)
            intent.putExtra("review_data", QuestionsHolder(questions) as Serializable)
            startActivity(intent)
            finish()
        } else {
            currentIndex++
            showQuestion()
        }
    }
}

// Simple serializable wrapper so the answered question list can be passed to ResultActivity for review
data class QuestionsHolder(val questions: List<Question>) : Serializable
