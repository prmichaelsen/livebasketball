package com.patrickmichaelsen.livebasketball;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
            leagueIsEnabledCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                    //android calls this method every time this view is rendered
                    //to prevent useless rest calls, make sure this
                    // was actually pressed, not just rendered
                    if(!v.isPressed()){
                       return;
                    }
                    league.setEnabled(v.isChecked());
                    Log.i("CHECKBOX", String.format("checkbox onClick, name: %s%s, isSelected: %s, identityHashCode: %s", leagueCountry, leagueName, v.isChecked(), System.identityHashCode(leagueIsEnabledCheckbox)));
                    Gson gson = new Gson();
                    String params = gson.toJson(league, League.class);
                    JSONObject jsonObject = null;
                    try{
                        jsonObject = new JSONObject(params);
                    }catch(Exception e){
                        Log.e("ERROR:",(e.getMessage() != null )? e.getMessage() : "Could not get error");
                    }
                    String url ="http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com/livebasketball/leagues";
                    if(jsonObject == null){
                        Log.e("Error: ", "Could not parse JSON");
                        Toast.makeText(mContext, "Error saving selected league", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JsonObjectRequest jsObjRequest = new JsonObjectRequest
                            (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("REST:%n %s", response.toString());
                                    Gson gson = new Gson();
                                    com.patrickmichaelsen.livebasketball.Response responseObj = gson.fromJson(response.toString(), com.patrickmichaelsen.livebasketball.Response.class);
                                    Toast.makeText(mContext, responseObj.getReturnData(), Toast.LENGTH_SHORT).show();
                                    Log.i("REST:%n %s", responseObj.toString());
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    Log.e("REST ERROR:",(e.getMessage() != null )? e.getMessage() : "Could not get error");
                                    Toast.makeText(mContext, "Server error saving selected league", Toast.LENGTH_SHORT).show();
                                }
                            });

                    // Access the RequestQueue through your singleton class.
                    ApplicationContext.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsObjRequest);
                }
            });
        }

    }
}
