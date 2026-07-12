package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pakquiz.app.databinding.ActivityInterstitialAdBinding

/**
 * Placeholder screen simulating where a real AdMob interstitial ad would be shown
 * after a quiz finishes, before the result screen. Simply forwards all extras through
 * to ResultActivity. See README "Adding real AdMob ads" for how to wire in a real ad here.
 */
class InterstitialAdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInterstitialAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueButton.setOnClickListener {
            val resultIntent = Intent(this, ResultActivity::class.java)
            resultIntent.putExtras(intent)
            startActivity(resultIntent)
            finish()
        }
    }
}
