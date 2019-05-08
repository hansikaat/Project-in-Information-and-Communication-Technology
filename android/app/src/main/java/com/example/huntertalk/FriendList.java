package com.example.huntertalk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FriendList extends AppCompatActivity {
    private DatabaseReference mDatabase, usersRef;
    private TableLayout tableFriendList;
    private HashMap<String,String> nickNames = new HashMap<String, String>();
    public String friendName,friendId;
    private TextView tv, tv1;
    private int k;
    private int f;
    private int i=150;
    private int rowNumber;
    private final FirebaseAuth auth =FirebaseAuth.getInstance();
    final String uid = auth.getCurrentUser().getUid();
    FirebaseUser currentUser= auth.getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        final EditText etSearch = findViewById(R.id.etsearch);
        Button searchButton = findViewById(R.id.searchButton);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");


        mDatabase.child(uid).child("friends").push();

       //enabling back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //create friendlist on start
        mDatabase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableFriendList= (TableLayout) findViewById(R.id.tableFriendList);
                tableFriendList.removeAllViews();
                k=0;
                f=0;
                startFriendList(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //changes text on touch
        etSearch.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etSearch.setHint("Enter email");
                return false;
            }
        });

       //does all the checks and adds on friendlist
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 final    String email = etSearch.getText().toString().trim();
                //checks if email is valid
                    if (TextUtils.isEmpty(email)) {
                        etSearch.setError("Invalid email addresss");
                        return;
                    }
                    if (email.equals("") || !email.contains("@") || !email.contains(".")) {
                        etSearch.setError("Invalid email addresss");
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        etSearch.setError("Enter Email");
                        return;
                    }

            // checks if email of the friend the user wants to add exists
                mDatabase.orderByChild("email").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot email1 : dataSnapshot.getChildren()){
                           String emailToCheck= email1.child("email").getValue().toString();
                          if (email.equals(emailToCheck)){
                               return;
                           }
                       }
                        etSearch.setError("User does not exist");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

          //checks if the friend already exists
            mDatabase.orderByChild("email").equalTo(email).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    final String futureFriend = dataSnapshot.getKey();
                    mDatabase.child(uid).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean alreadyFriends=false;
                            for (DataSnapshot person : dataSnapshot.getChildren()) {
                                String friendName = person.getKey().toString();
                                if (futureFriend.equals(friendName)){
                                    alreadyFriends=true;
                                    break;
                                }
                            }
                            if(alreadyFriends){
                                etSearch.setError("You're already friends!");
                                return;
                            }

                            //adds friend to the list with nickname
                                mDatabase.child(futureFriend).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nickname3 = dataSnapshot.getValue().toString();
                                        mDatabase.child(uid).child("friends").child(futureFriend).setValue(nickname3);
                                        friendName=nickname3;
                                        nickNames.put(futureFriend,nickname3);
                                        createTable();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

                }

        });
    }
 // get all friends from the database and show on the friendlist
    private void startFriendList(DataSnapshot dataSnapshot) {
        for (DataSnapshot friends : dataSnapshot.getChildren()){
            if (friends.getKey().equals("friends")){
                for (DataSnapshot person : friends.getChildren()){
                    UserInformation uInfo = new UserInformation();
                    friendName = person.getValue().toString();
                    friendId = person.getKey().toString();
                    nickNames.put(friendId,friendName);

                }
                createTable();
            }
        }
    }


    private void createTable(){
    rowNumber=0;

        for (String key: nickNames.keySet()){
            String nickname= nickNames.get(key);
            final TableRow row = new TableRow(getBaseContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 10, 5, 10);
            row.setLayoutParams(lp);
            Button btn = new Button(this);
            btn.setText("X");
            btn.setId(i+k);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView text=(TextView) row.getChildAt(1);
                    String id= text.getText().toString();
                    mDatabase.child(uid).child("friends").child(id).removeValue();
                    nickNames.remove(id);
                    tableFriendList.removeView(row);
                }
            });
            tv1 = new TextView(getBaseContext());
            tv1.setText(nickname);
            tv1.setId(i + k + 10000);
            tv = new TextView(getBaseContext());
            tv.setText(key);
            tv.setId(i + k + 1000);
            tv.setVisibility(View.GONE);
            row.setId(k);
            row.addView(tv1, lp);
            row.addView(tv, lp);
            row.addView(btn);
            tableFriendList.addView(row, rowNumber);
            rowNumber++;
        }

    }

//Back button functionality
    @Override
        public boolean onOptionsItemSelected (MenuItem menuItem){
            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    FriendList.this.finish();
                    return true;
            }
            return (super.onOptionsItemSelected(menuItem));
        }
}

