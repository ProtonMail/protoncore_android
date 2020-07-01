import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.ScriptHandlerScope
import studio.forface.easygradle.dsl.`kotlin-gradle-plugin`
import studio.forface.easygradle.dsl.`serialization-gradle-plugin`
import studio.forface.easygradle.dsl.android.`android-gradle-plugin`

val ScriptHandlerScope.classpathDependencies: DependencyHandlerScope.() -> Unit get() = {
    classpath(`kotlin-gradle-plugin`)
    classpath(`serialization-gradle-plugin`)
    classpath(`android-gradle-plugin`)
}
