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

object MimeTypes {

    val application = Application
    val text = Text
    val image = Image
    val video = Video

    object Application {
        const val pdf = "application/pdf"
        const val zip = "application/zip"
        const val docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }

    object Text {
        const val plain = "text/plain"
        const val rtf = "text/rtf"
        const val html = "text/html"
        const val json = "text/json"
    }

    object Image {
        const val png = "image/png"
        const val jpeg = "image/jpeg"
        const val gif = "image/gif"
    }

    object Video {
        const val mp4 = "video/mp4"
        const val jp3 = "video/3gp"
    }
}
