package com.creativesit.roommanager.util

import java.text.SimpleDateFormat
import java.util.*

val BN_MONTHS = listOf(
    "", "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
)
val BN_WEEKDAYS = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
val BN_DIGITS = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

fun todayIso(): String {
    val cal = Calendar.getInstance()
    return isoDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
}

fun isoDate(year: Int, month: Int, day: Int): String =
    "%04d-%02d-%02d".format(year, month, day)

fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

/** (year, month) থেকে [delta] মাস যোগ/বিয়োগ করে নতুন (year, month) রিটার্ন করে */
fun addMonths(year: Int, month: Int, delta: Int): Pair<Int, Int> {
    val total = (year * 12 + (month - 1)) + delta
    val newYear = Math.floorDiv(total, 12)
    val newMonth = Math.floorMod(total, 12) + 1
    return newYear to newMonth
}

/**
 * মাস/বছর ড্রপডাউনের জন্য সীমাবদ্ধ অপশন তৈরি করে।
 * [maxAheadMonths] = বর্তমান মাস থেকে সর্বোচ্চ কত মাস আগামি পর্যন্ত দেখানো যাবে (0 = শুধু বর্তমান পর্যন্ত)
 * [pastYears] = বর্তমান বছর থেকে কত বছর পিছনে পর্যন্ত অপশন দেখানো হবে (সিস্টেম-শুরুর তারিখ না থাকলে ফলব্যাক)
 */
data class MonthYearBounds(val maxYear: Int, val maxMonth: Int, val minYear: Int, val minMonth: Int = 1) {
    fun yearOptions(): List<Int> = (minYear..maxYear).toList()
    fun monthOptionsFor(year: Int): List<Int> {
        val hi = if (year == maxYear) maxMonth else 12
        val lo = if (year == minYear) minMonth else 1
        return (lo..hi).toList()
    }
    /** নির্বাচিত (month,year) সীমার বাইরে চলে গেলে সবচেয়ে কাছের বৈধ মানে ক্ল্যাম্প করে */
    fun clamp(month: Int, year: Int): Pair<Int, Int> {
        if (year > maxYear || (year == maxYear && month > maxMonth)) return maxMonth to maxYear
        if (year < minYear || (year == minYear && month < minMonth)) return minMonth to minYear
        return month to year
    }
}

/**
 * [systemStartYear]/[systemStartMonth] দেওয়া থাকলে (এডমিন সেটিংসে যা সেট করা আছে) সেটাই
 * সবচেয়ে পুরনো সীমা হবে, না দিলে [pastYears] বছর পিছনে পর্যন্ত ফলব্যাক হিসেবে দেখানো হবে।
 */
fun monthYearBounds(maxAheadMonths: Int, pastYears: Int = 3, systemStartYear: Int? = null, systemStartMonth: Int? = null): MonthYearBounds {
    val (maxYear, maxMonth) = addMonths(currentYear(), currentMonth(), maxAheadMonths)
    val minYear = systemStartYear ?: (currentYear() - pastYears)
    val minMonth = if (systemStartYear != null) (systemStartMonth ?: 1) else 1
    return MonthYearBounds(maxYear = maxYear, maxMonth = maxMonth, minYear = minYear, minMonth = minMonth)
}

fun daysInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun weekdayOf(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, day)
    return BN_WEEKDAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
}

/** ০-৯ ইংরেজি সংখ্যাকে বাংলা সংখ্যায় রূপান্তর করে */
fun toBnDigits(input: String): String = input.map { c -> if (c.isDigit()) BN_DIGITS[c - '0'] else c }.joinToString("")

fun formatMoney(amount: Double?): String {
    val n = (amount ?: 0.0)
    val rounded = if (n == n.toLong().toDouble()) n.toLong().toString() else "%.2f".format(n)
    return toBnDigits(rounded)
}

/** ISO datetime স্ট্রিং (2026-07-23 10:00:00 বা 2026-07-23T10:00:00) কে সহজপাঠ্য বাংলা সময়ে দেখায় */
fun formatDateTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val clean = iso.replace("T", " ").substringBefore(".")
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val date = parser.parse(clean) ?: return iso
        val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
        fmt.format(date)
    } catch (e: Exception) {
        iso
    }
}
