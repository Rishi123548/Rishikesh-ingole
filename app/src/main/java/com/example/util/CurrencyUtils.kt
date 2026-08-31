package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object CurrencyUtils {

    /**
     * Formats a number into Indian Currency standard: e.g. ₹1,50,000 or ₹25,999.00
     */
    fun formatInr(amount: Double, showDecimals: Boolean = false): String {
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        val longVal = absAmount.toLong()
        val decimalVal = Math.round((absAmount - longVal) * 100)

        val strVal = longVal.toString()
        val result = StringBuilder()

        if (strVal.length <= 3) {
            result.append(strVal)
        } else {
            val lastThree = strVal.substring(strVal.length - 3)
            val rest = strVal.substring(0, strVal.length - 3)

            val restFormatted = StringBuilder()
            var count = 0
            for (i in rest.length - 1 downTo 0) {
                restFormatted.append(rest[i])
                count++
                if (count == 2 && i != 0) {
                    restFormatted.append(",")
                    count = 0
                }
            }
            result.append(restFormatted.reverse().toString())
            result.append(",")
            result.append(lastThree)
        }

        val prefix = if (isNegative) "-₹" else "₹"
        return if (showDecimals && decimalVal > 0) {
            "$prefix$result.${String.format("%02d", decimalVal)}"
        } else {
            "$prefix$result"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    /**
     * Converts amount to Words (Indian Numbering System: Lakhs, Crores)
     */
    fun amountToWords(amount: Double): String {
        val num = amount.toLong()
        if (num == 0L) return "Rupees Zero Only"

        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
        )
        val tens = arrayOf(
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        )

        fun convertLessThanOneThousand(n: Int): String {
            var current = ""
            var remainder = n
            if (remainder >= 100) {
                current += units[remainder / 100] + " Hundred "
                remainder %= 100
            }
            if (remainder >= 20) {
                current += tens[remainder / 10] + " "
                remainder %= 10
            }
            if (remainder > 0) {
                current += units[remainder] + " "
            }
            return current.trim()
        }

        var temp = num
        var result = ""

        val crore = (temp / 10000000).toInt()
        temp %= 10000000

        val lakh = (temp / 100000).toInt()
        temp %= 100000

        val thousand = (temp / 1000).toInt()
        val hundred = (temp % 1000).toInt()

        if (crore > 0) {
            result += convertLessThanOneThousand(crore) + " Crore "
        }
        if (lakh > 0) {
            result += convertLessThanOneThousand(lakh) + " Lakh "
        }
        if (thousand > 0) {
            result += convertLessThanOneThousand(thousand) + " Thousand "
        }
        if (hundred > 0) {
            result += convertLessThanOneThousand(hundred) + " "
        }

        return "Rupees ${result.trim()} Only"
    }
}
