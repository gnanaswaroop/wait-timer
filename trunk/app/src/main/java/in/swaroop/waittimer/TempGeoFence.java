package in.swaroop.waittimer;

import android.util.Log;

import com.google.android.gms.location.Geofence;

import java.util.UUID;

/**
 * Created by swaroop on 12/14/2014.
 * TempGeoFence creates an in-memory reference and holds the details of the Temporary Geo Fence.
 */
public class TempGeoFence {

    // Instance variables
    private String mId = "";
    private double mLatitude = 0;
    private double mLongitude = 0;
    private float mRadius = 0;
    private long mExpirationDuration;
    private int mTransitionType;
    private Geofence currentGeoFenceInstance = null;

    // TODO: This constructor will probably never be used by this application. Just in case it's required for some other app.
    public TempGeoFence(String mId, double mLatitude, double mLongitude, float mRadius, long mExpirationDuration, int mTransitionType) {
        this.mId = mId;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRadius = mRadius;
        this.mExpirationDuration = mExpirationDuration;
        this.mTransitionType = mTransitionType;
    }

    // Constructor that will be used by the app. Just need Lat/Longitude as inputs and it defaults the rest of the values.
    public TempGeoFence(double mLatitude, double mLongitude) {
        this.mId = UUID.randomUUID().toString();
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRadius = 1001;
        this.mExpirationDuration = Geofence.NEVER_EXPIRE;
        this.mTransitionType = Geofence.GEOFENCE_TRANSITION_EXIT; // Because we only care about Exit.
    }

    public Geofence getCurrentGeoFenceInstance() {
        return currentGeoFenceInstance;
    }

    /**
     * Builds and returns a new Android Geofence.

     * @return
     */
    public Geofence buildGeoFence() {

        if(currentGeoFenceInstance == null) {
            Log.d("Current Geo Fence Instance for Lat/Long - (" + mLatitude + "," + mLongitude + ") doesn't exist - Creating now ", WaitTimerAllTimesCardViewActivity.TAG);

            currentGeoFenceInstance = new Geofence.Builder()
                    .setRequestId(mId)
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(
                            mLatitude, mLongitude, mRadius)
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        } else {
            Log.d("Current Geo Fence Instance for Lat/Long - (" + mLatitude + "," + mLongitude + ") exists - Returning the same ", WaitTimerAllTimesCardViewActivity.TAG);
        }

        return currentGeoFenceInstance;
    }

    public void removeGeoFence() {
        // TODO Write
    }
}
