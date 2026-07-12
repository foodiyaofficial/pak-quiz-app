package com.pakquiz.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val questions: List<Question>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.reviewQuestionNumber)
        val question: TextView = view.findViewById(R.id.reviewQuestionText)
        val yourAnswer: TextView = view.findViewById(R.id.reviewYourAnswer)
        val correctAnswer: TextView = view.findViewById(R.id.reviewCorrectAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val q = questions[position]
        val context = holder.itemView.context

        holder.number.text = "Question ${position + 1}"
        holder.question.text = q.question

        val userAnsweredCorrectly = q.selectedIndex == q.correctIndex
        val yourAnswerLabel = if (q.selectedIndex in q.options.indices) q.options[q.selectedIndex] else "Not answered"

        holder.yourAnswer.text = "Your answer: $yourAnswerLabel"
        holder.yourAnswer.setTextColor(
            ContextCompat.getColor(context, if (userAnsweredCorrectly) R.color.correct else R.color.wrong)
        )

        if (!userAnsweredCorrectly) {
            holder.correctAnswer.visibility = View.VISIBLE
            holder.correctAnswer.text = "Correct answer: ${q.options[q.correctIndex]}"
        } else {
            holder.correctAnswer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = questions.size
}
