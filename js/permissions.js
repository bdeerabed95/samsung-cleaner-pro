async function requestStoragePermissions() {
    if (window.Capacitor && window.Capacitor.Plugins) {
        try {
            const permissions = await window.Capacitor.Plugins.Permissions.query({
                name: 'storage'
            });
            
            if (permissions.state !== 'granted') {
                const result = await window.Capacitor.Plugins.Permissions.request({
                    name: 'storage'
                });
                
                if (result.state === 'granted') {
                    showToast('✅ تم منح صلاحيات التخزين', 'success');
                    return true;
                } else {
                    showToast('❌ تم رفض صلاحيات التخزين', 'error');
                    return false;
                }
            }
            return true;
        } catch (e) {
            console.log('Permissions plugin not available');
        }
    }
    return true;
}

document.addEventListener('DOMContentLoaded', async () => {
    await requestStoragePermissions();
});
