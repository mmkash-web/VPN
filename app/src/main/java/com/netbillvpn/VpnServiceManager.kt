package com.netbillvpn

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class VpnServiceManager : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("VpnServiceManager", "VPN Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
        Log.i("VpnServiceManager", "VPN Service Destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("VpnServiceManager", "VPN Service Started")
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        val builder = Builder()
        builder.setSession("NETBILL VPN")
            .addAddress("10.0.0.2", 24)
            .addRoute("0.0.0.0", 0)
            .setBlocking(true)

        vpnInterface = builder.establish()
        Log.i("VpnServiceManager", "VPN Interface Established")
    }

    fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
        Log.i("VpnServiceManager", "VPN Stopped")
    }
}
