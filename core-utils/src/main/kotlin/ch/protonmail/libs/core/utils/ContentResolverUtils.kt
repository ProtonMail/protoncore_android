@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

/*
 * Utilities for Android's ContentResolver
 * Author: Davide Farella
 */

/**
 * @return [String] name of the File for the given [uri]
 *
 * @param withExtension if `true` the file name will include its extension.
 * Example: with extension 'picture.jpg' - without extension 'picture'
 * Default if `false`
 *
 * @throws IllegalArgumentException if impossible to get filename for given [uri]
 */
fun ContentResolver.getFileName(uri: Uri, withExtension: Boolean = false): String {
    val nameWithExtension = when (uri.scheme) {
        "file" -> uri.lastPathSegment
        "content" -> {
            val projection = arrayOf(
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            query(uri, projection, null, null, null).use { it!!
                // Try to get the file name from TITLE or DISPLAY_NAME
                try {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
                    it.moveToFirst()
                    it.getString(columnIndex)!!
                } catch (t: Throwable /* NPE if String is null or IllegalAE if Column is not found */) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    it.moveToFirst()
                    it.getString(columnIndex)!!
                }
            }

        }
        else -> throw IllegalArgumentException("Cannot get filename for given uri")
    }

    return if (withExtension) nameWithExtension
    else nameWithExtension.substringBeforeLast('.')
}
