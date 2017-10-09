package com.parm.livebasketball;

import com.parm.livebasketball.core.League;
import com.parm.livebasketball.core.Leagues;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.Map;

public interface FirebaseService {
    @POST("/leagues.json")
    Call<League> postLeague(@Body League league);

    @GET("/leagues.json")
    Call<Map<String,League>> getLeagues();
}
