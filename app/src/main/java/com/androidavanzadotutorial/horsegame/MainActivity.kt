package com.androidavanzadotutorial.horsegame

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private var bitmap:Bitmap ?= null
    private var mHandler:Handler? = null //Tiempo
    private var timeInSeconds:Long = 0
    private var gaming = true
    private var width_bonus = 0
    private var cellSelected_x = 0
    private var cellSelected_y = 0
    private var string_share = ""
    private var level = 1
    private var lives = 1

    private var score_lives = 1
    private var scoreLevel = 1

    private var nextLevel = false
    private var levelMoves = 0
    private var movesRequired = 0
    private var moves = 0
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
        this.startGame()
    }

    private fun startGame(){

        this.setLevel()
        this.setLevelParameters()

        this.resetBoard()
        this.clearBoard()

        this.setBoardLevel()
        this.setFirstPosition()

        this.resetTime()
        this.startTime()
        this.gaming = true
    }

    private fun clearBoard(){
        var iv:ImageView

        var colorBlack = ContextCompat.getColor(this, resources.getIdentifier(nameColorBlack,"color",packageName))
        var colorWhite = ContextCompat.getColor(this, resources.getIdentifier(nameColorWhite,"color",packageName))

        for(i in 0..7){
            for(j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j","id",packageName))
                //iv.setImageResource(R.drawable.horse)
                iv.setImageResource(0)
                if(this.checkColorCell(i,j) == "black") iv.setBackgroundColor(colorBlack)
                else iv.setBackgroundColor(colorWhite)
            }
        }

    }

    private var chronometer:Runnable = object :Runnable{
        override fun run() {
            try {
                if(gaming){
                    timeInSeconds++
                    updateStopWatchView(timeInSeconds)
                }
            }finally {
                mHandler!!.postDelayed(this,1000L)
            }
        }
    }

    private fun updateStopWatchView(timeInSeconds:Long){
        val formattedTime:String = this.getFormattedStopWatch((timeInSeconds * 1000))
        var tvTimeData:TextView = findViewById(R.id.tvTimeData)
        tvTimeData.text = formattedTime
    }

    private fun getFormattedStopWatch(ms:Long):String{
        var milliseconds = ms
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds-= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (minutes < 10) "0" else ""}$minutes:" + "${if (seconds < 10) "0" else ""}$seconds"
    }

    private fun resetTime(){
        this.mHandler?.removeCallbacks(this.chronometer)
        this.timeInSeconds = 0

        var tvTimeData:TextView = findViewById(R.id.tvTimeData)
        tvTimeData.text = "00:00"
    }

    private fun startTime(){
        this.mHandler = Handler(Looper.getMainLooper())
        this.chronometer.run()
    }

    private fun initScreenGame(){
        setSizeBoard()
        hide_message(false)
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

    private fun hide_message(start:Boolean){
        var lyMessage:LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE

        if(start) this.startGame()
    }

    private fun setFirstPosition(){
        var x = 0
        var y = 0

        var firstPosition = false

        while (firstPosition == false){
            x = (0..7).random()
            y = (0..7).random()
            if(this.board[x][y] == 1) firstPosition = true
            this.checkOption(x,y)
            if(this.options == 0) firstPosition = false
        }


        this.cellSelected_x = x
        this.cellSelected_y = y

        selectCell(x,y)
    }

    private fun setLevel(){
        if(this.nextLevel){
            this.level++
        }else{
            this.lives--
            if(this.lives < 1){
                this.level = 1
                this.lives = 1
            }
        }
    }

    private fun setLevelParameters(){
        var tvLiveData:TextView = findViewById(R.id.tvLiveData)
        tvLiveData.text = this.lives.toString()

        this.score_lives = this.lives

        var tvLevelNumber:TextView = findViewById(R.id.tvLevelNumber)
        tvLevelNumber.text = this.level.toString()
        this.scoreLevel = this.level

        this.bonus = 0
        var tvBonusData:TextView = findViewById(R.id.tvBonusData)
        tvBonusData.text = ""

        this.setLevelMoves()
        this.moves = this.levelMoves

        this.movesRequired = this.setMovesRequired()

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
        this.gaming = false
        this.nextLevel = !gameOver

        var lyMessage:LinearLayout = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.VISIBLE

        var tvTitleMessage: TextView = findViewById(R.id.tvTitleMessage)
        tvTitleMessage.text = title

        var score:String = ""
        var tvTimeData: TextView = findViewById(R.id.tvTimeData)
        if(gameOver){
            score = "Score: "+(this.levelMoves-this.moves)+"/"+this.levelMoves
            string_share = "This game makes me sick !!! " + score
        }else{
            score = tvTimeData.text.toString()
            string_share = "Let's go!!! New challenge completed. Level: $level (" + score + ")"
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

    private fun setLevelMoves(){
        when(this.level){
            1->this.levelMoves = 64
            2->this.levelMoves = 56
            3->this.levelMoves = 32
            4->this.levelMoves = 16
            5->this.levelMoves = 48
        }
    }

    private fun setMovesRequired():Int{
        var movesRequired = 0

        when(this.level){
            1->movesRequired = 8
            2->movesRequired = 10
            3->movesRequired = 12
            4->movesRequired = 10
            5->movesRequired = 10
        }
        return movesRequired
    }

    private fun paint_column(column:Int){
        for(i in 0..7){
            this.board[column][i] = 1
            this.paintHorseCell(column,i,"previus_cell")
        }
    }

    private fun paintLevel_2(){
        this.paint_column(6)
    }
    private fun paintLevel_3(){
        for(i in 0..7){
            for(j in 4..7){
                this.board[j][i] = 1
                this.paintHorseCell(j,i,"previus_cell")
            }
        }
    }
    private fun paintLevel_4(){
        this.paintLevel_3()
        this.paintLevel_5()
    }
    private fun paintLevel_5(){
        for(i in 0..3){
            for(j in 0..3){
                this.board[j][i] = 1
                this.paintHorseCell(j,i,"previus_cell")
            }
        }
    }

    private fun setBoardLevel(){
        when(this.level){
            2->this.paintLevel_2()
            3->this.paintLevel_3()
            4->this.paintLevel_4()
            5->this.paintLevel_5()
        }
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

    private fun launchAction(v:View){
        this.hide_message(true)
    }

    private fun launchShareGame(v:View){
        this.shareGame(v)
    }

    private fun shareGame(view:View){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        //var ssc:ScreenCapture = capture(this)
        //this.bitmap = ssc.getBitmap()

        /*var screenView:View = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        this.bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);*/

        view.setDrawingCacheEnabled(true);
        this.bitmap = Bitmap.createBitmap(view.getDrawingCache());

        if(this.bitmap != null){
            var idGame = SimpleDateFormat("yyyy/MM/dd").format(Date())
            idGame = idGame.replace(":","")
            idGame = idGame.replace("/","")

            val path = saveImage(this.bitmap,"${idGame}.jpg")
            val bmpUri = Uri.parse(path)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT, string_share)
            shareIntent.type = "image/png"

            val finalShareIntent = Intent.createChooser(shareIntent, "Select the app you want to share the game to")
            finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(finalShareIntent)

        }
    }

    private fun saveImage(bitmap: Bitmap?, fileName: String):String? {
        if(bitmap == null){
            return null
        }
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES + "/Screenshots")
            }
            val uri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
            if(uri != null){
                this.contentResolver.openOutputStream(uri).use {
                    if(it == null) return@use
                    bitmap.compress(Bitmap.CompressFormat.PNG,85,it)
                    it.flush()
                    it.close()
                    MediaScannerConnection.scanFile(this,arrayOf(uri.toString()),null,null)
                }
            }
            return uri.toString()
        }
        val filePath = Environment.getExternalStoragePublicDirectory (
            Environment.DIRECTORY_PICTURES + "/Screenshots"
        ).absolutePath
        val dir = File(filePath)
        if(!dir.exists()) dir.mkdirs()
        val file = File(dir,fileName)
        val fOut = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG,85,fOut)
        fOut.flush()
        fOut.close()

        MediaScannerConnection.scanFile(this,arrayOf(file.toString()),null,null)
        return filePath
    }
}