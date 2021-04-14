package com.example.walkpsandroid

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity(), FetchAddressTask.OnTaskCompleted {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 55
        private const val TRACKING_LOCATION_KEY = "tracking_location"
    }

    // Views
    private var getLocationButton: Button? = null
    private var locationTextView: TextView? = null
    private var androidImageView: ImageView? = null

    // Animation
    private var rotateAnim: AnimatorSet? = null

    // Location classes
    private var trackingLocation = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var firstLocationRecord = true
    private var firstLocation: Location? = null

    // Initialize the location callbacks.
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init Views
        getLocationButton = findViewById(R.id.button_location)
        locationTextView = findViewById(R.id.textview_location)
        androidImageView = findViewById(R.id.imageview_android)

        // Apply animation on the "Android" image view.
        rotateAnim = AnimatorInflater.loadAnimator(this, R.animator.rotate) as AnimatorSet
        rotateAnim?.setTarget(androidImageView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (savedInstanceState != null) {
            trackingLocation = savedInstanceState
                .getBoolean(TRACKING_LOCATION_KEY, false)
        }

        locationCallback = object : LocationCallback() {
            /**
             * This is the callback that is triggered when the
             * FusedLocationClient updates your location.
             * @param locationResult The result containing the device location.
             */
            override fun onLocationResult(locationResult: LocationResult) {

                if (firstLocationRecord) {
                    firstLocation = Location(locationResult.lastLocation)
                    firstLocationRecord = false
                }

                if (trackingLocation) {
                    FetchAddressTask(
                        this@MainActivity,
                        this@MainActivity
                    ).execute(
                        LocationRecord(
                            lastLocation = locationResult.lastLocation,
                            firstLocation = firstLocation!!
                        )
                    )
                }
            }
        }

        getLocationButton?.setOnClickListener {

            if (trackingLocation) {
                stopTrackingLocation()
            } else {
                startTrackingLocation()
            }
        }
    }

    /**
     * Starts tracking the device. Checks for
     * permissions, and requests them if they aren't present. If they are,
     * requests periodic location updates, sets a loading text and starts the
     * animation.
     */
    private fun startTrackingLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            trackingLocation = true
            fusedLocationClient.requestLocationUpdates(
                getLocationRequest(), locationCallback, null
            )

            // Set a loading text while you wait for the address to be
            // returned
            locationTextView?.text = getString(
                R.string.address_text,
                getString(R.string.loading),
                System.currentTimeMillis()
            )
            getLocationButton?.setText(R.string.stop_tracking_location)
            rotateAnim?.start()

        }
    }

    /**
     * Stops tracking the device. Removes the location
     * updates, stops the animation, and resets the UI.
     */
    private fun stopTrackingLocation() {
        if (trackingLocation) {
            trackingLocation = false
            getLocationButton?.setText(R.string.start_tracking_location)
            locationTextView?.setText(R.string.textview_hint)
            rotateAnim?.end()
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    /**
     * Callback that is invoked when the user responds to the permissions
     * dialog.
     *
     * @param requestCode  Request code representing the permission request
     *                     issued by the app.
     * @param permissions  An array that contains the permissions that were
     *                     requested.
     * @param grantResults An array with the results of the request for each
     *                     permission requested.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {

                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    startTrackingLocation()
                } else {
                    Toast.makeText(
                        this,
                        R.string.location_permission_denied,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onTaskCompleted(result: String) {
        if (trackingLocation) {
            // Update the UI
            locationTextView?.text = getString(
                R.string.address_text,
                result, System.currentTimeMillis()
            )
        }
    }

    override fun onPause() {
        if (trackingLocation) {
            stopTrackingLocation()
            trackingLocation = true
        }
        super.onPause()
    }

    override fun onResume() {
        if (trackingLocation) {
            startTrackingLocation()
        }
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(TRACKING_LOCATION_KEY, trackingLocation)
        super.onSaveInstanceState(outState)
    }
}