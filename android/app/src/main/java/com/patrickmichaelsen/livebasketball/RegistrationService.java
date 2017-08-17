package com.patrickmichaelsen.livebasketball;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by Patrick on 7/12/2017.
 */

public class RegistrationService extends IntentService {

    public RegistrationService() {
        super("RegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID myID = InstanceID.getInstance(this);
        String registrationToken = null;
        try {
            registrationToken = myID.getToken(
                    getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );
        } catch (IOException e) {
           e.printStackTrace();
        }
        GcmPubSub subscription = GcmPubSub.getInstance(this);
        if(registrationToken != null){
            try {
                Log.e("subscribe", "here");
                subscription.subscribe(registrationToken,"/topics/live_basketball", null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("subscribe", "no, here");
            }
        }else{
            Log.e("RegistrationService","Could not get registration token");
        }
        Log.d("Registration Token", registrationToken);
    }
}
