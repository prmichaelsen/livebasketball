package com.parm.livebasketball;

import com.parm.livebasketball.core.PushNotification;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PushService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AIzaSyA02SxebkYb9TcLvKLc6fHC7QRU9UgbHpE"
    })
    @POST("/")
    Call<PushNotification> sendNotification(@Body PushNotification pushNotification);
}
