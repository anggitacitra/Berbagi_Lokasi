package com.anggitacitra.berbagilokasi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.anggitacitra.berbagilokasi.databinding.ActivityMainBinding
import com.anggitacitra.berbagilokasi.modul.InternalFileRepository
import com.anggitacitra.berbagilokasi.modul.Note
import com.anggitacitra.berbagilokasi.modul.NoteRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() , LocationListener {

    private val repo: NoteRepository by lazy { InternalFileRepository(this) }
    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    // Access Sensor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.btngetgps.setOnClickListener {
            getLocation()
        }
        // Menyimpan Data Sensor
        binding.btnlog.setOnClickListener {
            var logDataSensor = binding.editTeksCatatan.text.toString()
            val timeStamp: String = SimpleDateFormat("yy-MM-dd").format(Date())
            binding.editFileName.setText("AnggitaCitra - " + timeStamp + ".txt")
            val logData1 = binding.Alamat.text.toString()
            logDataSensor = "$logDataSensor$logData1\n"
            binding.editTeksCatatan.setText(logDataSensor)
        }
        // Mambaca data sensor
        binding.btnSimpan.setOnClickListener {
            if (binding.editFileName.text.isNotEmpty()) {
                try {
                    if (repo.deleteNote(binding.editFileName.text.toString())) {
                        Toast.makeText(this, "File Could Not Be Save", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "File Success Save", Toast.LENGTH_LONG).show()
                    }
                    repo.addNote(
                        Note(
                            binding.editFileName.text.toString(),
                            binding.editTeksCatatan.text.toString()
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(this, "File Write Failed", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
                binding.editFileName.text.clear()
                binding.editTeksCatatan.text.clear()
            } else {
                Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
            }
        }
        // Mengambil data sensor yang telah disimpan
        binding.btnRead.setOnClickListener {
            if (binding.editFileName.text.isNotEmpty()) {
                try {
                    val note = repo.getNote(binding.editFileName.text.toString())
                    binding.editTeksCatatan.setText(note.noteText)
                } catch (e: Exception) {
                    Toast.makeText(this, "File Read Failed", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnDelete.setOnClickListener {
            if (binding.editFileName.text.isNotEmpty()) {
                try {
                    if (repo.deleteNote(binding.editFileName.text.toString())) {
                        Toast.makeText(this, "File Deleted", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "File Could Not Be Deleted", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "File Delete Failed", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
                binding.editFileName.text.clear()
                binding.editTeksCatatan.text.clear()
            } else {
                Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnshare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                val logData1 = binding.Alamat.text.toString()
                putExtra(Intent.EXTRA_TEXT, "$logData1\n")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)

        }
    }
    // Access Sensor Dan Menampilkan Data Sensor
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    // mendapatkan data lokasi dari perangkat (latitude, longitude)
                    val location: Location? = task.result
                    if (location != null) {
                        // gunakan geocoder utk mendapatkan data lokasi
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        //menampilkan nilai dari geocoder
                        binding.apply {
                            Alamat.text = "Alamat\n${list[0].getAddressLine(0)}"
                        }
                    }
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")

    }
}