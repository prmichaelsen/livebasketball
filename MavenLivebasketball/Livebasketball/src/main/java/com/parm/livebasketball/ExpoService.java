package com.parm.livebasketball;

import com.parm.livebasketball.core.ExpoNotification;
import retrofit2.Call;
import retrofit2.http.*;


public interface ExpoService {
    @Headers({
            "accept: application/json",
            "accept-encoding: gzip, deflate",
            "content-type: application/json",
    })
    @POST ("https://exp.host/--/api/v2/push/send")
    Call<ExpoNotification> sendNotification(@Body ExpoNotification notif);
}
