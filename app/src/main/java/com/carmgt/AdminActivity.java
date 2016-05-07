package com.carmgt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.carmgt.com.carmgt.adapter.JourneyListAdapter;
import com.carmgt.com.carmgt.ds.Journey;
import com.carmgt.com.carmgt.util.DividerItemDecoration;
import com.carmgt.com.carmgt.util.RandomNumber;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "AdminActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new JourneyListAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        // Code to Add an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).deleteItem(index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((JourneyListAdapter) mAdapter).setOnItemClickListener(new
           JourneyListAdapter.MyClickListener() {
               @Override
               public void onItemClick(int position, View v) {
                   Log.i(LOG_TAG, " Clicked on Item " + position);
               }
           });
    }

    private ArrayList<Journey> getDataSet() {
        ArrayList results = new ArrayList<Journey>();
        String driver = "";
        for (int index = 0; index < 20; index++) {

            if(index % 2 == 0)
            {
                driver = "Deba";
            }else{
                driver = "Sudeb";
            }
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.add(Calendar.HOUR_OF_DAY, index * -1); // adds one hour
            SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
            Journey obj = new Journey(driver,
                    "Pickup at " + format.format(cal.getTime()), String.valueOf(RandomNumber.randInt(10,200)));
            results.add(index, obj);
        }
        return results;
    }

}
