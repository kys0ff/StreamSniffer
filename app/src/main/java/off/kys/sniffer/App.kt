package off.kys.sniffer

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import off.kys.sniffer.data.AdBlocker

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        runBlocking(Dispatchers.IO) {
            AdBlocker.initialize(this@App)
        }
    }
}