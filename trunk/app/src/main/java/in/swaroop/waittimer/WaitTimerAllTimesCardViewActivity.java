package in.swaroop.waittimer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class WaitTimerAllTimesCardViewActivity extends ActionBarActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public final static String TAG = "WaitTimer";

    private GoogleApiClient mGoogleApiClient;

    //    private TextView mLocationView;
    private PendingIntent mGeofencePendingIntent;
    private TempGeoFence currentActiveGeoFence;

    private RecyclerView mRecyclerView;
    private WaitTimeCardViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static Activity currentActivity;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_timer_all_times_card_view);

        currentActivity = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.all_wait_times_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        mAdapter = new WaitTimeCardViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        servicesConnected();

        Log.d(TAG, "on Create Complete");
    }

    private void initiateGPSLogging() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(WaitTimerAllTimesCardViewActivity.TAG, "Location received: " + location.toString());

        lastKnownLocation = location;

        // TODO: Remove/Unregister the previous GeoFence if a previous one exists.
        mAdapter.insertMoreRecords(location.toString());

        runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });

        // and Disconnect the Listener
        if(currentActiveGeoFence == null) {
            addGeoFence(location);
        }
    }

    private void addGeoFence(Location location) {

        if (currentActiveGeoFence != null) {
            synchronized (currentActiveGeoFence) {
                Toast.makeText(this, "Another active Geo Fence already running, Remove that first", Toast.LENGTH_LONG);
                Log.d(TAG, "Another active Geo Fence already running, Remove that first");
                return;
            }
        }
        mGeofencePendingIntent = getTransitionPendingIntent();
        currentActiveGeoFence = new TempGeoFence(location.getLatitude(), location.getLongitude());

        Geofence activeGeoFence = currentActiveGeoFence.buildGeoFence();
        List<Geofence> allGeoFencesList = new ArrayList();
        allGeoFencesList.add(activeGeoFence);

        PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, allGeoFencesList, mGeofencePendingIntent);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    // Successfully registered
                    //                    if(mCallback != null){
                    //                        mCallback.onGeofencesRegisteredSuccessful();
                    //                    }
                    Toast.makeText(currentActivity, "Geo Fence successfully registered", Toast.LENGTH_LONG);
                    Log.d(TAG, "Geo Fence has been successfully registered");
                } else if (status.hasResolution()) {
                    // Google provides a way to fix the issue
                /*
                status.startResolutionForResult(
                        mContext,     // your current activity used to receive the result
                        RESULT_CODE); // the result code you'll look for in your
                // onActivityResult method to retry registering
                */
                } else {
                    // No recovery. Weep softly or inform the user.
                    Log.e(TAG, "Registering failed: " + status.getStatusMessage());
                }
            }
        });
    }

    public void removeLastActiveGeofence() {
        if(currentActiveGeoFence == null) {
            String msg = "No Active Geo Fence, Start one before you remove it";
            Toast.makeText(this, msg, Toast.LENGTH_LONG);
            Log.d(TAG, msg);
            return;
        }

        synchronized (currentActiveGeoFence) {

            String msg = "Active GeoFence successfully removed";
            currentActiveGeoFence.removeGeoFence();
            currentActiveGeoFence = null;
            Toast.makeText(this, msg, Toast.LENGTH_LONG);
            Log.d(TAG, msg);
        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {

                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        // TODO: Link code to start GPS Fence creation here
                        break;
                }

        }

    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Geofence Detection",
                    "Google Play services is available.");
            // Continue
            initiateGPSLogging();

            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(), TAG);
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wait_timer_all_times_card_view, menu);
        return true;
    }

    // TODO: Add options like "Delete All" here.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_start_geo_fence) {
            Toast.makeText(this, "New Geo Fence Event", Toast.LENGTH_LONG);
            addGeoFence(lastKnownLocation);
        } else if(id == R.id.action_stop_geo_fence) {
            Toast.makeText(this, "Old Geo Fence Deleted", Toast.LENGTH_LONG);
            removeLastActiveGeofence();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
    * Called by Location Services if the attempt to
    * Location Services fails.
    */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {

//            // TODO: Display Error here.
//            /*
//             * If no resolution is available, display a dialog to the
//             * user with the error.
//             */
//            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
