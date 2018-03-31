package com.parm.livebasketball;

import com.parm.livebasketball.core.League;
import com.parm.livebasketball.core.ServerStatus;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface FirebaseService {
    @POST("/leagues.json")
    Call<League> postLeague(@Body League league);

    @GET("/leagues.json")
    Call<Map<String,League>> getLeagues();

    @DELETE("leagues/{leagueId}/.json")
    Call<League> deleteLeague(@Path("leagueId") String leagueId);

    @PUT("serverStatus/.json")
    Call<ServerStatus> putServerStatus(@Body ServerStatus serverStatus);

    @GET("/leagueSettings/{userId}/{leagueId}/.json")
    Call<Boolean> getUserLeagueSetting(
            @Path("userId") String userId,
            @Path("leagueId") String leagueId
    );

    @GET("/leagueSettings/users/{userId}/leagues/.json")
    Call<Map<String, Boolean>> getUserLeagueSettings(
            @Path("userId") String userId
    );

    @PATCH("/leagueSettings/users/{userId}/leagues.json")
    Call<Map<String, Boolean>> patchUserLeagueSetting(
            @Path("userId") String userId,
            @Body Map<String, Boolean> leagueSetting
    );

    @GET("/users.json")
    Call<Map<String,League>> getUsers();
}
