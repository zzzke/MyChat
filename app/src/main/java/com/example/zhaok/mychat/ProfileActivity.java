package com.example.zhaok.mychat;


import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    private Button sendFriendsRequestBtn;
    private Button declineFriendsRequestBtn;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;

    private DatabaseReference usersReference;
    private DatabaseReference friendsRequestReference;
    private DatabaseReference friendsReference;
    private DatabaseReference notificationReference;
    private FirebaseAuth mAuth;


    private String CURRENT_STATE;
    String senderUserID;
    String receiverUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendsRequestReference.keepSynced(true);

        friendsReference =FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsReference.keepSynced(true);

        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationReference.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();

        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        sendFriendsRequestBtn= (Button) findViewById(R.id.profile_visit_send_req_btn);
        declineFriendsRequestBtn = (Button) findViewById(R.id.profile_visit_decline_req_btn);
        profileImage = (ImageView)findViewById(R.id.profile_visit_user_image);
        profileName = (TextView) findViewById(R.id.profile_visit_username);
        profileStatus =(TextView)findViewById(R.id.profile_visit_user_status);

        CURRENT_STATE ="not_friends";


       usersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String name = dataSnapshot.child("userName").getValue().toString();
               String status = dataSnapshot.child("userStatus").getValue().toString();
               String image = dataSnapshot.child("userImage").getValue().toString();

               profileName.setText(name);
               profileStatus.setText(status);
               Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile).into(profileImage);

               friendsRequestReference.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {

                           if(dataSnapshot.hasChild(receiverUserID)){
                               String requestType = dataSnapshot.child(receiverUserID).child("requests_type").getValue().toString();

                               if(requestType.equals("sent")){
                                   CURRENT_STATE ="request_sent";
                                   sendFriendsRequestBtn.setText("Cancel Friend Request");
                                   declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                   declineFriendsRequestBtn.setEnabled(false);
                               }
                               else if(requestType.equals("received")){
                                   CURRENT_STATE = "request_received";
                                   sendFriendsRequestBtn.setText("Accept Friend Request");

                                   declineFriendsRequestBtn.setVisibility(View.VISIBLE);
                                   declineFriendsRequestBtn.setEnabled(true);


                                   declineFriendsRequestBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           declineFriendsRequest();
                                       }
                                   });
                               }
                           }


                       else
                       {
                           friendsReference.child(senderUserID)
                                   .addListenerForSingleValueEvent(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                           if(dataSnapshot.hasChild(receiverUserID)){
                                               CURRENT_STATE="friends";
                                               sendFriendsRequestBtn.setText("Unfriend this person");

                                               declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                               declineFriendsRequestBtn.setEnabled(false);
                                           }
                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {

                                       }
                                   });
                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });

           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });


        declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
        declineFriendsRequestBtn.setEnabled(false);

       if(!senderUserID.equals(receiverUserID))
       {
           sendFriendsRequestBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   sendFriendsRequestBtn.setEnabled(false);

                   if(CURRENT_STATE.equals("not_friends")){
                       sendFriendsRequest();
                   }
                   if(CURRENT_STATE.equals("request_sent")){
                       cancelFriendsRequest();
                   }
                   if(CURRENT_STATE.equals("request_received")){
                       acceptFriendsRequest();
                   }
                   if(CURRENT_STATE.equals("friends")){
                       unfriendsRequest();
                   }
               }
           });
       }
       else
       {
           sendFriendsRequestBtn.setVisibility(View.INVISIBLE);
           declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
       }


    }

    private void declineFriendsRequest()
    {
        friendsRequestReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsRequestReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendFriendsRequestBtn.setEnabled(true);
                                CURRENT_STATE ="not_friends";
                                sendFriendsRequestBtn.setText("Send Friend Request");

                                declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendsRequestBtn.setEnabled(false);

                            }
                        }
                    });
                }

            }
        });

    }

    private void unfriendsRequest()
    {
        friendsReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendFriendsRequestBtn.setEnabled(true);
                                CURRENT_STATE="not_friends";
                                sendFriendsRequestBtn.setText("Send Freinds Request");

                                declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendsRequestBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendsRequest() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
        final String saveCurrentDate = currentDate.format(calendar.getTime());

        friendsReference.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        friendsReference.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        friendsRequestReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    friendsRequestReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                sendFriendsRequestBtn.setEnabled(true);
                                                                CURRENT_STATE ="friends";
                                                                sendFriendsRequestBtn.setText("Unfriend this person");

                                                                declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                                                declineFriendsRequestBtn.setEnabled(false);

                                                            }
                                                        }
                                                    });
                                                }

                                            }
                                        });
                                    }
                                });
                    }
                });

    }

    private void cancelFriendsRequest() {

        friendsRequestReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsRequestReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendFriendsRequestBtn.setEnabled(true);
                                CURRENT_STATE ="not_friends";
                                sendFriendsRequestBtn.setText("Send Friend Request");

                                declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendsRequestBtn.setEnabled(false);

                            }
                        }
                    });
                }

            }
        });
    }

    private void sendFriendsRequest() {
        friendsRequestReference.child(senderUserID).child(receiverUserID).child("requests_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    friendsRequestReference.child(receiverUserID).child(senderUserID)
                            .child("requests_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                HashMap<String,String> notificationsData = new HashMap<>();
                                notificationsData.put("from",senderUserID);
                                notificationsData.put("type","request");


                                notificationReference.child(receiverUserID).push().setValue(notificationsData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {

                                            sendFriendsRequestBtn.setEnabled(true);
                                            CURRENT_STATE = "request_sent";
                                            sendFriendsRequestBtn.setText("Cancel Friend Request");

                                            declineFriendsRequestBtn.setVisibility(View.INVISIBLE);
                                            declineFriendsRequestBtn.setEnabled(false);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}
