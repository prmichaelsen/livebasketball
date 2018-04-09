package com.parm.livebasketball;

import com.parm.livebasketball.core.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface FirebaseService {
    @GET("/settings/defaultUser.json")
    Call<Settings> getSettings();

    @POST("/leagues.json")
    Call<League> postLeague(@Body League league);

    @POST("/games.json")
    Call<Game> postGame(@Body Game game);

    @GET("/games.json")
    Call<Map<String,Game>> getGames();

    @GET("/leagues.json")
    Call<Map<String,League>> getLeagues();

    @GET("/tokens.json")
    Call<Map<String,ExpoToken>> getTokens();

    @DELETE("leagues/{leagueId}/.json")
    Call<League> deleteLeague(@Path("leagueId") String leagueId);

    @PUT("serverStatus/.json")
    Call<ServerStatus> putServerStatus(@Body ServerStatus serverStatus);

    @PUT("games/{gameUuid}/.json")
    Call<Game> putGame(@Body Game game, @Path("gameUuid") String gameUuid);

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
