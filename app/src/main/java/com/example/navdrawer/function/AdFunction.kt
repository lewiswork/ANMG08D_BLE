package com.example.navdrawer.function

class AdFunction {

    fun ByteToBits(input :UByte):BooleanArray {
        var arr = BooleanArray(8)
        var mask : Int = 1

        val value = input.toInt()

        for (i in arr.indices) {
            arr[i] = (value and mask)> 0
            mask = mask shl 1
        }

        return arr
    }
}