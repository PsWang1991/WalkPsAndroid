package com.example.walkpsandroid

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import java.io.IOException
import java.util.*

/**
 * Created by PS Wang on 2021/4/12
 */
class FetchAddressTask(
    private val context: Context, private val listener: OnTaskCompleted
) : AsyncTask<LocationRecord, Nothing, String>() {

    private val TAG = FetchAddressTask::class.java.simpleName

    interface OnTaskCompleted {
        fun onTaskCompleted(result: String)
    }

    override fun doInBackground(vararg locationRecords: LocationRecord?): String {

        val geoCoder = Geocoder(context, Locale.getDefault())
        val locationRecord = locationRecords[0]
        var addresses: MutableList<Address>? = null
        var resultMessage = ""

        try {

            addresses = geoCoder.getFromLocation(
                locationRecord!!.lastLocation.latitude,
                locationRecord.lastLocation.longitude,
                1
            )

        } catch (ioException: IOException) {

            // Catch network or other I/O problems
            resultMessage = context
                .getString(R.string.service_not_available)
            Log.e(TAG, resultMessage, ioException)

        } catch (illegalArgumentException: IllegalArgumentException) {

            // Catch invalid latitude or longitude values
            resultMessage = context
                .getString(R.string.invalid_lat_long_used);
            Log.e(
                TAG, resultMessage + ". " +
                        "Latitude = " + locationRecord!!.lastLocation.latitude +
                        ", Longitude = " +
                        locationRecord.lastLocation.longitude, illegalArgumentException
            );
        }

        // If no addresses found, print an error message.
        if (addresses == null || addresses.size == 0) {

            if (resultMessage.isEmpty()) {
                resultMessage = context
                    .getString(R.string.no_address_found);
                Log.e(TAG, resultMessage);
            }

        } else {

            // If an address is found, read it into resultMessage
            val address = addresses[0]
            val addressParts: ArrayList<String?> = ArrayList()
            val distance = locationRecord!!.firstLocation.distanceTo(locationRecord.lastLocation)

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread
            for (i in 0..address.maxAddressLineIndex) {
                addressParts.add(address.getAddressLine(i))
            }

            resultMessage = TextUtils.join("\n", addressParts)
            resultMessage += String.format("\n與第一個地點距離: %1$.4f", distance)
        }

        return resultMessage
    }

    override fun onPostExecute(address: String) {

        listener.onTaskCompleted(address)
        super.onPostExecute(address)
    }
}