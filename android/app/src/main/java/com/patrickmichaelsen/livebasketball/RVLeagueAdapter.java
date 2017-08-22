package com.patrickmichaelsen.livebasketball;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Patrick on 8/20/2017.
 */

public class RVLeagueAdapter extends RecyclerView.Adapter<RVLeagueAdapter.LeagueViewHolder> {
    List<League> leagues;

    RVLeagueAdapter(List<League> leagues){
        this.leagues = leagues;
    }

    @Override
    public LeagueViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.league_card_view, viewGroup, false);
        LeagueViewHolder lvh = new LeagueViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(LeagueViewHolder leagueViewHolder, int i) {
        leagueViewHolder.leagueCountry.setText(leagues.get(i).getCountry());
        leagueViewHolder.leagueName.setText(leagues.get(i).getName());
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
        CardView cv;
        TextView leagueCountry;
        TextView leagueName;

        LeagueViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            leagueCountry = (TextView)itemView.findViewById(R.id.league_country);
            leagueName = (TextView)itemView.findViewById(R.id.league_name);
        }
    }
}
