import android.content.Context
import android.content.Intent
import android.os.Build

class CleanerBridge {
    
    companion object {
        fun startFullCleaning(context: Context) {
            val intent = Intent(context, CleanerServerService::class.java).apply {
                action = CleanerServerService.ACTION_START_CLEANING
            }
            startService(context, intent)
        }
        
        fun startCacheCleaning(context: Context) {
            val intent = Intent(context, CleanerServerService::class.java).apply {
                action = CleanerServerService.ACTION_CLEAN_CACHE
            }
            startService(context, intent)
        }
        
        fun startJunkCleaning(context: Context) {
            val intent = Intent(context, CleanerServerService::class.java).apply {
                action = CleanerServerService.ACTION_CLEAN_JUNK
            }
            startService(context, intent)
        }
        
        fun startRAMBoost(context: Context) {
            val intent = Intent(context, CleanerServerService::class.java).apply {
                action = CleanerServerService.ACTION_BOOST_RAM
            }
            startService(context, intent)
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, CleanerServerService::class.java).apply {
                action = CleanerServerService.ACTION_STOP_SERVICE
            }
            startService(context, intent)
        }
        
        private fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
