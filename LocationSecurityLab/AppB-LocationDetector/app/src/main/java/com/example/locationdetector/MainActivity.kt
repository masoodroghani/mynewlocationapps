package com.example.locationdetector

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var previous: Location?=null
    private val callback=object:LocationCallback(){ override fun onLocationResult(r:LocationResult){ r.lastLocation?.let(::show) } }
    override fun onCreate(b:Bundle?){ super.onCreate(b); setContentView(R.layout.activity_main); findViewById<Button>(R.id.start).setOnClickListener{start()}; findViewById<Button>(R.id.stop).setOnClickListener{client.removeLocationUpdates(callback)} }
    private fun start(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),10); return }
        val req=LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).setMinUpdateIntervalMillis(500).build(); client.requestLocationUpdates(req,callback,Looper.getMainLooper())
    }
    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){ super.onRequestPermissionsResult(r,p,g); if(r==10 && g.firstOrNull()==PackageManager.PERMISSION_GRANTED) start() }
    private fun show(l:Location){
        val p=previous; var calc=0.0; var dist=0f
        if(p!=null){ dist=p.distanceTo(l); val dt=(l.elapsedRealtimeNanos-p.elapsedRealtimeNanos)/1e9; if(dt>0) calc=dist/dt }
        previous=Location(l)
        val mock=if(Build.VERSION.SDK_INT>=31) l.isMock else @Suppress("DEPRECATION") l.isFromMockProvider
        val suspicious=mock || calc>100.0 || (p!=null && dist>1000 && calc>50)
        findViewById<TextView>(R.id.output).text="""Latitude:  %.6f
Longitude: %.6f
Provider:  %s
Accuracy:  %.1f m
isMock:    %s
Reported speed: %.2f m/s
Calculated speed: %.2f m/s
Distance since fix: %.1f m
Bearing: %.1f°

Assessment: %s

Note: isMock=false does not prove a location is genuine.""".trimIndent().format(l.latitude,l.longitude,l.provider,l.accuracy,mock,l.speed,calc,dist,l.bearing,if(suspicious)"SUSPICIOUS / TEST LOCATION" else "NO SIMPLE ANOMALY DETECTED")
    }
    override fun onPause(){ super.onPause(); client.removeLocationUpdates(callback) }
}
