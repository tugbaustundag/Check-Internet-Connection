package com.smality.checkinternetandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val networkMonitor = NetworkMonitorUtil(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //NetworkMonitorUtil sınıfında elde ettğimiz internet kontrollerinin sonuçlarını
        //kullanıcı arayüzünde gösterme
        networkMonitor.result = { isAvailable, type ->
            runOnUiThread {
                when (isAvailable) {
                    true -> {
                        when (type) {
                            ConnectionType.Wifi -> {
                                internet_status.text = "Wifi Connection"
                            }
                            ConnectionType.Cellular -> {
                                internet_status.text = "Cellular Connection"
                            }
                            else -> { }
                        }
                    }
                    false -> {
                        internet_status.text = "No Connection"
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.register()
    }
    override fun onStop() {
        super.onStop()
        networkMonitor.unregister()
    }

}