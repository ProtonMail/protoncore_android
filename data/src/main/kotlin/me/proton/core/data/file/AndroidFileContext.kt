package me.proton.core.data.file

import android.content.Context
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UniqueId
import java.io.File
import kotlin.time.Duration.Companion.minutes

/**
 * Android [File] generator based on [Directory] and [Filename].
 */
@ExperimentalProtonFileContext
@Suppress("TooManyFunctions")
class AndroidFileContext<Directory: UniqueId, Filename: UniqueId>(
    override val baseDir: String,
    val context: Context
) : FileContext<Directory, Filename> {

    private val cache = Cache.Builder().expireAfterWrite(1.minutes).build<String, String>()

    private fun getKey(directory: Directory, filename: Filename) = "${directory.id}/${filename.id}"

    private suspend fun getDir() = withContext(Dispatchers.IO) {
        context.getDir(baseDir, Context.MODE_PRIVATE)
    }

    private suspend fun getDir(directory: Directory) = withContext(Dispatchers.IO) {
        File(getDir(), directory.id).also { if (!it.exists()) it.mkdirs() }
    }

    override suspend fun getFile(
        directory: Directory,
        filename: Filename
    ): File = File(getDir(directory), filename.id)

    override suspend fun deleteFile(
        directory: Directory,
        filename: Filename
    ): Boolean = withContext(Dispatchers.IO) {
        cache.invalidate(getKey(directory, filename))
        getFile(directory, filename).delete()
    }

    override suspend fun deleteDir(directory: Directory) = withContext(Dispatchers.IO) {
        cache.invalidateAll()
        getDir(directory).deleteRecursively()
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        cache.invalidateAll()
        getDir().deleteRecursively()
    }

    override suspend fun writeText(
        directory: Directory,
        filename: Filename,
        data: String
    ) = withContext(Dispatchers.IO) {
        val file = getFile(directory, filename)
        if (!file.exists()) file.createNewFile()
        cache.invalidate(getKey(directory, filename))
        file.also { it.writeText(data) }
    }

    override suspend fun readText(
        directory: Directory,
        filename: Filename
    ): String? = withContext(Dispatchers.IO) {
        val file = getFile(directory, filename)
        if (!file.exists()) return@withContext null
        cache.get(getKey(directory, filename)) { file.readText() }
    }

    override suspend fun deleteText(
        directory: Directory,
        filename: Filename
    ) = deleteFile(directory, filename)
}
