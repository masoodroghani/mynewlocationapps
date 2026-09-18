package com.example.locationsimulator

import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.startStatic).setOnClickListener { startStatic() }
        findViewById<Button>(R.id.startRoute).setOnClickListener { startRoute() }
        findViewById<Button>(R.id.stop).setOnClickListener { stopMock() }
    }

    private fun num(id:Int)=findViewById<EditText>(id).text.toString().toDoubleOrNull()
    private fun status(s:String){ findViewById<TextView>(R.id.status).text=s }

    private fun startStatic() {
        val lat=num(R.id.startLat)?:return status("Invalid latitude")
        val lon=num(R.id.startLon)?:return status("Invalid longitude")
        val acc=(num(R.id.accuracy)?:5.0).toFloat()
        job?.cancel(); job=lifecycleScope.launch {
            try { client.setMockMode(true).await(); while(isActive){ inject(lat,lon,acc); status("Static test location: %.6f, %.6f".format(lat,lon)); delay(1000) } }
            catch(e:Exception){ status("Select this app in Developer options → Select mock location app.\n${e.message}") }
        }
    }

    private fun startRoute() {
        val aLat=num(R.id.startLat)?:return status("Invalid start")
        val aLon=num(R.id.startLon)?:return status("Invalid start")
        val bLat=num(R.id.endLat)?:return status("Invalid destination")
        val bLon=num(R.id.endLon)?:return status("Invalid destination")
        val seconds=(num(R.id.duration)?:120.0).toInt().coerceAtLeast(1)
        val acc=(num(R.id.accuracy)?:5.0).toFloat()
        job?.cancel(); job=lifecycleScope.launch {
            try {
                client.setMockMode(true).await()
                for(i in 0..seconds){ ensureActive(); val f=i.toDouble()/seconds; val lat=aLat+(bLat-aLat)*f; val lon=aLon+(bLon-aLon)*f; inject(lat,lon,acc); status("Route ${i}/${seconds}s\n%.6f, %.6f".format(lat,lon)); delay(1000) }
                status("Route complete. Press Stop to disable mock mode.")
            } catch(e:Exception){ status("Select this app in Developer options → Select mock location app.\n${e.message}") }
        }
    }

    private suspend fun inject(lat:Double, lon:Double, acc:Float){
        val l=Location("fused").apply { latitude=lat; longitude=lon; accuracy=acc; time=System.currentTimeMillis(); elapsedRealtimeNanos=SystemClock.elapsedRealtimeNanos() }
        client.setMockLocation(l).await()
    }

    private fun stopMock(){ job?.cancel(); job=null; lifecycleScope.launch { try{client.setMockMode(false).await()}catch(_:Exception){}; status("Status: stopped") } }
    override fun onDestroy(){ job?.cancel(); super.onDestroy() }
}
