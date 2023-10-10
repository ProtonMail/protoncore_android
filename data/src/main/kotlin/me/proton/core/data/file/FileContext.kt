package me.proton.core.data.file

import me.proton.core.domain.entity.UniqueId
import java.io.File

@RequiresOptIn(message = "This API is experimental. It may be changed in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalProtonFileContext

@ExperimentalProtonFileContext
interface FileContext<Directory: UniqueId, Filename: UniqueId> {

    val baseDir: String

    suspend fun getFile(directory: Directory, filename: Filename): File

    suspend fun deleteFile(directory: Directory, filename: Filename): Boolean

    suspend fun deleteDir(directory: Directory): Boolean

    suspend fun deleteAll(): Boolean

    suspend fun writeText(directory: Directory, filename: Filename, data: String): File

    suspend fun readText(directory: Directory, filename: Filename): String?

    suspend fun deleteText(directory: Directory, filename: Filename): Boolean
}
