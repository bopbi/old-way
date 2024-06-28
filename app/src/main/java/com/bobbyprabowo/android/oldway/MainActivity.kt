package com.bobbyprabowo.android.oldway

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

private const val MESSAGE_UPDATE_TEXT = 1

class MainActivity : AppCompatActivity() {

    private var mainText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainText = findViewById(R.id.main_text)
        val availableProcessor = Runtime.getRuntime().availableProcessors()
        val executorService = Executors.newFixedThreadPool(availableProcessor * 2)

        // Start the network operation using the thread pool
        executorService.execute(NetworkOperation(mainActivityHandler))
    }

    private val mainActivityHandler by lazy {
        Handler(Looper.getMainLooper()) { message ->
            if (message.what == MESSAGE_UPDATE_TEXT) {
                (message.obj as? String)?.let {
                    mainText?.text = it
                    return@Handler true
                } ?: return@Handler false
            }
            return@Handler false
        }
    }

    private class NetworkOperation(private val handler: Handler) : Runnable {
        override fun run() {
            try {
                // Perform network operation
                val url = URL("https://jsonplaceholder.typicode.com/posts/1")
                val urlConnection = url.openConnection() as HttpsURLConnection
                try {
                    val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var inputLine: String?
                    while (input.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    input.close()

                    // Send the result back to the main thread
                    val message = handler.obtainMessage(MESSAGE_UPDATE_TEXT, response.toString())
                    handler.sendMessage(message)
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
