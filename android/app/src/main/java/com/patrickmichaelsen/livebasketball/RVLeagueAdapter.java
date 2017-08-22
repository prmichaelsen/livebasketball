package com.patrickmichaelsen.livebasketball;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Patrick on 8/20/2017.
 */

public class RVLeagueAdapter extends RecyclerView.Adapter<RVLeagueAdapter.LeagueViewHolder> {
    List<League> leagues;
    Context mContext;

    RVLeagueAdapter(List<League> leagues, Context mContext){
        this.leagues = leagues;
        this.mContext = mContext;
    }

    @Override
    public LeagueViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.league_card_view, viewGroup, false);
        LeagueViewHolder lvh = new LeagueViewHolder(v, mContext);
        return lvh;
    }

    @Override
    public void onBindViewHolder(LeagueViewHolder leagueViewHolder, int i) {
        leagueViewHolder.league = leagues.get(i);
        leagueViewHolder.leagueCountry.setText(leagues.get(i).getCountry());
        leagueViewHolder.leagueName.setText(leagues.get(i).getName());
        leagueViewHolder.leagueIsEnabledCheckbox.setChecked(leagues.get(i).getEnabled());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return leagues.size();
    }

    public static class LeagueViewHolder extends RecyclerView.ViewHolder {
        League league;
        CardView cv;
        TextView leagueCountry;
        TextView leagueName;
        CheckBox leagueIsEnabledCheckbox;

        LeagueViewHolder(View itemView, final Context mContext) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            leagueCountry = (TextView)itemView.findViewById(R.id.league_country);
            leagueName = (TextView)itemView.findViewById(R.id.league_name);
            leagueIsEnabledCheckbox = (CheckBox)itemView.findViewById(R.id.league_is_enabled_checkbox);
            leagueIsEnabledCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    league.setEnabled(leagueIsEnabledCheckbox.isSelected());
                    Log.i("CHECKBOX", String.format("checkbox onClick, isSelected: %s, identityHashCode: %s", leagueIsEnabledCheckbox.isSelected(), System.identityHashCode(leagueIsEnabledCheckbox)));
                    Gson gson = new Gson();
                    String params = gson.toJson(league, League.class);
                    JSONObject jsonObject = null;
                    try{
                        jsonObject = new JSONObject(params);
                    }catch(Exception e){
                        Log.e("ERROR:",e.getMessage());
                    }
                    String url ="http://10.0.2.2:8080/livebasketball/leagues";
                    if(jsonObject == null){
                        Log.e("Error: ", "Could not parse JSON");
                        return;
                    }
                    JsonObjectRequest jsObjRequest = new JsonObjectRequest
                            (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("REST:%n %s", response.toString());
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("REST Error: ", error.getMessage());
                                }
                            });

                    // Access the RequestQueue through your singleton class.
                    ApplicationContext.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsObjRequest);
                }
            });
        }

    }
}
