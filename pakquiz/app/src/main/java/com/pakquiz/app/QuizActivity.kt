package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.pakquiz.app.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var questions: List<Question> = emptyList()
    private var currentIndex = 0
    private var score = 0
    private lateinit var categoryId: String
    private lateinit var categoryTitle: String
    private lateinit var categoryJson: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra("category_id") ?: "islamiat"
        categoryTitle = intent.getStringExtra("category_title") ?: "Quiz"
        categoryJson = intent.getStringExtra("category_json") ?: "questions_islamiat.json"

        questions = QuestionBank.load(this, categoryJson)
        title = categoryTitle

        showQuestion()

        binding.nextButton.setOnClickListener {
            checkAnswerAndAdvance()
        }
    }

    private fun showQuestion() {
        val q = questions[currentIndex]
        binding.progressText.text = "Question ${currentIndex + 1} of ${questions.size}"
        binding.questionText.text = q.question
        binding.optionsGroup.clearCheck()

        val optionViews = listOf(binding.option0, binding.option1, binding.option2, binding.option3)
        optionViews.forEachIndexed { i, radioButton ->
            radioButton.text = q.options.getOrElse(i) { "" }
            radioButton.visibility = if (i < q.options.size) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.nextButton.text = if (currentIndex == questions.size - 1) getString(R.string.finish) else getString(R.string.next)
    }

    private fun checkAnswerAndAdvance() {
        val selectedId = binding.optionsGroup.checkedRadioButtonId
        if (selectedId == -1) {
            return
        }
        val selectedButton = findViewById<RadioButton>(selectedId)
        val optionViews = listOf(binding.option0, binding.option1, binding.option2, binding.option3)
        val selectedIndex = optionViews.indexOf(selectedButton)

        if (selectedIndex == questions[currentIndex].correctIndex) {
            score++
        }

        if (currentIndex == questions.size - 1) {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("score", score)
            intent.putExtra("total", questions.size)
            intent.putExtra("category_id", categoryId)
            intent.putExtra("category_title", categoryTitle)
            intent.putExtra("category_json", categoryJson)
            startActivity(intent)
            finish()
        } else {
            currentIndex++
            showQuestion()
        }
    }
}
