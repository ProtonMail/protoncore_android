package me.proton.core.data.file

import android.content.Context
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UniqueId
import java.io.File
import kotlin.time.Duration.Companion.minutes

/**
 * Android [File] generator based on [Directory] and [Filename].
 */
@ExperimentalProtonFileContext
@Suppress("TooManyFunctions")
class AndroidFileContext<Directory : UniqueId, Filename : UniqueId>(
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
        val key = getKey(directory, filename)
        keyMutex(key).withLock {
            cache.invalidate(key)
            getFile(directory, filename).delete()
        }
    }

    override suspend fun deleteDir(directory: Directory) = withContext(Dispatchers.IO) {
        staticMutex.withLock {
            cache.invalidateAll()
            getDir(directory).deleteRecursively()
        }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        staticMutex.withLock {
            cache.invalidateAll()
            getDir().deleteRecursively()
        }
    }

    override suspend fun writeText(
        directory: Directory,
        filename: Filename,
        data: String
    ) = withContext(Dispatchers.IO) {
        val key = getKey(directory, filename)
        keyMutex(key).withLock {
            val file = getFile(directory, filename)
            if (!file.exists()) file.createNewFile()
            cache.invalidate(key)
            file.also { it.writeText(data) }
        }
    }

    override suspend fun readText(
        directory: Directory,
        filename: Filename
    ): String? = withContext(Dispatchers.IO) {
        val key = getKey(directory, filename)
        keyMutex(key).withLock {
            val file = getFile(directory, filename)
            if (!file.exists()) return@withContext null
            cache.get(key) { file.readText() }
        }
    }

    override suspend fun deleteText(
        directory: Directory,
        filename: Filename
    ) = deleteFile(directory, filename)

    private companion object {
        private val staticMutex: Mutex = Mutex()
        private val mutexMap: MutableMap<String, Mutex> = mutableMapOf()
        private suspend fun keyMutex(key: String) = staticMutex.withLock { mutexMap.getOrPut(key) { Mutex() } }
    }
}
