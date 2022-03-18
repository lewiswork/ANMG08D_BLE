package com.adsemicon.anmg08d.function

class Function {
    companion object {
        fun byteToBits(input: UByte): BooleanArray {
            var arr = BooleanArray(8)
            var mask: Int = 1

            val value = input.toInt()

            for (i in arr.indices) {
                arr[i] = (value and mask) > 0
                mask = mask shl 1
            }

            return arr
        }

        fun byteToBooleanArray(input: Byte, size: Int): BooleanArray {
            var arr = BooleanArray(size)
            var mask: Int = 1

            val value = input.toInt()

            for (i in arr.indices) {
                arr[i] = (value and mask) > 0
                mask = mask shl 1
            }
            return arr
        }
    }
}