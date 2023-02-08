package com.androidavanzadotutorial.horsegame

import android.content.Context
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var width_bonus = 0
    private var cellSelected_x = 0
    private var cellSelected_y = 0

    private var levelMoves = 64
    private var movesRequired = 4
    private var moves = 64
    private var options = 0
    private var bonus = 0
    private var checkMovement = true

    private var nameColorBlack = "black_cell"
    private var nameColorWhite = "white_cell"

    private lateinit var board:Array<IntArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.initScreenGame()
        this.resetBoard()
        this.setFirstPosition()
    }

    private fun initScreenGame(){
        setSizeBoard()
        hide_message()
    }

    private fun setSizeBoard(){
        var iv:ImageView

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x

        var width_dp = (width / getResources().getDisplayMetrics().density)
        var lateralMarginDP = 0
        val width_cell = (width_dp - lateralMarginDP) / 8
        val height_cell = width_cell

        this.width_bonus = 2 * width_cell.toInt()

        for(i in 0..7){
            for(j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j","id",packageName))
                var height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,height_cell,getResources().getDisplayMetrics()).toInt()
                var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,width_cell,getResources().getDisplayMetrics()).toInt()
                iv.setLayoutParams(TableRow.LayoutParams(width,height))
            }
        }
    }

    private fun hide_message(){
        var lyMessage:LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE
    }

    private fun setFirstPosition(){
        var x = 0
        var y = 0
        x = (0..7).random()
        y = (0..7).random()

        this.cellSelected_x = x
        this.cellSelected_y = y

        selectCell(x,y)
    }

    private fun selectCell(x:Int,y:Int){

        this.moves--
        var tvMovesData:TextView = findViewById(R.id.tvMovesData)
        tvMovesData.text = moves.toString()

        this.growProgressBonus()

        if(this.board[x][y] == 2){
            this.bonus++
            var tvBonusData:TextView = findViewById(R.id.tvBonusData)
            tvBonusData.text = "+ ${this.bonus}"
        }

        this.board[x][y] = 1
        paintHorseCell(this.cellSelected_x,this.cellSelected_y,"previus_cell")

        this.cellSelected_x = x
        this.cellSelected_y = y

        this.clearOptions()

        paintHorseCell(x,y,"selected_cell")
        this.checkMovement = true
        this.checkOption(x,y)

        if(this.moves > 0){
            this.checkNewBonus()
            this.checkGameOver()
        }else{
            this.showMessage("You Win!!","Next Level",false)
        }
    }

    private fun checkGameOver(){
        if(this.options == 0){
            if(this.bonus > 0){
                this.checkMovement = false
                this.paintAllOptions()
            }else{
                this.showMessage("Game over","Try Again!",true)
            }
        }
    }

    private fun paintAllOptions(){
        for (i in 0..7){
            for(j in 0..7){
                if(this.board[i][j] != 1) this.paintOption(i,j)
                if(this.board[i][j] == 0) this.board[i][j] = 9
            }
        }
    }

    private fun showMessage(title:String, action:String, gameOver:Boolean){
        var lyMessage:LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.VISIBLE

        var tvTitleMessage: TextView = findViewById(R.id.tvTitleMessage)
        tvTitleMessage.text = title

        var score:String = ""
        var tvTimeData: TextView = findViewById(R.id.tvTimeData)
        if(gameOver){
            score = "Score: "+(this.levelMoves-this.moves)+"/"+this.levelMoves
        }else{
            score = tvTimeData.text.toString()
        }

        var tvScoreMessage: TextView = findViewById(R.id.tvScoreMessage)
        tvScoreMessage.text = score

        var tvAction: TextView = findViewById(R.id.tvAction)
        tvAction.text = action

    }

    private fun growProgressBonus(){
        var v:View = findViewById(R.id.vNewBonus)
        var moves_done = this.levelMoves - this.moves
        var bonus_done = moves_done /this.movesRequired
        var moves_rest = this.movesRequired * (bonus_done)
        var bonus_grow = moves_done - moves_rest

        var widthBonus = ((this.width_bonus/this.movesRequired) * bonus_grow).toFloat()

        var height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8f,getResources().getDisplayMetrics()).toInt()
        var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,widthBonus,getResources().getDisplayMetrics()).toInt()
        v.setLayoutParams(TableRow.LayoutParams(width,height))
    }

    private fun checkNewBonus(){
        if(this.moves % this.movesRequired == 0){
            var bonusCell_x = 0
            var bonusCell_y = 0
            var bonusCell = false

            while (bonusCell == false){
                bonusCell_x = (0..7 ).random()
                bonusCell_y = (0..7 ).random()
                if(this.board[bonusCell_x][bonusCell_y] == 0){
                    bonusCell = true
                }
            }
            this.board[bonusCell_x][bonusCell_y] = 2
            this.paintBonusCell(bonusCell_x,bonusCell_y)
        }
    }

    private fun paintBonusCell(x:Int, y:Int){
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id",packageName))
        iv.setImageResource(R.drawable.bonus)
    }

    private fun clearOptions(){
        for (i in 0..7){
            for (j in 0..7){
                if(this.board[i][j] == 9 || this.board[i][j] == 2){
                    if(this.board[i][j] == 9) this.board[i][j] = 0
                    this.clearOption(i,j)
                }
            }
        }
    }

    private fun clearOption(x:Int,y:Int){
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id",packageName))
        if(this.checkColorCell(x,y).equals("black")){
            iv.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier(nameColorBlack,"color",packageName)))
        }else{
            iv.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier(nameColorWhite,"color",packageName)))
        }

        if(this.board[x][y] == 1){
            iv.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier("previus_cell","color",packageName)))
        }
    }

    private fun paintHorseCell(x:Int,y:Int,color:String){
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id",packageName))
        iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(color,"color",packageName)))
        iv.setImageResource(R.drawable.horse)
    }

    private fun resetBoard(){
        /**
         * 0 esta libre
         * 1 casilla marcada
         * 2 es un bonus
         * 9 es una opcion del movimiento actual
         */
        this.board = arrayOf(
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0)
        )
    }

    fun checkCellClicked(v:View){
        var name = v.tag.toString()
        var x = name.subSequence(1,2).toString().toInt()
        var y = name.subSequence(2,3).toString().toInt()

        this.checkCell(x,y)
    }

    private fun checkCell(x:Int, y:Int){

        var checkTrue = true
        if(this.checkMovement){
            var dif_x = x - this.cellSelected_x
            var dif_y = y - this.cellSelected_y

            checkTrue = false
            if(dif_x == 1 && dif_y == 2)   checkTrue = true // right - top long
            if(dif_x == 1 && dif_y == -2)  checkTrue = true // right - bottom long
            if(dif_x == 2 && dif_y == 1)   checkTrue = true // right long - top
            if(dif_x == 2 && dif_y == -1)  checkTrue = true // right long - bottom
            if(dif_x == -1 && dif_y == 2)  checkTrue = true // left - top long
            if(dif_x == -1 && dif_y == -2) checkTrue = true // left - bottom long
            if(dif_x == -2 && dif_y == 1)  checkTrue = true // left long - top
            if(dif_x == -2 && dif_y == -1) checkTrue = true // left long - bottom
        }else{
            if(this.board[x][y] != 1){
                this.bonus--
            }
        }

        if(this.board[x][y] == 1) checkTrue = false

        if(checkTrue) this.selectCell(x,y)
    }

    private fun checkOption(x:Int, y:Int){
        this.options = 0

        this.checkMove(x,y,1,2)   // check move right - top long
        this.checkMove(x,y,2,1)   // check move right long - top
        this.checkMove(x,y,1,-2)  // check move right - bottom long
        this.checkMove(x,y,2,-1)  // check move right long - bottom
        this.checkMove(x,y,-1,2)  // check move left - top long
        this.checkMove(x,y,-2,1)  // check move left long - top
        this.checkMove(x,y,-1,-2) // check move left - bottom long
        this.checkMove(x,y,-2,-1) // check move left long - bottom

        var tvOptionsData:TextView = findViewById(R.id.tvOptionsData)
        tvOptionsData.text = options.toString()
    }

    private fun checkMove(x:Int, y: Int, mov_x:Int, mov_y:Int){
        var option_x = x + mov_x
        var option_y = y + mov_y

        if(option_x < 8 && option_y < 8 && option_x >= 0 && option_y >= 0){
            if(this.board[option_x][option_y] == 0 || this.board[option_x][option_y] == 2){
                this.options++
                this.paintOption(option_x,option_y)
                if(this.board[option_x][option_y] == 0) this.board[option_x][option_y] = 9
            }
        }
    }

    private fun paintOption(x:Int, y:Int){
        var iv:ImageView = findViewById(resources.getIdentifier("c$x$y","id",packageName))
        if(this.checkColorCell(x,y).equals("black")) iv.setBackgroundResource(R.drawable.option_black)
        else iv.setBackgroundResource(R.drawable.option_white)
    }

    private fun checkColorCell(x:Int, y:Int):String{
        var color = ""
        var blackColumn_x = arrayOf(0,2,4,6)
        var blackRow_x = arrayOf(1,3,5,7)

        if(blackColumn_x.contains(x) && blackColumn_x.contains(y) || blackRow_x.contains(x) && blackRow_x.contains(y)){
            color = "black"
        }else{
            color = "white"
        }
        return color
    }

}