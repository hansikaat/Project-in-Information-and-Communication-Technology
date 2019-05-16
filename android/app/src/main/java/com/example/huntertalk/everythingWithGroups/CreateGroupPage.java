package com.example.huntertalk.everythingWithGroups;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huntertalk.R;
import com.example.huntertalk.userRelated.SettingsPage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class CreateGroupPage extends AppCompatActivity {

    private Button btnCreate;
    private DatabaseReference usersRef, groupRef;
    private TextView friend, tick, tv1;
    private String friendName, friendId;
    private TableLayout tableRecHunted,tableFriends;
    private FusedLocationProviderClient fusedLocationClient;
    private int k = 0;
    private int f = 0;
    private String[][] selected;
    private HashMap<String,String> friends = new HashMap<String, String>();
    private HashMap<String,String> recentlyHunted = new HashMap<String, String>();
    private LatLng lastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        lastKnownLocation = new LatLng(-33.8523341, 151.2106085);
                        if (location != null) {
                            lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });








        setContentView(R.layout.activity_create_group_page);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create New Group");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        groupRef = database.getReference().child("groups");
        usersRef = database.getReference().child("users");
        GroupId groupIdObject = new GroupId();
        final String groupId = Integer.toString(groupIdObject.getId());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();

        btnCreate = (Button) findViewById(R.id.createButton);
        btnCreate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //adds current user as joined to the group and
                usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String nickname = dataSnapshot.child("nickname").getValue().toString();
                        groupRef.child(groupId).child("joined").child(uid).setValue(nickname);

                        usersRef.child(uid).child("currentGroup").setValue(groupId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                /**
                 * Adding people to invite list
                 * Nicknames are stored at 0 ids are at 1
                 */

                for(int i = 0; i < k+f; i++) {
                    if(selected[i][0] != null){
                        groupRef.child(groupId).child("invited").child(selected[i][1]).setValue(selected[i][0]);
                    }
                }

                groupRef.child(groupId).child("locations").child(uid).setValue(lastKnownLocation);

                Intent intent =new Intent(CreateGroupPage.this, InsideGroupActivity.class);
                intent.putExtra("groupID", groupId);
                startActivity(intent);
            }
        });

        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tableRecHunted = (TableLayout) findViewById(R.id.tableGroupMembers1);
                tableRecHunted.removeAllViews();
                k = 0;

                tableFriends = (TableLayout) findViewById(R.id.tableFriends);
                tableFriends.removeAllViews();
                f=0;
            /**
            * Method to output all the recently hunted and friends
            */
                for (DataSnapshot info : dataSnapshot.getChildren()) {
                    if (info.getKey().equals("recentlyHunted") ) {
                        for (DataSnapshot person : info.getChildren()) {
                            friendName = person.getValue().toString();
                            friendId = person.getKey();
                            recentlyHunted.put(friendId, friendName);
                            usersRef.child(friendId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String nickname;
                                    try {
                                        nickname = dataSnapshot.getValue().toString();
                                    } catch (NullPointerException e) {
                                        nickname = "ERROR";
                                    }
                                    recentlyHunted.put(friendId, nickname);

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                        createTable(recentlyHunted, "rc");

                    }
                    else if ( info.getKey().equals("friends")){
                        for (DataSnapshot person : info.getChildren()) {
                            friendName = person.getValue().toString();
                            friendId = person.getKey();
                            friends.put(friendId, friendName);
                        }
                        createTable(friends, "fr");
                    }
                }
                //nicknames are stored at 0 ids are at 1
                selected = new String[f + k][2];
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
    });
}
    /**
     *  Create a table based on Hash Map
     */
    private void createTable(HashMap<String, String> people, String command){
        for (String key: people.keySet()){
            String nickname= people.get(key);
            final String keyForStoringId =key;
            final TableRow row = new TableRow(getBaseContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 15, 5,10);
            row.setLayoutParams(lp);
            tv1 = new TextView(getBaseContext());
            tv1.setText(nickname);
            tv1.setId(f+k + 1000);

            tick = new TextView(getBaseContext());
            tick.setText("\u2713");
            tick.setTextColor(Color.WHITE);
            tick.setTextSize(16);
            tick.setId(f+k + 2000);


            tv1.setTextColor(Color.BLACK);
            tv1.setTextSize(16);


            row.setId(f+k);
            row.addView(tick, lp);
            row.addView(tv1, lp);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int clicked_id = v.getId();
                    friend = (TextView) findViewById(clicked_id + 1000);
                    tick = (TextView) findViewById(clicked_id + 2000);
                    String nameRH = friend.getText().toString();
                    if (selected[clicked_id][0]== null) {
                        friend.setTextColor(Color.parseColor("#355e3b"));
                        tick.setTextColor(Color.parseColor("#355e3b"));
                        selected[clicked_id][0] = nameRH;
                        selected[clicked_id][1] = keyForStoringId;
                    }
                    else {
                        friend.setTextColor(Color.BLACK);
                        tick.setTextColor(Color.WHITE);
                        selected[clicked_id][0] = null;

                    }
                }
            }
         );
        if (command.equals("rc")) {
            tableRecHunted.addView(row, k);
            k++;
        }
        else{
            tableFriends.addView(row, f);
            f++;
        }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                    this.finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
