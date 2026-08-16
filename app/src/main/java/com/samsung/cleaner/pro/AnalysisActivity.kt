package com.samsung.cleaner.pro

import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.samsung.cleaner.pro.engines.*
import kotlinx.coroutines.*

class AnalysisActivity : AppCompatActivity() {

    private lateinit var animation: LottieAnimationView
    private lateinit var result: TextView
    private lateinit var btnAnalyze: Button

    private val analysisScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        animation = findViewById(R.id.analysisAnimation)
        result = findViewById(R.id.analysisResult)
        btnAnalyze = findViewById(R.id.btnAnalyze)

        btnAnalyze.setOnClickListener {
            analyzeSystem()
        }
    }

    private fun analyzeSystem() {
        btnAnalyze.isEnabled = false
        btnAnalyze.text = "جاري التحليل..."
        animation.playAnimation()

        analysisScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val baseDir = filesDir.parentFile?.parentFile!!
                    
                    val ram = PerformanceEngine.getRamUsage(this@AnalysisActivity)
                    val temp = BatteryEngine.getBatteryTemperature(this@AnalysisActivity)
                    val battery = BatteryEngine.getBatteryLevel(this@AnalysisActivity)
                    val residuals = ResidualFinderEngine.findResiduals(baseDir).size
                    val duplicates = DuplicateFinderEngine.findDuplicates(baseDir).size
                    val storage = Environment.getExternalStorageDirectory().freeSpace / (1024 * 1024)
                    val cacheSize = AppCleanerEngine.getCacheSize(this@AnalysisActivity)

                    val report = """
                        🔍 تقرير تحليل النظام المتقدم:
                        
                        • استخدام الرام: $ram%
                        • حرارة البطارية: $temp°C
                        • مستوى البطارية: $battery%
                        • الملفات المتبقية: $residuals
                        • الملفات المتكررة: $duplicates
                        • حجم الكاش: ${cacheSize / (1024 * 1024)} MB
                        • المساحة الحرة: $storage MB
                        
                        ${
                            when {
                                ram > 80 -> "⚠ تحذير: استخدام الرام مرتفع"
                                temp > 40 -> "⚠ تحذير: حرارة البطارية مرتفعة"
                                duplicates > 10 -> "⚠ يوجد $duplicates ملف متكرر"
                                else -> "✔ النظام بحالة جيدة"
                            }
                        }
                    """.trimIndent()

                    withContext(Dispatchers.Main) {
                        animation.setAnimation(R.raw.success_animation)
                        animation.playAnimation()
                        result.text = report
                        btnAnalyze.isEnabled = true
                        btnAnalyze.text = "تحليل النظام الآن"
                    }
                }
            } catch (e: Exception) {
                result.text = "❌ خطأ في التحليل: ${e.message}"
                btnAnalyze.isEnabled = true
                btnAnalyze.text = "إعادة المحاولة"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        analysisScope.cancel()
    }
}
