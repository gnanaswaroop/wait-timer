package in.swaroop.waittimer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by swaroop on 12/14/2014.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    protected void onEnteredGeofences(String[] strings) {
        Log.d(WaitTimerAllTimesCardViewActivity.TAG, "onEnter");

        //do something!
        Toast.makeText(this, "onEnter", Toast.LENGTH_SHORT).show();

    }

    protected void onExitedGeofences(String[] strings) {
        Log.d(WaitTimerAllTimesCardViewActivity.TAG, "onExit");

        //do something!
        //do something!
        Toast.makeText(this, "onExit", Toast.LENGTH_SHORT).show();
    }

    protected void onError(int i) {
        Log.e(WaitTimerAllTimesCardViewActivity.TAG, "Error: " + i);

        Toast.makeText(this, "Error: " + i, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event != null){

            if(event.hasError()){
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if(transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    String[] geofenceIds = new String[event.getTriggeringGeofences().size()];
                    for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
                        geofenceIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
                    }

                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        onEnteredGeofences(geofenceIds);
                    } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        onExitedGeofences(geofenceIds);
                    }
                }
            }

        }
    }
}
