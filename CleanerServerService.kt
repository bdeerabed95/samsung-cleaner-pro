import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CleanerServerService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    companion object {
        const val CHANNEL_ID = "CleanerServerChannel"
        const val ACTION_START_CLEANING = "ACTION_START_CLEANING"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_CLEAN_CACHE = "ACTION_CLEAN_CACHE"
        const val ACTION_CLEAN_JUNK = "ACTION_CLEAN_JUNK"
        const val ACTION_BOOST_RAM = "ACTION_BOOST_RAM"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CLEANING -> {
                startForeground(1, createNotification("جاري فحص وتنظيف النظام..."))
                executeFullCleaningTask()
            }
            ACTION_CLEAN_CACHE -> {
                startForeground(1, createNotification("جاري تنظيف الكاش..."))
                executeCacheCleaningTask()
            }
            ACTION_CLEAN_JUNK -> {
                startForeground(1, createNotification("جاري تنظيف الملفات المهملة..."))
                executeJunkCleaningTask()
            }
            ACTION_BOOST_RAM -> {
                startForeground(1, createNotification("جاري تحرير الذاكرة..."))
                executeRAMBoostTask()
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun scanAndClean(directory: File): Long {
        var freedBytes = 0L
        
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            files?.forEach { file ->
                if (file.isDirectory) {
                    freedBytes += scanAndClean(file)
                } else if (file.name.endsWith(".tmp") || 
                          file.name.endsWith(".log") || 
                          file.name.endsWith(".cache")) {
                    if (file.exists()) {
                        freedBytes += file.length()
                        file.delete()
                    }
                }
            }
        }
        return freedBytes
    }

    private fun executeFullCleaningTask() {
        serviceScope.launch {
            try {
                val targetDirs = listOf(
                    File(getExternalFilesDir(null)?.parentFile?.parentFile, "Android/data"),
                    File(getExternalFilesDir(null)?.parentFile, "cache"),
                    cacheDir,
                    externalCacheDir
                )
                
                var totalFreed = 0L
                
                for (dir in targetDirs) {
                    if (dir != null && dir.exists()) {
                        totalFreed += scanAndClean(dir)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    val freedMB = (totalFreed / (1024 * 1024)).toInt()
                    updateNotification("✅ تم التنظيف: حرر $freedMB MB")
                    stopSelf()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateNotification("❌ خطأ: ${e.message}")
                    stopSelf()
                }
            }
        }
    }

    private fun executeCacheCleaningTask() {
        serviceScope.launch {
            try {
                var totalFreed = 0L
                cacheDir?.let { totalFreed += scanAndClean(it) }
                externalCacheDir?.let { totalFreed += scanAndClean(it) }
                
                withContext(Dispatchers.Main) {
                    val freedMB = (totalFreed / (1024 * 1024)).toInt()
                    updateNotification("✅ تم تنظيف الكاش: $freedMB MB")
                    stopSelf()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateNotification("❌ خطأ: ${e.message}")
                    stopSelf()
                }
            }
        }
    }

    private fun executeJunkCleaningTask() {
        serviceScope.launch {
            try {
                var totalFreed = 0L
                val tempDirs = listOf(
                    File(getExternalFilesDir(null)?.parentFile, "temp"),
                    File(getExternalFilesDir(null)?.parentFile, "tmp")
                )
                
                for (dir in tempDirs) {
                    if (dir.exists()) {
                        totalFreed += scanAndClean(dir)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    val freedMB = (totalFreed / (1024 * 1024)).toInt()
                    updateNotification("✅ تم تنظيف الملفات المهملة: $freedMB MB")
                    stopSelf()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateNotification("❌ خطأ: ${e.message}")
                    stopSelf()
                }
            }
        }
    }

    private fun executeRAMBoostTask() {
        serviceScope.launch {
            try {
                val runtime = Runtime.getRuntime()
                runtime.gc()
                System.gc()
                
                withContext(Dispatchers.Main) {
                    updateNotification("✅ تم تحرير الذاكرة")
                    stopSelf()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateNotification("❌ خطأ: ${e.message}")
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "خادم التنظيف",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("خادم التنظيف يعمل")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_delete)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, createNotification(text))
    }
}
