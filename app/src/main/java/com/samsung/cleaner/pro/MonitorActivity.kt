package com.samsung.cleaner.pro

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.samsung.cleaner.pro.engines.*
import kotlinx.coroutines.*

class MonitorActivity : AppCompatActivity() {

    private lateinit var animation: LottieAnimationView
    private lateinit var ramText: TextView
    private lateinit var tempText: TextView
    private lateinit var statusText: TextView
    private lateinit var storageText: TextView
    private lateinit var batteryText: TextView

    private val monitorScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)

        animation = findViewById(R.id.monitorAnimation)
        ramText = findViewById(R.id.liveRam)
        tempText = findViewById(R.id.liveTemp)
        statusText = findViewById(R.id.liveStatus)
        storageText = findViewById(R.id.liveStorage)
        batteryText = findViewById(R.id.liveBattery)

        startMonitoring()
    }

    private fun startMonitoring() {
        monitorScope.launch {
            while (isActive) {
                try {
                    val ram = PerformanceEngine.getRamUsage(this@MonitorActivity)
                    val temp = BatteryEngine.getBatteryTemperature(this@MonitorActivity)
                    val battery = BatteryEngine.getBatteryLevel(this@MonitorActivity)
                    val storage = StorageEngine.getFreeSpace(this@MonitorActivity)

                    ramText.text = "RAM: $ram%"
                    tempText.text = "Battery Temp: $temp°C"
                    batteryText.text = "Battery: $battery%"
                    storageText.text = "Free Space: ${storage / (1024 * 1024)} MB"

                    statusText.text = when {
                        ram > 85 -> "⚠ النظام تحت ضغط شديد"
                        ram > 70 -> "⚠ النظام تحت ضغط"
                        temp > 45 -> "⚠ حرارة مرتفعة جداً"
                        temp > 40 -> "⚠ حرارة مرتفعة"
                        battery < 15 -> "⚠ البطارية منخفضة"
                        else -> "✔ النظام يعمل بشكل ممتاز"
                    }

                    delay(1000)
                } catch (e: Exception) {
                    statusText.text = "❌ خطأ في المراقبة"
                    delay(5000)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorScope.cancel()
    }
}
