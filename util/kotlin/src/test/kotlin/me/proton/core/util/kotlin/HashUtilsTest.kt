@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.proton.core.util.kotlin

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HashUtilsTest {

    private fun getTempFile(filename: String) = File.createTempFile("$filename.", "")

    @Test
    fun `file sha`() {
        val file = getTempFile("test_file_sha")
        file.appendText("predictable hash")
        val sha256 = file.sha256()
        val sha512 = file.sha512()
        assertEquals("b347a2b1245d5ea0258c5c6ae71c5d7cc4bcceb50365685244921e9b4347a168", sha256)
        assertEquals("f0cb69c852dbddddb8b28c85dd3d60d7c673a735bbb81fd1bfc46d5693fa3e8a8cbbb46036f40dcdb342745e2d8bb754918e42ef7f10ac71b781e25488a0990c", sha512)
        file.delete()
    }

    @Test
    fun `large file sha`() {
        val file = getTempFile("test_large_file_sha")
        for (i in 0..100000) file.appendText("01234567890012345678900123456789001234567890012345678900123456789001234567890012345678900123456789001234567890")
        val sha256 = file.sha256()
        val sha512 = file.sha512()
        assertEquals("3dba313e894f48aad8b2d07261bd29dc6353b8786b69afa306ce417ce220f521", sha256)
        assertEquals("18443e5c000e23085bc683181ad9eeec9d6ece7208b4214af0e1504f660bcfbebfafb0c0808c3f35a9d374029164d433e94292d2160c0066772ffa46bdbf5433", sha512)
        file.delete()
    }

    @Test
    fun `file empty sha`() {
        val file = getTempFile("test_file_empty_sha")
        file.appendText("")
        val sha256 = file.sha256()
        val sha512 = file.sha512()
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256)
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e", sha512)
        file.delete()
    }

    @Test
    fun `string hmac 256`() {
        val key = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".toByteArray()
        assertEquals("02ef4861a4b9f833aa104a8210f5eb338e231c9532d9c2551aaf76bafb511208", HashUtils.hmacSha256("garçon", key))
        assertEquals("fd80de16c11bdcea2783274f6b7f334093ef95d58c7381c615005614ed77dc94", HashUtils.hmacSha256("apă", key))
        assertEquals("35733f41071d4997876b5bb54acc1d587646bdf1251f9b9c49ee9dc023a69962", HashUtils.hmacSha256("bala", key))
        assertEquals("4f4dee0cd87928027982c6ca280d2c7661073082ed46a82c111880126b0c3e14", HashUtils.hmacSha256("țânțar", key))
        assertEquals("6bed2bff136e165ad54d0a2a9a549481c88aca55d567c93123e0e5b876c291b2", HashUtils.hmacSha256("întuneric", key))
        assertEquals("2b112b1b7ac4fd9dae5a2acd8fcf2e905bd92a06a95dc4495fa012bda93e8607", HashUtils.hmacSha256("mädchen", key))
    }
}
