package com.smality.checkinternetandroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

enum class ConnectionType {
    Wifi, Cellular
}
class NetworkMonitorUtil(context: Context) {
    private var mContext = context
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    lateinit var result: ((isAvailable: Boolean, type: ConnectionType?) -> Unit)

    @Suppress("DEPRECATION")
    fun register() {
        //Android 9 ve üstü versiyonlarda NetworkCallback ile internet kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //uygulama ilk açıldığında internet olmama durumunun kontrolu
            if (connectivityManager.activeNetwork == null) {
                result(false,null)
            }

            //internet varken, sonrasında internet kapatıldığında onLost metodu çalışır
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    super.onLost(network)
                    result(false, null)
                }
                //İnternetin geldiği ağın Wifi mi yoksa mobil yeri mi(Cellular) olduğunun tespiti
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    when {
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            // WIFI
                            result(true,ConnectionType.Wifi)
                        }
                        else -> {
                            // CELLULAR
                            result(true,ConnectionType.Cellular)
                        }
                    }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            //Android 8 ve altı versiyonlarda Intent Filter ile internet kontrolü
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            mContext.registerReceiver(networkChangeReceiver, intentFilter)
        }
    }
    //uygulama kapatıldığında network sinyallerinin dinlemesini durdurulması
    fun unregister() {
        //Android 9 ve üstü versiyonlarda
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val connectivityManager =
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } else {
            //Android 8 ve altı versiyonlarda
            mContext.unregisterReceiver(networkChangeReceiver)
        }
    }
    //Network sinyallerini dinleyerek (BroadcastReceiver), internet değişikliklerini takip etme
    @Suppress("DEPRECATION")
    private val networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo

            if (activeNetworkInfo != null) {
                //Network bağlantı tipinin kontrolu (WIFI ya da Mobil veri)
                when (activeNetworkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        // WIFI
                        result(true, ConnectionType.Wifi)
                    }
                    else -> {
                        // CELLULAR
                        result(true, ConnectionType.Cellular)
                    }
                }
            } else {

                result(false, null)
            }
        }
    }
}