package com.samsung.cleaner.pro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.samsung.cleaner.pro.engines.*
import kotlinx.coroutines.*

class OptimizeActivity : AppCompatActivity() {

    private lateinit var animation: LottieAnimationView
    private lateinit var result: TextView
    private lateinit var btnOptimize: Button

    private val optimizeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optimize)

        animation = findViewById(R.id.optimizeAnimation)
        result = findViewById(R.id.optimizeResult)
        btnOptimize = findViewById(R.id.btnOptimizeNow)

        btnOptimize.setOnClickListener {
            optimizeDevice()
        }
    }

    private fun optimizeDevice() {
        btnOptimize.isEnabled = false
        btnOptimize.text = "جاري التحسين..."
        animation.setAnimation(R.raw.scan_animation)
        animation.loop(true)
        animation.playAnimation()

        optimizeScope.launch {
            try {
                val cleanupResult = withContext(Dispatchers.IO) {
                    val baseDir = filesDir.parentFile?.parentFile!!

                    val appCache = AppCleanerEngine.cleanAppCache(this@OptimizeActivity)
                    val appFiles = AppCleanerEngine.cleanAppFiles(this@OptimizeActivity)
                    val systemClean = SystemCleanerEngine.cleanSystem(baseDir)
                    val residuals = ResidualFinderEngine.deleteResiduals(
                        ResidualFinderEngine.findResiduals(baseDir)
                    )
                    val duplicates = DuplicateFinderEngine.findDuplicates(baseDir)
                        .count { it.delete() }
                    val repaired = RepairEngine.repairBrokenFiles(baseDir)
                    val deep = DeepCleanerEngine.deepScanAndClean(baseDir)
                    val ultra = UltraCleanerEngine.ultraClean(baseDir)

                    appCache + appFiles + systemClean + residuals + duplicates + repaired + deep + ultra
                }

                withContext(Dispatchers.Main) {
                    animation.setAnimation(R.raw.success_animation)
                    animation.loop(false)
                    animation.playAnimation()

                    result.text = """
                        ✅ تم تحسين الجهاز بالكامل
                        
                        • ملفات محذوفة: $cleanupResult
                        • النظام: محسن بالكامل
                        
                        الجهاز الآن بأفضل أداء ممكن!
                    """.trimIndent()
                    
                    btnOptimize.isEnabled = true
                    btnOptimize.text = "تحسين الجهاز بالكامل"
                }
            } catch (e: Exception) {
                result.text = "❌ خطأ في التحسين: ${e.message}"
                btnOptimize.isEnabled = true
                btnOptimize.text = "إعادة المحاولة"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        optimizeScope.cancel()
    }
}
