package com.patrickmichaelsen.livebasketball;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Patrick on 7/12/2017.
 */

public class TokenRefreshListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh(){
        Intent i = new Intent(this, RegistrationService.class);
        startService(i);
    }
}
