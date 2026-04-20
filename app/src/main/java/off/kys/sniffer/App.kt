package off.kys.sniffer

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import off.kys.sniffer.data.AdBlocker

class App : Application() {

    // A scope tied to the application lifecycle
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Launching on IO doesn't block the UI thread
        applicationScope.launch(Dispatchers.IO) {
            AdBlocker.initialize(this@App)
        }
    }
}