package me.proton.core.network.presentation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.cache.AndroidDiskCache
import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import me.proton.core.network.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

/**
 * Install a Java security provider that enables certificate transparency checks.
 *
 * Any network call will then be checked (including WebView for Android > 6.0).
 *
 * Note: Must be called before any other network setup or injection.
 *
 * Example:
 * ```
 * @HiltAndroidApp
 * class ProtonApplication : Application() {
 *     ...
 *     override fun onCreate() {
 *         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
 *             installCertificateTransparencySupport()
 *         }
 *         super.onCreate()
 *         ...
 *     }
 *     ...
 * }
 * ```
 */
@ExcludeFromCoverage
@RequiresApi(Build.VERSION_CODES.N)
public fun Context.installCertificateTransparencySupport(
    logger: CTLogger = CoreCTLogger(),
    diskCache: DiskCache = AndroidDiskCache(this@installCertificateTransparencySupport),
    failOnError: Boolean = true,
): Unit = installCertificateTransparencyProvider {
    this.logger = logger
    this.diskCache = diskCache
    this.failOnError = failOnError
}

@ExcludeFromCoverage
public class CoreCTLogger : CTLogger {
    override fun log(host: String, result: VerificationResult) {
        CoreLogger.d(LogTag.API_REQUEST, "CertificateTransparency: $host $result")
    }
}
