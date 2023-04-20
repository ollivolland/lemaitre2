package datas

import android.app.Dialog
import android.content.Context
import android.widget.Spinner
import android.widget.TextView
import com.ollivolland.lemaitre2.Client
import com.ollivolland.lemaitre2.MyClientThread
import com.ollivolland.lemaitre2.MySocket
import com.ollivolland.lemaitre2.R
import config

class HostData private constructor(val hostName:String, val clients: Array<Client>) {
    val mySockets:Array<MySocket> = Array(clients.size) { i -> MyClientThread(clients[i].ipWifiP2p, clients[i].port) }
    val lastUpdate:Array<Long> = Array(clients.size) { 0 }
    var command:String = COMMAND_CHOICES[0]
    var flavor:Long = FLAVOR_CHOICES[0]
    var delta:Long = DELTA_CHOICES[0]
    var videoLength:Long = DURATION_CHOICES[0]

    init {
        for (x in mySockets) x.write("fin")
    }

    companion object {
        const val COMMAND_KURZ = "kKurz"
        const val COMMAND_MITTEL = "kMittel"
        const val COMMAND_LANG = "kLang"
        const val COMMAND_BIEP = "biep"
        val COMMAND_CHOICES = arrayOf(COMMAND_KURZ, COMMAND_MITTEL, COMMAND_LANG, COMMAND_BIEP)
        val COMMAND_DESCRIPTIONS = arrayOf("Wettkampf 1-2s", "Wettkampf 2-3s", "Wettkampf 3-4s", "Biep")
        val FLAVOR_CHOICES = arrayOf(10_000L, 20_000L, 30_000L)
        val FLAVOR_DESCRIPTIONS = arrayOf("10s", "20s", "30s")
        val DURATION_CHOICES = arrayOf(10_000L, 30_000L, 60_000L)
        val DURATION_DESCRIPTIONS = arrayOf("duration 10s", "duration 30s", "duration 60s")
        val DELTA_CHOICES = arrayOf(3_000L, 10_000L, 60_000L)
        val DELTA_DESCRIPTIONS = arrayOf("3s", "10s", "60s")

        var get: HostData = HostData("", arrayOf()); private set

        fun set(hostName: String, clients: MutableList<Client>){
            get = HostData(hostName, clients.toTypedArray())
        }

        fun createRoot(context:Context):Dialog {
            val d = Dialog(context)
            d.setContentView(R.layout.dialog_global)

            val vTitle = d.findViewById<TextView>(R.id.global_tTitle)
            val vSpinnerCommand = d.findViewById<Spinner>(R.id.home_sCommand)
            val vSpinnerFlavor = d.findViewById<Spinner>(R.id.home_sFlavor)
            val vSpinnerLength = d.findViewById<Spinner>(R.id.home_sVideoLength)
            val vSpinnerDelta = d.findViewById<Spinner>(R.id.home_sDelta)

            vTitle.text = "Kommando"
            vSpinnerCommand.config(COMMAND_DESCRIPTIONS, COMMAND_CHOICES.indexOf(get.command)) { i -> get.command = COMMAND_CHOICES[i] }
            vSpinnerFlavor.config(FLAVOR_DESCRIPTIONS, FLAVOR_CHOICES.indexOf(get.flavor)) { i -> get.flavor = FLAVOR_CHOICES[i] }
            vSpinnerLength.config(DURATION_DESCRIPTIONS, DURATION_CHOICES.indexOf(get.videoLength)) { i -> get.videoLength = DURATION_CHOICES[i] }
            vSpinnerDelta.config(DELTA_DESCRIPTIONS, DELTA_CHOICES.indexOf(get.delta)) { i -> get.delta = DELTA_CHOICES[i] }

            d.show()
            return d
        }
    }
}