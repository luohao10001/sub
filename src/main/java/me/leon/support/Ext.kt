package me.leon.support

import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.util.*

fun String.readFromNet() =
    try {
        String(
            (URL(this).openConnection().apply {
                    //                setRequestProperty("Referer",
                    // "https://pc.woozooo.com/mydisk.php")
                    connectTimeout = 30000
                    readTimeout = 30000
                    setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    setRequestProperty(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36"
                    )
                } as
                    HttpURLConnection)
                .takeIf {
                    //            println("$this __ ${it.responseCode}")
                    it.responseCode == 200
                }
                ?.inputStream
                ?.readBytes()
                ?: "".toByteArray()
        )
    } catch (e: IOException) {
        println("$this read err ${e.message}")
        ""
    }

fun String.queryParamMap() =
    "(\\w+)=([^&]*)".toRegex().findAll(this).fold(mutableMapOf<String, String>()) { acc, matchResult
        ->
        acc.apply { acc[matchResult.groupValues[1]] = matchResult.groupValues[2] }
    }

fun String.queryParamMapB64() =
    "(\\w+)=([^&]*)".toRegex().findAll(this).fold(mutableMapOf<String, String>()) { acc, matchResult
        ->
        acc.apply {
            acc[matchResult.groupValues[1]] =
                matchResult.groupValues[2].urlDecode().replace(" ", "+").b64SafeDecode()
        }
    }

fun Int.slice(group: Int): MutableList<IntRange> {
    val slice = kotlin.math.ceil(this.toDouble() / group.toDouble()).toInt()
    return (0 until group).foldIndexed(mutableListOf()) { index, acc, i ->
        acc.apply {
            acc.add(
                slice * index until ((slice * (i + 1)).takeIf { group - 1 != index } ?: this@slice)
            )
        }
    }
}

fun <T> Any?.safeAs(): T? = this as? T?

fun timeStamp(timeZone: String = "Asia/Shanghai"): String {
    val instance = Calendar.getInstance()
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.time)
}
