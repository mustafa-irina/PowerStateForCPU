package com.example.powerstateforcpu

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import java.io.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis
import android.os.Handler

class MainActivity : AppCompatActivity() {

    class CPUInfo {
        var core: String? = null
        var TiO: Long = 0
        var TiI: Long = 0
        var TiOff: Long = 0
        var STemp: Long = 0
        var FTemp: Long = 0
        var mapsFreq =  mutableMapOf<Long, Long>()

        constructor(core: String?, TiO: Long, TiI: Long, TiOff: Long, STemp: Long, FTemp: Long, mapsFreq: MutableMap<Long, Long>) {
            this.core = core
            this.TiO = TiO
            this.TiI = TiI
            this.TiOff = TiOff
            this.STemp = STemp
            this.FTemp = FTemp
            this.mapsFreq = mapsFreq.toSortedMap()
        }
    }

    private var cpuInfo =  mutableMapOf<String, CPUInfo>()
    private var fileStatisticArr = mutableMapOf<String, OutputStreamWriter>()
    private var fileStatistic: OutputStreamWriter? = null
    private var isHeaderWritten = mutableMapOf<String, Boolean>()
    //private var handler: Handler? = null

    fun makeCSVFilesForConcreteK (launch: Long, cpuX: String) {

        var cpuXInfo = cpuInfo.get(cpuX)!!

        var CSV_HEADER = "Core,Time in Online,Time in Idle,Time in Offline,Start Temperature,Finish Temperature"
        for (freq in cpuInfo.get(cpuX)!!.mapsFreq.keys) {
            CSV_HEADER += ',' + freq.toString()
        }
        CSV_HEADER += ",Launch Number"

        var statistics = ""
        statistics += cpuXInfo.core + ','
        statistics += cpuXInfo.TiO.toString() + ','
        statistics += cpuXInfo.TiI.toString() + ','
        statistics += cpuXInfo.TiOff.toString() + ','
        statistics += cpuXInfo.STemp.toString() + ','
        statistics += cpuXInfo.FTemp.toString() + ','
        for (freq in cpuXInfo.mapsFreq!!.iterator())  {
            statistics += (freq.value * 10).toString() + ','
        }
        statistics += launch.toString()
        statistics += '\n'

        if (!isHeaderWritten.get(cpuX)!!) {
            isHeaderWritten[cpuX] = true
            fileStatisticArr.get(cpuX)?.append(CSV_HEADER)
            fileStatisticArr.get(cpuX)?.append('\n')
        }

        fileStatisticArr.get(cpuX)?.append(statistics)
    }

    private var mHelloTextView: TextView? = null
    private var mInternetTextView: TextView? = null
    private var mNEditText: EditText? = null
    private var mKEditText: EditText? = null
    private var worker: MyThread? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHelloTextView = findViewById(R.id.textView) as TextView
        mNEditText = findViewById(R.id.editTextN)
        mKEditText = findViewById(R.id.editTextK)
        //barChartView = findViewById(R.id.chartConsumptionGraph)
        for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
            //fileStatistic = OutputStreamWriter(this.openFileOutput("Table_for_Concrete_" + cpuX + ".csv", Context.MODE_PRIVATE))
            fileStatisticArr.put(cpuX, OutputStreamWriter(this.openFileOutput("Table_for_Concrete_" + cpuX + ".csv", Context.MODE_PRIVATE)))
            isHeaderWritten.put(cpuX, false)
        }
        // this.openFileOutput("Table_for_Concrete_K.csv", Context.MODE_PRIVATE).write("".toByteArray())
        //handler = Handler()

    }

    fun parserCpuIdle (wfi: File, standalone_pc: File, pc: File) : MutableMap<String, Long> { //
        var listStatistic = mutableMapOf<String, Long>()

        val bufferedReadWFI = BufferedReader(FileReader(wfi))
        listStatistic?.put("WFI", bufferedReadWFI.readLine().trim().toLong())
        bufferedReadWFI.close()

        val bufferedReadStandalonePC = BufferedReader(FileReader(standalone_pc))
        listStatistic?.put("Standalone PC", bufferedReadStandalonePC.readLine().trim().toLong())
        bufferedReadStandalonePC.close()

        val bufferedReadPC = BufferedReader(FileReader(pc))
        var tmp =  bufferedReadPC.readLine().trim().toLong()
        listStatistic?.put("Power Collapse", tmp)
        bufferedReadPC.close()
        return listStatistic
    }

    fun parserTimeInState (file: File) : MutableMap<Long, Long> {
        /*val input: InputStream = file.inputStream()
        val baos = ByteArrayOutputStream()
        input.use { it.copyTo(baos) }
        val inputAsString = baos.toString()*/
        var listStatistic = mutableMapOf<Long, Long>()
        //BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
        val bufferedRead = BufferedReader(FileReader(file))
        var freq: String? = ""
        var value: String? = ""
        var i = 0
        var sum = ""
        for (line in bufferedRead.readLines()) {
            val matchedResults = Regex(pattern = """\d+""").findAll(input = line)
            for (matchedText in matchedResults) {
                if (i == 0) {
                    freq = matchedText.value
                    i++
                } else if (i == 1) {
                    value = matchedText.value
                    i = 0
                } else return listStatistic
            }
            /*for (word in line.split(Regex("\\s+"))){
                if (i == 0) {
                    freq = word
                    i++
                } else {
                    value = word.toLong()
                    i = 0
                }
            }*/
            listStatistic.put(freq!!.toLong(), value!!.toLong())
            //sum += value + "\n"
        }
        bufferedRead.close()
        return listStatistic
    }

    fun parserCpuTemp (temp: File) : Long{
        return FileReader(temp).readText().trim().toLong()
    }

    fun <T> diffMap(map1: MutableMap<T, Long>, map2: MutableMap<T, Long>): MutableMap <T, Long> {
        var diff = mutableMapOf<T, Long>()

        for (elem in map1){
            diff.put(elem.key, map2.get(elem.key)!! - elem.value)
        }
        return diff
    }





    fun internetButtonOnClick(view: View) {
        /*val randomId = Math.abs(Random.nextLong()) % 10 + 1
        NetworkService.getInstance()
            .jsonApi
            .getPostWithID(randomId)
            .enqueue(object : retrofit2.Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    val post = response.body()!!
                    mLongernetTextView = findViewById(R.id.textView2) as TextView

                    mLongernetTextView?.text = ""
                    mLongernetTextView?.append("${post.getId()}\n")
                    mLongernetTextView?.append("${post.getUserId()}\n")
                    mLongernetTextView?.append(post.getTitle() + "\n")
                    mLongernetTextView?.append(post.getBody() + "\n")
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {

//                    textView.append("Error occurred while getting request!")
                    t.prLongStackTrace()
                }
            })*/
        var allTime = 0 //общее время работы программы
        var N = 1 //сколько раз запустить тред
        var K = 1 //сколько раз запустить тест
        var textN = ""
        var textK = ""
        runOnUiThread({->mHelloTextView!!.setText("провер очка")})
        //mHelloTextView!!.setText("провер очка")
        try {
            textN = mNEditText!!.getText().toString()
            textK = mKEditText!!.getText().toString()
            N = textN.toInt()
            K = textK.toInt()
        }catch (e: NumberFormatException) {
            //handler!!.post({->mHelloTextView!!.setText("N = 1; K = 1;\nДоигрались")})
            return mHelloTextView!!.setText("нормально сделай\nНапоминаю:\nN - сколько раз запустить тред\nK - сколько раз запустить сортировочку в одном треде")
        }
        for (i in 0 until N) {
            var arrayTimeInState = mutableMapOf<String, File>() //список (ядро, time_in_state)
            var arrayWFI = mutableMapOf<String, File>() //список (ядро, wfi_time)
            var arrayStandalonePC = mutableMapOf<String, File>() //список (ядро, standalone_pc_time)
            var arrayPC = mutableMapOf<String, File>() //список (ядро, pc_time)
            //var arrayTemp = arrayListOf<File>()
            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                arrayTimeInState.put(cpuX, File("./../../../../sys/devices/system/cpu/" + cpuX + "/cpufreq/stats/time_in_state"))
                arrayWFI.put(cpuX, File("./../../../../sys/devices/system/cpu/" + cpuX + "/cpuidle/state0/time"))
                arrayStandalonePC.put(cpuX, File("./../../../../sys/devices/system/cpu/" + cpuX + "/cpuidle/state1/time"))
                arrayPC.put(cpuX, File("./../../../../sys/devices/system/cpu/" + cpuX + "/cpuidle/state2/time"))
                //arrayTemp.add()
            }
//
//            mInternetTextView = findViewById(R.id.textView2) as TextView

            var mapTimeInStateStart = mutableMapOf<String, MutableMap<Long, Long>>() //список ядер и их частот перед стартом треда
            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                mapTimeInStateStart.put(cpuX, parserTimeInState(arrayTimeInState.get(cpuX)!!))
            }

            var mapIdleStart = mutableMapOf<String, MutableMap<String, Long>>() //список ядер и их idle состояний перед стартом треда
            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                mapIdleStart.put(cpuX, parserCpuIdle(arrayWFI.get(cpuX)!!, arrayStandalonePC.get(cpuX)!!, arrayPC.get(cpuX)!!))
            }

            //mHelloTextView!!.setText("work thread")

            val executionTime = measureTimeMillis { //работа треда
                worker = MyThread(K)
                worker?.start()
                //worker?.wait()
                worker?.join()
                worker = null
                //mHelloTextView!!.setText("idle")
            }

            allTime += executionTime.toInt()

            //mInternetTextView?.setText("")

            var mapTimeInStateFinish = mutableMapOf<String, MutableMap<Long, Long>>() //список ядер и их частот после стартом треда
            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                mapTimeInStateFinish.put(cpuX, parserTimeInState(arrayTimeInState.get(cpuX)!!))
            }

            var mapIdleFinish = mutableMapOf<String, MutableMap<String, Long>>() //список ядер и их idle состояний после стартом треда
            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                mapIdleFinish.put(cpuX, parserCpuIdle(arrayWFI.get(cpuX)!!, arrayStandalonePC.get(cpuX)!!, arrayPC.get(cpuX)!!))
            }
            cpuInfo.clear() //подготовка массива данных об энергосостояниях

            var mapSumDiff = mutableMapOf<String, MutableMap<Long, Long>>() //(ядро, (частота, время работы)
            var mapSumDiffIdle = mutableMapOf<String, MutableMap<String, Long>>() //(ядро, (idle-состояние, время пребывания))
            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                mapSumDiff.put(cpuX, diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!))
                mapSumDiffIdle.put(cpuX, diffMap(mapIdleStart.get(cpuX)!!, mapIdleFinish.get(cpuX)!!))
            }

//            var mapSumTiO = mutableMapOf<Long, Long>() //хз
//            for (e in mapSumDiff.get("cpu0")!!.keys) {
//                var sum: Long = 0
//                for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
//                    sum += mapSumDiff.get(cpuX)!!.get(e)!!
//                }
//                mapSumTiO.put(e, sum)
//            }
//
//            var mapSumTiI = mutableMapOf<String, Long>() //хз
//            for (e in mapSumDiffIdle.get("cpu0")!!.keys) {
//                var sum: Long = 0
//                for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
//                    sum += mapSumDiffIdle.get(cpuX)!!.get(e)!!
//                }
//                mapSumTiI.put(e, sum)
//            }

            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                cpuInfo.put(cpuX, CPUInfo(cpuX, diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!).map {it.value}?.sum() * 10,
                    diffMap(mapIdleStart.get(cpuX)!!, mapIdleFinish.get(cpuX)!!).map {it.value}?.sum() / 1000,
                    executionTime - diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!).map {it.value}?.sum() * 10,
                    0, 0, diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!)))
            }

//            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7", "cpu"*/)) {
//                if (cpuX == "cpu") {
//                    cpuInfo.add(CPUInfo(cpuX, executionTime.toLong(), mapSumTiI.map {it.value}?.sum() / 1000, 0,
//                        0, 0, mapSumTiO))
//                } else {
//                    cpuInfo.add(CPUInfo(cpuX, diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!).map {it.value}?.sum() * 10,
//                        diffMap(mapIdleStart.get(cpuX)!!, mapIdleFinish.get(cpuX)!!).map {it.value}?.sum() / 1000,
//                        executionTime - diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!).map {it.value}?.sum() * 10,
//                        0, 0, diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!)))
//                }//(diffMap(mapTimeInStateStart.get(cpuX)!!, mapTimeInStateFinish.get(cpuX)!!).map {it.value}?.sum() * 10 + diffMap(mapIdleStart.get(cpuX)!!, mapIdleFinish.get(cpuX)!!).map {it.value}?.sum() / 1000)
//
//            }

            for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
                makeCSVFilesForConcreteK(i.toLong(), cpuX)
            }


            //mInternetTextView!!.append("time work: " + executionTime.toString())
           // mHelloTextView!!.setText("time work: " + allTime + "\nПроверь таблицу")

        }
        for (cpuX in arrayListOf("cpu0", "cpu1", "cpu2", "cpu3"/*, "cpu4", "cpu5", "cpu6", "cpu7"*/)) {
            fileStatisticArr.get(cpuX)?.close()
        }
        mHelloTextView!!.setText("time work: " + allTime + "\nПроверь таблицу")
    }
}
