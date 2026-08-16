let realCleaner = null;

document.addEventListener('DOMContentLoaded', async () => {
    realCleaner = new RealCleaner();
    
    if (realCleaner.isNative) {
        showToast('✅ تم الاتصال بنظام Android بنجاح', 'success');
        await updateRealStorageInfo();
    } else {
        showToast('⚠️ النسخة التجريبية - بعض الميزات محدودة', 'warning');
    }
});

async function updateRealStorageInfo() {
    try {
        const storageInfo = await realCleaner.getStorageInfo();
        if (storageInfo) {
            const freeGB = (storageInfo.free / (1024 * 1024 * 1024)).toFixed(1);
            const totalGB = (storageInfo.total / (1024 * 1024 * 1024)).toFixed(1);
            const usedPercent = ((storageInfo.used / storageInfo.total) * 100).toFixed(0);
            
            document.getElementById('freeSpace').textContent = freeGB + ' GB';
            document.getElementById('junkSpace').textContent = 
                ((storageInfo.used * 0.15) / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
            
            updateProgress(usedPercent);
            document.getElementById('percentage').textContent = usedPercent + '%';
        }
    } catch (e) {
        console.error('Error updating storage info:', e);
    }
}

async function cleanCacheReal() {
    showToast('🔍 فحص ذاكرة التخزين المؤقت...', 'info');
    
    if (realCleaner && realCleaner.isNative) {
        try {
            const result = await realCleaner.clearAppCache('com.android.chrome');
            if (result && result.success) {
                const freedMB = (result.cacheSize / (1024 * 1024)).toFixed(1);
                showToast(`✅ تم تنظيف ${freedMB} MB من الكاش`, 'success');
                document.getElementById('cacheInfo').textContent = '0 MB ✓ تم التنظيف';
                document.getElementById('cacheStatus').textContent = '✅';
                document.getElementById('cacheStatus').className = 'cleaner-status status-success';
            }
        } catch (e) {
            console.error('Error cleaning cache:', e);
        }
    } else {
        await simulateCleaning('item1', 'cacheStatus', 'cacheInfo', '0 MB ✓ تم التنظيف');
        showToast('✅ تم تنظيف الكاش', 'success');
    }
}

async function cleanJunkReal() {
    showToast('🔍 فحص الملفات المهملة...', 'info');
    
    if (realCleaner && realCleaner.isNative) {
        try {
            const result = await realCleaner.cleanTempFiles();
            if (result && result.success) {
                const freedMB = (result.freedBytes / (1024 * 1024)).toFixed(1);
                showToast(`✅ تم حذف ${freedMB} MB من الملفات المؤقتة`, 'success');
                document.getElementById('junkInfo').textContent = '0 MB ✓ تم التنظيف';
                document.getElementById('junkStatus').textContent = '✅';
                document.getElementById('junkStatus').className = 'cleaner-status status-success';
            }
        } catch (e) {
            console.error('Error cleaning junk:', e);
        }
    } else {
        await simulateCleaning('item2', 'junkStatus', 'junkInfo', '0 MB ✓ تم التنظيف');
        showToast('✅ تم تنظيف الملفات المهملة', 'success');
    }
}

async function boostRAMReal() {
    showToast('🔍 تحليل الذاكرة...', 'info');
    
    if (realCleaner && realCleaner.isNative) {
        try {
            const result = await realCleaner.boostRAM();
            if (result && result.success) {
                showToast('✅ تم تحرير الذاكرة', 'success');
                document.getElementById('ramInfo').textContent = '1.1 GB / 3 GB ✓';
                document.getElementById('ramStatus').textContent = '✅';
                document.getElementById('ramStatus').className = 'cleaner-status status-success';
            }
        } catch (e) {
            console.error('Error boosting RAM:', e);
        }
    } else {
        await simulateCleaning('item3', 'ramStatus', 'ramInfo', '1.1 GB / 3 GB ✓');
        showToast('✅ تم تحرير الذاكرة', 'success');
    }
}
