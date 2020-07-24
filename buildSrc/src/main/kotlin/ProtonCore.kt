import org.gradle.api.JavaVersion

/**
 * An object containing params for the Project
 * @author Davide Farella
 */
object ProtonCore {

    /** The Android API level as target of the App */
    const val targetSdk = 28
    /** The Android API level required for run the App */
    const val minSdk = 21
    /** The version of the JDK  */
    val jdkVersion = JavaVersion.VERSION_1_8
}
