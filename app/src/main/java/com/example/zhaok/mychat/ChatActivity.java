package com.example.zhaok.mychat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID;
    private String messageReceiverName;


    private Toolbar mToobar;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;
    private ImageButton selectImageBtn;
    private ImageButton sendmessageBtn;
    private EditText inputMessageTxt;
    private RecyclerView messagesList;
    private final List<Messages> userMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    String messageSenderID;

    private static  int Gallery_Pick = 1;

    private StorageReference messageImageStorageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messageReceiverID = getIntent().getExtras().getString("visitUserID");
        messageReceiverName = getIntent().getExtras().getString("userName");

        messageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        mToobar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToobar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View actionBarView = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);

        userNameTitle = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen =(TextView) findViewById(R.id.cutstom_user_last_seen);
        userChatProfileImage =(CircleImageView)findViewById(R.id.custom_profile_image);
        sendmessageBtn = (ImageButton) findViewById(R.id.send_message);
        inputMessageTxt =(EditText)findViewById(R.id.input_message);
        selectImageBtn =(ImageButton)findViewById(R.id.select_image);


        messageAdapter = new MessageAdapter(userMessagesList);
        messagesList =(RecyclerView) findViewById(R.id.messages_list);

        linearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);

        messagesList.setLayoutManager(linearLayoutManager);

        messagesList.setAdapter(messageAdapter);

        fetchMessages();


        userNameTitle.setText(messageReceiverName);

        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String online = dataSnapshot.child("online").getValue().toString();
                final String thumbImage = dataSnapshot.child("userThumbImage").getValue().toString();

                Picasso.with(ChatActivity.this).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                        .into(userChatProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(thumbImage).placeholder(R.drawable.default_profile).into(userChatProfileImage);

                            }
                        });

                if(online.equals("true")){
                    userLastSeen.setText("Online");
                }
                else{

                    LastSeenTime getTime = new LastSeenTime();
                    long  lastSeen = Long.parseLong(online);
                    String lastSeenDisplayTime = getTime.getTimeAgo(lastSeen,getApplicationContext()).toString();
                    userLastSeen.setText(lastSeenDisplayTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendmessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               sendMessage();
            }
        });

        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            final String messageSenderRef = "Messages/"+ messageSenderID +"/"+ messageReceiverID;

            final String messageReceiverRef = "Messages/"+ messageReceiverID +"/"+ messageSenderID;
            DatabaseReference userMessageKey = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                    .push();

            final String messagePushID = userMessageKey.getKey();

            StorageReference filePath = messageImageStorageRef.child(messagePushID+".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                {

                    if(task.isSuccessful())
                    {
                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", downloadUrl);
                        messageTextBody.put("seen",false);
                        messageTextBody.put("type","image");
                        messageTextBody.put("time", ServerValue.TIMESTAMP);
                        messageTextBody.put("from",messageSenderID);

                        Map messageBodyDetails = new HashMap();

                        messageBodyDetails.put(messageSenderRef+ "/"+ messagePushID, messageTextBody);

                        messageBodyDetails.put(messageReceiverRef+ "/"+ messagePushID, messageTextBody);


                        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                            {
                                if(databaseError != null)
                                {
                                    Log.d("Chat Log",databaseError.getMessage().toString());
                                }

                                inputMessageTxt.setText("");
                            }
                        });
                        Toast.makeText(ChatActivity.this, "Picture sent successfully", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Picture not sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });



        }



    }

    private void fetchMessages()
    {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Messages messages = dataSnapshot.getValue(Messages.class);

                userMessagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        String message = inputMessageTxt.getText().toString();

        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(ChatActivity.this, "Please write a message.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/"+ messageSenderID +"/"+ messageReceiverID;

            String messageReceiverRef = "Messages/"+ messageReceiverID +"/"+ messageSenderID;

            DatabaseReference userMessageKey = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                    .push();

            String messagePushID = userMessageKey.getKey();


            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",messageSenderID);

            Map messageBodyDetails = new HashMap();

            messageBodyDetails.put(messageSenderRef+ "/"+ messagePushID, messageTextBody);

            messageBodyDetails.put(messageReceiverRef+ "/"+ messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if(databaseError != null){
                        Log.d("chat Log",databaseError.getMessage().toString());
                    }

                    inputMessageTxt.setText("");

                }
            });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }
}
