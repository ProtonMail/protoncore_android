package me.proton.core.data.file

import androidx.core.util.AtomicFile
import me.proton.core.domain.entity.UniqueId

@RequiresOptIn(message = "This API is experimental. It may be changed in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalProtonFileContext

@ExperimentalProtonFileContext
interface FileContext<Directory: UniqueId, Filename: UniqueId> {

    val baseDir: String

    suspend fun getFile(directory: Directory, filename: Filename): AtomicFile

    suspend fun deleteFile(directory: Directory, filename: Filename): Boolean

    suspend fun deleteDir(directory: Directory): Boolean

    suspend fun deleteAll(): Boolean

    suspend fun writeText(directory: Directory, filename: Filename, data: String): AtomicFile

    suspend fun readText(directory: Directory, filename: Filename): String?

    suspend fun deleteText(directory: Directory, filename: Filename): Boolean
}
