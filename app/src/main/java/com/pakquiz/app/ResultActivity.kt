package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pakquiz.app.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 0)
        val categoryId = intent.getStringExtra("category_id") ?: "islamiat"
        val categoryTitle = intent.getStringExtra("category_title") ?: "Quiz"
        val categoryJson = intent.getStringExtra("category_json") ?: "questions_islamiat.json"

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        val holder = intent.getSerializableExtra("review_data") as? QuestionsHolder

        val prefs = getSharedPreferences("pakquiz_scores", MODE_PRIVATE)
        val bestKey = "best_$categoryId"
        val previousBest = prefs.getInt(bestKey, 0)
        val newBest = if (score > previousBest) score else previousBest
        prefs.edit().putInt(bestKey, newBest).apply()

        PerformanceStore.recordQuizCompletion(this, categoryId, categoryTitle, score, total)

        binding.scoreText.text = "$score / $total"
        binding.bestScoreText.text = "${getString(R.string.best_score)}: $newBest / $total"

        binding.reviewButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("review_data", holder)
            intent.putExtra("category_title", categoryTitle)
            startActivity(intent)
        }

        binding.retryButton.setOnClickListener {
            val isFullTest = categoryId == "fulltest"
            val intent = Intent(this, if (isFullTest) FullTestActivity::class.java else QuizActivity::class.java)
            if (!isFullTest) {
                intent.putExtra("category_id", categoryId)
                intent.putExtra("category_json", categoryJson)
                intent.putExtra("category_title", categoryTitle)
            }
            startActivity(intent)
            finish()
        }

        binding.homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
