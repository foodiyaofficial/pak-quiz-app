package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pakquiz.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoryRecyclerView.adapter = CategoryAdapter(QuestionBank.categories) { category ->
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("category_id", category.id)
            intent.putExtra("category_json", category.jsonFile)
            intent.putExtra("category_title", category.title)
            startActivity(intent)
        }
    }
}
