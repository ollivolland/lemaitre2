package com.ollivolland.lemaitre2

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

abstract class MySocket(private val port: Int, private val type:String) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    protected val myOnSocketListener = mutableListOf<(Socket) -> Unit>()
    protected val myOnCloseListeners = mutableListOf<() -> Unit>()
    protected val myOnReadListeners = mutableListOf<(String) -> Unit>()
    protected lateinit var mOutputStream: OutputStream
    protected lateinit var mInputStream: InputStream
    protected var isOpen = true
    protected var isInputOpen = true

    fun log(mainActivity: MainActivity) {
        mainActivity.log("[$port] $type creating")
        myOnSocketListener.add { socket -> mainActivity.log("[$port] $type created ${socket.localAddress.hostAddress} => ${socket.inetAddress.hostAddress}") }
        myOnCloseListeners.add { mainActivity.log("[$port] $type closed") }
    }

    fun write(s:String) = write(s.encodeToByteArray())
    fun write(byteArray: ByteArray) {
        if(!isOpen) return
        if(!this::mOutputStream.isInitialized) {
            myOnSocketListener.add { write(byteArray) } //  broken
            return
        }

        executor.execute {
            try {
                mOutputStream.write(byteArray)
            } catch (_:Exception) {
                isOpen = false
            }
        }
    }
    fun writeTime(f:()->Long) {
        executor.execute { mOutputStream.write("time=${f()}".encodeToByteArray()) }
    }

    fun close() {
        executor.execute { isInputOpen = false }
        isOpen=false
    }

    fun addOnConfigured(action:(Socket) -> Unit) = myOnSocketListener.add(action)
    fun addOnClose(action:() -> Unit) = myOnCloseListeners.add(action)

    fun addOnRead(action:(String) -> Unit) = myOnReadListeners.add(action)
    fun removeOnRead(action:(String) -> Unit) = myOnReadListeners.remove(action)
}

class MyClientThread(private val inetAddress: String, port: Int): MySocket(port, "client") {
    private val socket: Socket = Socket()

    init {
        thread {
            socket.connect(InetSocketAddress(inetAddress, port), 3000)
            mInputStream = socket.getInputStream()
            mOutputStream = socket.getOutputStream()
            for (x in myOnSocketListener) x(socket)

            val buffer = ByteArray(1024)
            var length:Int
            while(isOpen) {
                try {
                    length = mInputStream.read(buffer)

                    if (length > 0) {
                        val s = String(buffer, 0, length)

                        for (x in myOnReadListeners) x(s)
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                    isOpen = false
                }
            }

            while (!isInputOpen) Thread.sleep(1)

            socket.close()
            for (x in myOnCloseListeners) x()
        }
    }
}

class MyServerThread(port:Int): MySocket(port, "server") {
    private var serverSocket: ServerSocket = ServerSocket(port)
    lateinit var socket: Socket

    init {
        thread {
            socket  = serverSocket.accept()
            mInputStream = socket.getInputStream()
            mOutputStream = socket.getOutputStream()
            for (x in myOnSocketListener) x(socket)

            val buffer = ByteArray(1024)
            var length:Int
            while(isOpen) {
                try {
                    length = mInputStream.read(buffer)

                    if (length > 0) {
                        val s = String(buffer, 0, length)

                        for (x in myOnReadListeners) x(s)
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                    isOpen = false
                }
            }

            while (!isInputOpen) Thread.sleep(1)

            socket.close()
            serverSocket.close()
            for (x in myOnCloseListeners) x()
        }
    }
}