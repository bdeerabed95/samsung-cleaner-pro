class RealCleaner {
    constructor() {
        this.isNative = false;
        this.checkEnvironment();
    }
    checkEnvironment() {
        if (window.Capacitor && window.Capacitor.Plugins) {
            this.isNative = true;
        }
    }
    async getStorageInfo() {
        if (this.isNative) {
            try {
                const { CleanerPlugin } = window.Capacitor.Plugins;
                return await CleanerPlugin.getStorageInfo();
            } catch (e) {
                console.log('CleanerPlugin not available');
            }
        }
        return this.getBrowserStorageInfo();
    }
    async getBrowserStorageInfo() {
        if (navigator.storage && navigator.storage.estimate) {
            const estimate = await navigator.storage.estimate();
            return {
                total: estimate.quota,
                used: estimate.usage,
                free: estimate.quota - estimate.usage
            };
        }
        return null;
    }
    async clearAppCache(packageName) {
        if (this.isNative) {
            try {
                const { CleanerPlugin } = window.Capacitor.Plugins;
                return await CleanerPlugin.clearCache({ packageName });
            } catch (e) {
                console.log('CleanerPlugin not available');
            }
        }
        return false;
    }
    async cleanTempFiles() {
        if (this.isNative) {
            try {
                const { CleanerPlugin } = window.Capacitor.Plugins;
                return await CleanerPlugin.cleanTempFiles();
            } catch (e) {
                console.log('CleanerPlugin not available');
            }
        }
        return false;
    }
    async boostRAM() {
        if (this.isNative) {
            try {
                const { MemoryManager } = window.Capacitor.Plugins;
                return await MemoryManager.boostRAM();
            } catch (e) {
                console.log('MemoryManager not available');
            }
        }
        return false;
    }
}
window.RealCleaner = RealCleaner;
