package com.example.othello

import android.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import java.lang.Integer.min


class OthelloView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val numRows = 8
    private val numCols = 8
    private val borderWidth = 5f
    private var cellSize = 0f
    private var discSize = 0f
    private var whiteScore = 2
    private var blackScore = 2
    private var currentPlayer: Int = 1 // -1 for white, 1 for black

    private val blackPaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val whitePaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        setARGB(255, 0, 0, 0)

    }
    private val borderPaint: Paint = Paint().apply {
        strokeWidth = borderWidth
        style = Paint.Style.STROKE
        color = Color.BLACK
    }
    private val cellRect: RectF = RectF()
    private val borderRect: RectF = RectF()
    private var board: Array<Array<Int>> = Array(numRows) { Array(numCols) { 0 } }

    private lateinit var wScore: TextView
    private lateinit var bScore: TextView
    private lateinit var currPlayer: TextView

    fun initViews(whiteScore: TextView, blackScore: TextView, currentPlayer: TextView) {
        this.wScore = whiteScore
        this.bScore = blackScore
        this.currPlayer = currentPlayer
    }
    init {
        setOnTouchListener(CellTouchListener())
        initializeBoard()
    }

    private fun initializeBoard() {
        currentPlayer = 1
        board[3][3] = -1
        board[3][4] = 1
        board[4][3] = 1
        board[4][4] = -1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewSize = min(measuredWidth, measuredHeight)
        cellSize = (viewSize - (borderWidth * 2)) / numCols
        discSize = cellSize * 0.8f
        setMeasuredDimension(viewSize, viewSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the border
        borderRect.set(
            borderWidth / 2,
            borderWidth / 2,
            width.toFloat() - borderWidth / 2,
            height.toFloat() - borderWidth / 2
        )
        canvas.drawRect(borderRect, borderPaint)

        // Draw the cells and discs
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                cellRect.set(
                    col * cellSize + borderWidth,
                    row * cellSize + borderWidth,
                    (col + 1) * cellSize + borderWidth,
                    (row + 1) * cellSize + borderWidth
                )
                canvas.drawRect(cellRect, borderPaint)

                when (board[row][col]) {
                    1 -> canvas.drawCircle(
                        cellRect.centerX(),
                        cellRect.centerY(),
                        discSize / 2,
                        blackPaint
                    )
                    -1 -> canvas.drawCircle(
                        cellRect.centerX(),
                        cellRect.centerY(),
                        discSize / 2 ,
                        whitePaint
                    )
                }
            }
        }
    }

    private inner class CellTouchListener : OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val col = ((event.x - borderWidth) / cellSize).toInt()
                    val row = ((event.y - borderWidth) / cellSize).toInt()

                    if (col in 0 until numCols && row in 0 until numRows) {
                        if (isValidMove(row, col, currentPlayer)) {
                            makeMove(row, col, currentPlayer)
                            view.invalidate()
                            switchPlayer()

                            if (isGameOver()) {
                                showGameOverDialog()
                            }
                        }

                    }
                }
            }
            return true
        }
    }

    private fun switchPlayer() {
        currentPlayer = -currentPlayer
    }


    private fun isValidMove(row: Int, col: Int, player: Int): Boolean {
        if (board[row][col] != 0) {
            // Cell is already occupied
            return false
        }

        // Check if move would flip any discs
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue

                var r = row + dr
                var c = col + dc
                var foundOpponent = false

                while (r in 0 until numRows && c in 0 until numCols) {
                    val cell = board[r][c]

                    if (cell == 0) {
                        // Empty cell - no flip
                        break
                    } else if (cell == player) {
                        // Found player's own disc - flip opponent's discs
                        if (foundOpponent) {
                            return true
                        } else {
                            break
                        }
                    } else {
                        // Found opponent's disc - continue searching
                        foundOpponent = true
                    }

                    r += dr
                    c += dc
                }
            }
        }

        return false
    }

    private fun makeMove(row: Int, col: Int, player: Int) {
        board[row][col] = player

        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0)
                    continue

                var r = row + dr
                var c = col + dc
                var foundOpponent = false

                while (r in 0 until numRows && c in 0 until numCols) {
                    val cell = board[r][c]

                    if (cell == 0) {
                        break
                    } else if (cell == player) {
                        if (foundOpponent) {
                            var fr = row + dr
                            var fc = col + dc

                            if(player==1) {
                                blackScore++
                                whiteScore--
                            }
                            else {
                                whiteScore++
                                blackScore--
                            }
                            while (fr != r || fc != c) {
                                board[fr][fc] = player
                                fr += dr
                                fc += dc
                            }
                            updateScores()
                            break
                        } else {
                            break
                        }
                    } else {
                        foundOpponent = true
                    }

                    r += dr
                    c += dc
                }
            }
        }
    }

    private fun isGameOver(): Boolean {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                if (board[row][col] == 0) {
                    return false
                }
            }
        }
        return true
    }

    private fun showGameOverDialog() {
        val blackScore = getScore(-1)
        val whiteScore = getScore(1)

        val winner: String
        val winnerScore: Int

        if (blackScore > whiteScore) {
            winner = "Black"
            winnerScore = blackScore
        } else if (whiteScore > blackScore) {
            winner = "White"
            winnerScore = whiteScore
        } else {
            winner = "No one"
            winnerScore = blackScore
        }

        val message = "Game Over!\nWinner: $winner\nScore: $winnerScore"
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        val alert = builder.create()
        alert.show()
    }

    private fun getScore(player: Int): Int {
        var score = 0
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                if (board[row][col] == player) {
                    score++
                }
            }
        }
        return score
    }
    fun updateScores() {
        blackScore = getScore(1)
        whiteScore = getScore(-1)
        bScore.text = "Black: $blackScore"
        wScore.text = "White: $whiteScore"
        if (currentPlayer == 1) {currPlayer.text = "White Player's turn"}
        else {currPlayer.text = "Black Player's turn"}
    }

    fun newGame() {
        board = Array(numRows) { Array(numCols) { 0 } }
        initializeBoard()
        updateScores()
        invalidate()
    }
}

