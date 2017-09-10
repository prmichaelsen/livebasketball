package com.patrickmichaelsen.livebasketball;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.patrickmichaelsen.livebasketball.R.id.rv;

public class MainActivity extends AppCompatActivity {

    private static RVLeagueAdapter adapter;
    private static List<League> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = new Intent(this,RegistrationService.class);
        this.startService(i);

        list = new ArrayList<League>();
        RecyclerView rv = (RecyclerView)findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        adapter = new RVLeagueAdapter(list, getApplicationContext());
        rv.setAdapter(adapter);
        getLeagues();

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLeagues();
                handler.postDelayed(this, delay);
            }
        }, delay);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    public void getLeagues(){
        Log.e("TIME", String.valueOf(SystemClock.currentThreadTimeMillis()));
        //String url ="http://10.0.2.2:8080/livebasketball/leagues";
        String url ="http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com/livebasketball/leagues";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("REST", (error.getMessage() != null)? error.getMessage() : "Error not found");
                Toast.makeText(getApplicationContext(), "Could not connect with server", Toast.LENGTH_LONG);
            }

        });
        // Add the request to the RequestQueue.
        ApplicationContext.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public void parseResponse(String response){
        // Display the first 500 characters of the response string.
        Gson gson = new Gson();
        Leagues leagues = gson.fromJson(response, Leagues.class);
        Collection<League> collection = leagues.getLeagues().values();
        list = new ArrayList<League>(collection);
        Collections.sort(list);
        adapter.setDataSet(list);
        Log.e("REST", response.substring(0,(response.length() > 500)? 500 : response.length() - 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
