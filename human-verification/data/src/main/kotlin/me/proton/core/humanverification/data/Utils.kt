package me.proton.core.humanverification.data

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by dinokadrikj on 6/18/20.
 */

fun Context.readFromAssets(resource: String): String? {
    val inputStream: InputStream = assets.open(resource)

    val bufferReader = BufferedReader(InputStreamReader(inputStream))
    var line: String?
    val text = StringBuilder()

    try {
        while (bufferReader.readLine().also { line = it } != null) {
            text.append(line)
            text.append('\n')
        }
    } catch (e: IOException) {
        return null
    }
    return text.toString()
}
