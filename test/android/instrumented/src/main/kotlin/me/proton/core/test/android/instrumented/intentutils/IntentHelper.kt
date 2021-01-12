/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package me.proton.core.test.android.instrumented.intentutils

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.core.test.android.instrumented.CoreTest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object IntentHelper {

    /**
     * Can be used to test file sharing from outside of the app.
     * @param mimeType - file mime type from [MimeTypes]
     * @param fileName - name of the file to share
     */
    fun sendShareFileIntent(mimeType: String, fileName: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation!!
            .executeShellCommand(
                "am start -a android.intent.action.SEND -t $mimeType " +
                    "--eu android.intent.extra.STREAM " +
                    "file:///data/data/${CoreTest.targetContext.packageName}/files/$fileName " +
                    " --grant-read-uri-permission"
            )
    }

    /**
     * Creates new activity result for a file in test app assets.
     * @param fileName - name of the file that will be copied from test app assets to the main app files.
     */
    fun createFileResultFromAssets(fileName: String): Instrumentation.ActivityResult {
        val resultIntent = Intent()

        // Declare variables for test and application context.
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File("${appContext.cacheDir}/$fileName")

        if (!file.exists()) {
            try {
                testContext.assets.open(fileName).use { it.copyTo(FileOutputStream(file)) }
            } catch (e: Exception) {
                throw IOException(e)
            }
        }

        // Build a stubbed result from temp file.
        resultIntent.data = Uri.fromFile(file)
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
    }
}
