package com.example.powerstateforcpu

public class MyThread() : Thread() {

    @Volatile
    private var cancellation = 0
    private var count = 0

    constructor (k: Int) : this() {
        count = 100 * k
    }



    fun Cancel() {
        cancellation = 1
    }

    public override  fun run() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND)

        while (cancellation == 0 && count != 0) {
            for (i in 0 until 1000) {
                for (j in 0 until  1000) {
                    var numbers = intArrayOf(7, 2, -5, 0, 3, 2, 12, -5, 30)

                    bubbleSort(numbers)
                }
            }
            count--

//            ++HowManyTimesArrayWasSorted
        }
    }

    private fun bubbleSort(numbers: IntArray) {
        for (pass in 0 until (numbers.size - 1)) {
            for (currentPosition in 0 until (numbers.size - 1)) {
                if (numbers[currentPosition] > numbers[currentPosition + 1]) {
                    val temp = numbers[currentPosition]
                    numbers[currentPosition] = numbers[currentPosition + 1]
                    numbers[currentPosition + 1] = temp
                }
            }
        }
    }

    public var HowManyTimesArrayWasSorted: Int = 0
        private set
}