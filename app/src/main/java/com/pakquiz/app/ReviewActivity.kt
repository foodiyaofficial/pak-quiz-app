package com.pakquiz.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pakquiz.app.databinding.ActivityReviewBinding

class ReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryTitle = intent.getStringExtra("category_title") ?: "Quiz"
        @Suppress("DEPRECATION")
        val holder = intent.getSerializableExtra("review_data") as? QuestionsHolder

        binding.reviewTitle.text = "Review: $categoryTitle"

        val questions = holder?.questions ?: emptyList()
        binding.reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reviewRecyclerView.adapter = ReviewAdapter(questions)

        binding.backButton.setOnClickListener { finish() }
    }
}
