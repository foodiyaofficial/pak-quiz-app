package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.pakquiz.app.databinding.ActivityFullTestBinding
import java.io.Serializable

class FullTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullTestBinding
    private var questions: MutableList<Question> = mutableListOf()
    private var currentIndex = 0
    private var score = 0
    private var selectedForThisQuestion = -1

    private lateinit var optionCards: List<MaterialCardView>
    private lateinit var optionTexts: List<android.widget.TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Full Test Challenge"

        optionCards = listOf(binding.optionCard0, binding.optionCard1, binding.optionCard2, binding.optionCard3)
        optionTexts = listOf(binding.option0, binding.option1, binding.option2, binding.option3)

        val questionCount = intent.getIntExtra("question_count", QuestionBank.FULL_TEST_SIZE)
        questions = QuestionBank.loadFullTest(this, questionCount).toMutableList()

        showQuestion()

        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener { selectOption(index) }
        }

        binding.nextButton.setOnClickListener {
            advance()
        }
    }

    private fun showQuestion() {
        selectedForThisQuestion = -1
        val q = questions[currentIndex]
        binding.progressText.text = "Question ${currentIndex + 1} of ${questions.size}  \u2022  Score: $score"
        binding.progressBar.progress = ((currentIndex.toFloat() / questions.size) * 100).toInt()
        binding.questionText.text = q.question
        binding.answerFeedback.visibility = android.view.View.GONE
        binding.nextButton.isEnabled = false
        binding.nextButton.text = if (currentIndex == questions.size - 1) getString(R.string.finish) else getString(R.string.next)

        optionTexts.forEachIndexed { i, tv ->
            if (i < q.options.size) {
                tv.text = q.options[i]
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
    }

    /**
     * Same behavior as QuizActivity: tapping just marks the current selection (no
     * correct/wrong reveal). Freely changeable until Next is pressed.
     */
    private fun selectOption(selectedIndex: Int) {
        optionCards.indices.forEach { resetCardStyle(it) }

        selectedForThisQuestion = selectedIndex
        optionCards[selectedIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.accent))
        optionTexts[selectedIndex].setTextColor(ContextCompat.getColor(this, R.color.primary_dark))

        binding.nextButton.isEnabled = true
    }

    private fun advance() {
        // Lock in the final selection now, only when Next is pressed
        val q = questions[currentIndex]
        q.selectedIndex = selectedForThisQuestion
        if (selectedForThisQuestion == q.correctIndex) {
            score++
        }

        val questionNumberJustFinished = currentIndex + 1

        if (currentIndex == questions.size - 1) {
            goToResult()
            return
        }

        currentIndex++

        // Show a full-screen ad placeholder every 25 questions (after Q25, Q50, Q75)
        if (questionNumberJustFinished % 25 == 0) {
            val intent = Intent(this, InterstitialAdActivity::class.java)
            intent.putExtra("resume_full_test", true)
            startActivityForResult(intent, REQUEST_AD)
        } else {
            showQuestion()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_AD) {
            showQuestion()
        }
    }

    private fun goToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("score", score)
        intent.putExtra("total", questions.size)
        intent.putExtra("category_id", "fulltest")
        intent.putExtra("category_title", "Full Test Challenge")
        intent.putExtra("category_json", "")
        intent.putExtra("review_data", QuestionsHolder(questions) as Serializable)
        intent.putExtra("is_full_test", true)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val REQUEST_AD = 501
    }
}
