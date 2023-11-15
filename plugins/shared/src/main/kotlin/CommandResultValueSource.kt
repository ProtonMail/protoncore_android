import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject

abstract class CommandResultValueSource @Inject constructor(
    private val execOperations: ExecOperations
) : ValueSource<String, CommandResultValueSource.Parameters> {
    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine(parameters.commandLine)
            workingDir(parameters.workingDir)
            standardOutput = output
        }
        return String(output.toByteArray(), Charset.defaultCharset()).trim()
    }

    interface Parameters : ValueSourceParameters {
        var commandLine: List<String>
        var workingDir: File
    }
}
