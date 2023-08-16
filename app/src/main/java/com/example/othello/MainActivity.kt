package com.example.othello

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var othelloView: OthelloView
    private lateinit var newGameButton: Button
    private lateinit var whiteScore: TextView
    private lateinit var blackScore: TextView
    private lateinit var currentPlayer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        othelloView = findViewById(R.id.othelloView)
        newGameButton = findViewById(R.id.newGameButton)
        whiteScore = findViewById(R.id.whiteScore)
        blackScore = findViewById(R.id.blackScore)
        currentPlayer = findViewById(R.id.currentPlayer)

        othelloView.initViews(whiteScore, blackScore, currentPlayer)
        newGameButton.setOnClickListener{
            othelloView.newGame()
        }
    }
    private fun updateScores() {
        othelloView.updateScores()
    }

}