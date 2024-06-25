package com.example.chatmsn

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private lateinit var messageTextView: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var scrollView: ScrollView

    private lateinit var socket: Socket
    private lateinit var out: PrintWriter
    private lateinit var `in`: BufferedReader

    private val SERVER_ADDRESS = "192.168.25.40"
    private val SERVER_PORT = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageTextView = findViewById(R.id.messageTextView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        scrollView = findViewById(R.id.scrollView)

        sendButton.setOnClickListener {
            sendMessage()
        }


        // Iniciar a conexão com o servidor em uma coroutine
        CoroutineScope(Dispatchers.IO).launch {
            connectToServer()
        }
    }

    private suspend fun connectToServer() {
        fun showNotification(message: String?) {

        }
        try {
            socket = Socket(SERVER_ADDRESS, SERVER_PORT)
            out = PrintWriter(socket.getOutputStream(), true)
            `in` = BufferedReader(InputStreamReader(socket.getInputStream()))

            
            // Receber mensagens do servidor em um loop
            var message: String?
            while (`in`.readLine().also { message = it } != null) {
                withContext(Dispatchers.Main) {
                    messageTextView.append("$message\n")
                    scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessage() {
        val message = messageEditText.text.toString()
        if (message.isNotEmpty()) {
            messageTextView.append("Eu: $message\n")
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }

            // Enviar mensagem para o servidor em uma coroutine
            CoroutineScope(Dispatchers.IO).launch {
                out.println(message)
            }

            messageEditText.text.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
