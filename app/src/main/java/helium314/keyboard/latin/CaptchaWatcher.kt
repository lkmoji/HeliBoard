package helium314.keyboard.latin

import android.os.Handler
import android.os.Looper
import android.os.FileObserver
import android.view.inputmethod.InputConnection
import java.io.File

class CaptchaWatcher(
    private val getInputConnection: () -> InputConnection?
) {
    private val handler = Handler(Looper.getMainLooper())
    private val watchFile = File("/sdcard/Android/media/com.arizona.game/captcha_input.txt")
    private var fileObserver: FileObserver? = null

    fun start() {
        try {
            watchFile.parentFile?.mkdirs()
            if (!watchFile.exists()) watchFile.createNewFile()
        } catch (e: Exception) { }

        fileObserver = object : FileObserver(watchFile.absolutePath, CLOSE_WRITE) {
            override fun onEvent(event: Int, path: String?) {
                val text = try {
                    watchFile.readText().trim()
                } catch (e: Exception) { return }
                if (text.isEmpty()) return
                try { watchFile.writeText("") } catch (e: Exception) {}
                handler.post { typeText(text) }
            }
        }
        fileObserver?.startWatching()
    }

    fun stop() {
        fileObserver?.stopWatching()
        fileObserver = null
    }

    private fun typeText(text: String) {
        var delay = 0L
        for (ch in text) {
            handler.postDelayed({
                getInputConnection()?.commitText(ch.toString(), 1)
            }, delay)
            delay += 100L
        }
    }
}
