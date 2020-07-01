import com.android.build.gradle.TestedExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

/** Default value for `sharedTest` modules */
private val testVersion = Version(0, 0, 0)

/**
 * Dsl for apply the android configuration to a library or application module
 * @author Davide Farella
 */
fun org.gradle.api.Project.android(

    version: Version = testVersion,
    appId: String? = null,
    minSdk: Int = ProtonCore.minSdk,
    targetSdk: Int = ProtonCore.targetSdk,
    config: ExtraConfig = {}

) = (this as ExtensionAware).extensions.configure<TestedExtension> {

    compileSdkVersion(targetSdk)
    defaultConfig {

        // Params
        appId?.let { applicationId = it }
        this.version = version

        // SDK
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)

        // Other
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        // Annotation processors must be explicitly declared now.  The following dependencies on
        // the compile classpath are found to contain annotation processor.  Please add them to the
        // annotationProcessor configuration.
        // - auto-service-1.0-rc4.jar (com.google.auto.service:auto-service:1.0-rc4)
        //
        // Note that this option ( 👇 ) is deprecated and will be removed in the future.
        // See https://developer.android.com/r/tools/annotation-processor-error-message.html for
        // more details.
        javaCompileOptions.annotationProcessorOptions.includeCompileClasspath = true
    }

    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true

    // Add support for `src/x/kotlin` instead of `src/x/java` only
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    compileOptions {
        sourceCompatibility = ProtonCore.jdkVersion
        targetCompatibility = sourceCompatibility
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lintOptions {
        isAbortOnError = false
        textReport = true
        textOutput("stdout")
    }

    packagingOptions {
        exclude("go/*.java")
        exclude("licenses/*.txt")
        exclude("licenses/*.TXT")
        exclude("licenses/*.xml")
        exclude("META-INF/*.txt")
        exclude("META-INF/plexus/*.xml")
        exclude("org/apache/maven/project/*.xml")
        exclude("org/codehaus/plexus/*.xml")
        exclude("org/cyberneko/html/res/*.txt")
        exclude("org/cyberneko/html/res/*.properties")
    }

    apply(config)
}

typealias ExtraConfig = TestedExtension.() -> Unit
