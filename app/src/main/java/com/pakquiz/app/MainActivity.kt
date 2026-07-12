package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.pakquiz.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.categoryRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.categoryRecyclerView.adapter = CategoryAdapter(QuestionBank.categories) { category ->
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("category_id", category.id)
            intent.putExtra("category_json", category.jsonFile)
            intent.putExtra("category_title", category.title)
            startActivity(intent)
        }

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.fullTestInclude.root.setOnClickListener {
            startActivity(Intent(this, FullTestActivity::class.java))
        }

        // TODO (AdMob): once you have a real Ad Unit ID, replace bannerAdContainer's placeholder
        // TextView with a com.google.android.gms.ads.AdView and call adView.loadAd(AdRequest.Builder().build())
        // See README section "Adding real AdMob ads" for the exact steps.
    }
}
