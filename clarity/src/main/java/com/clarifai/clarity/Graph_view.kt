package com.clarifai.clarity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_graph_view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.random.Random


class Graph_view : AppCompatActivity() {
    private val images = readfile()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_view)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        var index=0

        ChangeImage(index)
        back_button.setOnClickListener{
            if(index>0){
                index--
            }
            else{
                Toast.makeText(this, "First Photo in gallery is currently shown", Toast.LENGTH_SHORT).show()
            }
            ChangeImage(index)
        }
        forward_button.setOnClickListener{
            if(index<images.length()-1){
            index++
            }
            else{index = 0}
            ChangeImage(index)
        }
        Creategraph(images)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.home -> {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            true
        }

        R.id.gallery -> {
            val intent = Intent(this, Graph_view::class.java)
            startActivity(intent)
            Toast.makeText(this, "Gallery Clicked", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.interval -> {
            Toast.makeText(this, "Interval Clicked", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.settings -> {
            val intent = Intent(this, OptionsAction::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
    private fun Creategraph(pictures: JSONArray){
        lateinit var series: LineGraphSeries<DataPoint>
        var x : Double
        var y : Double

        val graph = findViewById(R.id.graph) as GraphView

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true)
        graph.getViewport().setMinY(0.9)
        graph.getViewport().setMaxY(1.0)
        graph.getViewport().setXAxisBoundsManual(true)
        graph.getViewport().setMinX(0.0)
        graph.getViewport().setMaxX(10.0)

        //enable scrolling
        //graph.getViewport().setScalable(true)
        //graph.getViewport().setScalableY(true)
        graph.getViewport().setScrollable(true)
        //Processes Data
        var values = JSONObject()
        var obj = JSONArray()

        for (i in 0 until pictures.length()){
            val picture = pictures.getJSONObject(i)
            val keys: Iterator<String> = picture.keys()
            for(key in keys){
                var nval = JSONObject()
                //Maps value
                if(key != "Filename" && key != "TimeStamp"){
                    try {
                        obj = values.getJSONArray(key)
                    }
                    catch (error: Throwable){
                        obj = JSONArray()
                    }
                    nval.put(i.toString(),(picture.get(key).toString()).toDouble())
                    obj.put(nval)
                    values.put(key,obj)

                }
            }
        }
        Log.d("Values",values.toString())

        //Plot Graph from objects
        val keys = values.keys()
        for(key in keys){
            val data = values.getJSONArray(key)
            Log.d("Data", data.toString())

            series = LineGraphSeries()
            for (i in 0 until data.length())
            {
                var spot = data.getJSONObject(i)
                var ting = spot.keys().next()
                x = ting.toDouble()
                y = spot.get(ting).toString().toDouble()
                series.appendData(DataPoint(x,y), true, 500)
            }
            series.setOnDataPointTapListener { series, dataPoint ->
                ChangeImage("${dataPoint.x}".toDouble().toInt())
                }

            series.setColor(getRandomColor())
            series.setTitle(key)
            graph.addSeries(series)
        }

        graph.getLegendRenderer().setVisible(true)
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP)
        graph.getLegendRenderer().setTextSize(20.00.toFloat())
        graph.getLegendRenderer().setWidth(200)
    }
    fun getRandomColor(): Int {
        val rnd = Random
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }
    private fun ChangeImage(index: Int){
        val obj = images.getJSONObject(index)
        var filestr = ""
        var tagstr = ""
        val assetsBitmap:Bitmap? = getBitmapFromAssets(obj.get("Filename").toString())
        val objstr = obj.toString().replace("{","").split(',')
        var path = objstr[0].replace("\\","")+"\n"
        var path1 = path.split("/")
        filestr = "FilePath:\n"+path1.last()+"\n"
        filestr += objstr[1]
        filestr.replace(Regex("\""),"")

        tagstr = objstr[2]+"\n"
        tagstr += objstr[3]+"\n"
        tagstr += objstr[4]+"\n"
        tagstr += objstr[5]+"\n"
        tagstr += objstr[6]
        tagstr.replace(Regex("\""),"")
        tagstr.replace("}","")

        fileinfo.text = filestr
        predictioninfo.text = tagstr
        image_view_assets.setImageBitmap(assetsBitmap)
    }
    private fun readfile(): JSONArray{
        val file = File("/storage/emulated/0/Pictures/SearchLight/PredictionData.json")
        val data = File(file.absolutePath).readText()
        var processed = data.split(';')
        processed = processed.dropLast(1)
        var pictures = JSONArray()
        for (data in processed) {
            pictures.put(JSONObject(data))
        }
        return pictures
    }
    private fun getBitmapFromAssets(path: String): Bitmap? {
        return try {
            //[^\\]+$ <- removes the \\'s
            var path = path.replace("\\","")
            BitmapFactory.decodeFile(path)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    companion object {
        private val TAG = Graph_view::class.java.simpleName
    }
}