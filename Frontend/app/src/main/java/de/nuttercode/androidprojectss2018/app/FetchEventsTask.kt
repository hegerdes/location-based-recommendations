package de.nuttercode.androidprojectss2018.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.location.LocationServices
import de.nuttercode.androidprojectss2018.csi.*
import de.nuttercode.androidprojectss2018.csi.query.QueryResultState
import java.lang.ref.WeakReference

class FetchEventsTask(private val context: WeakReference<Context>, private val callback: FetchEventsTaskCallback): AsyncTask<ClientConfiguration, Void, ArrayList<ScoredEvent>>() {

    @SuppressLint("MissingPermission")
    override fun doInBackground(vararg clientConfiguration: ClientConfiguration): ArrayList<ScoredEvent> {
        if (clientConfiguration.size != 1) throw IllegalArgumentException("The method requires exactly one argument: a ClientConfiguration")

        val clientConfig = clientConfiguration[0]
        val out = ArrayList<ScoredEvent>()

        Log.i(TAG, "Radius in clientConfig: ${clientConfig.radius}")

        Log.i(TAG, "Now listing tags in tagPreferenceConfiguration:")
        for (tag in clientConfig.tagPreferenceConfiguration) {
            Log.i(TAG, tag.name)
        }

        val eventStore = EventStore(clientConfig)   // TODO: Use real user location
        // val locTask = LocationServices.getFusedLocationProviderClient(context.get()!!).lastLocation
        //        .addOnSuccessListener { location -> eventStore.setUserLocation(location.latitude, location.longitude) }   // TODO: Add proper permission check

        // while (!locTask.isComplete) Thread.sleep(500)   // TODO: Better solution?
        eventStore.setUserLocation(52.283, 8.045)
        val eqrs = eventStore.refresh()

        Log.i(TAG, "EQRS: ${eqrs.queryResultState}")
        if (eqrs.queryResultState != QueryResultState.OK) Log.e(TAG, "Query was not successful (${eqrs.queryResultState}), Message: ${eqrs.message}")
        Log.i(TAG, "Now listing all Events in EventStore. There are ${eventStore.all.size} events matching the given tags in the store")
        for (e in eventStore.all) {
            Log.i(TAG, "Event in eventStore: ${e.event.name}")
        }
        Log.i(TAG, "Finished listing all Events in EventStore")

        out.addAll(eventStore.all)
        Log.i(TAG, "Fetched all events and added them to ArrayList")

        return out
    }

    override fun onPostExecute(result: ArrayList<ScoredEvent>) {
        callback.processFetchEventsResult(result)
    }

    companion object {
        const val TAG = "FetchEventsTask"
    }

}

interface FetchEventsTaskCallback {
    fun processFetchEventsResult(result: ArrayList<ScoredEvent>)
}
