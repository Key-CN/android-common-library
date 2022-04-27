package io.keyss.library.common.utils

import java.io.BufferedReader
import java.io.BufferedWriter

/**
 * @author Key
 * Time: 2021/07/12 14:06
 * Description:
 */
object ShellUtil {

    data class Result(val success: Boolean, val text: String)

    fun executeSuShell(vararg cmd: String): Result {
        val joinToString = cmd.joinToString(separator = ";"/*, postfix = ";"*/)
        //println("joinTo su String=$joinToString")
        return executeShell("su", "-c", joinToString)
    }

    fun executeShell(vararg cmd: String): Result {
        if (cmd.isEmpty()) {
            return Result(false, "命令不可以为空")
        }
        val runtime = Runtime.getRuntime()
        val process = try {
            if (cmd.size == 1) runtime.exec(cmd[0]) else runtime.exec(cmd)
        } catch (e: Exception) {
            return Result(false, "命令执行错误，Exception: $e")
        }
        val normalText = try {
            process.inputStream.bufferedReader().use { br ->
                br.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        //println("normalText=$normalText")
        val errorText = try {
            process.errorStream.bufferedReader().use { br ->
                br.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        //println("errorText=$errorText")
        if (try {
                process.exitValue()
                false
            } catch (e: IllegalThreadStateException) {
                //e.printStackTrace()
                true
            }
        ) {
            process.destroy()
        }

        val result = if (errorText.isNullOrBlank()) {
            Result(true, normalText)
        } else {
            Result(false, errorText)
        }
        return result
    }

    fun executeShellStream(
        cmd: String,
        outputBlock: ((br: BufferedWriter) -> Unit)? = null,
        inputBlock: ((br: BufferedReader) -> Unit)? = null,
        errorBlock: ((br: BufferedReader) -> Unit)? = null
    ) {
        val runtime = Runtime.getRuntime()
        val process = try {
            runtime.exec(cmd)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        try {
            process.outputStream.bufferedWriter().use { br ->
                outputBlock?.invoke(br)
                // buffer,防止调用者忘记
                br.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            process.inputStream.bufferedReader().use { br ->
                inputBlock?.invoke(br)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            process.errorStream.bufferedReader().use { br ->
                errorBlock?.invoke(br)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (try {
                process.exitValue()
                false
            } catch (e: IllegalThreadStateException) {
                //e.printStackTrace()
                true
            }
        ) {
            process.destroy()
        }
    }
}