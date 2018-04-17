package com.example.zhaok.mychat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


    private View mMainView;
    private RecyclerView chatsList;

    private FirebaseAuth mAuth;
    private DatabaseReference friendsReference;
    private DatabaseReference usersReference;

    String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        friendsReference.keepSynced(true);
        usersReference =FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) mMainView.findViewById(R.id.chats_list);

        chatsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatsList.setLayoutManager(linearLayoutManager);
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Chats,ChatsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (
                        Chats.class,
                        R.layout.all_users_display_layout,
                        ChatsViewHolder.class,
                        friendsReference
                )
        {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Chats model, int position) {


                final String listUserID = getRef(position).getKey();
                usersReference.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("userName").getValue().toString();
                        String thumbImage = dataSnapshot.child("userThumbImage").getValue().toString();
                        String userStatus = dataSnapshot.child("userStatus").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String onlineStatus =  dataSnapshot.child("online").getValue().toString();

                            viewHolder.setUserOnline(onlineStatus);
                        }

                        viewHolder.setUserName(username);
                        viewHolder.setThumbImage(thumbImage, getContext());
                        viewHolder.setUserStatus(userStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                if(dataSnapshot.child("online").exists())
                                {
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("visitUserID",listUserID);
                                    chatIntent.putExtra("userName",username);
                                    startActivity(chatIntent);
                                }
                                else
                                {
                                    usersReference.child(listUserID).child("online")
                                            .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visitUserID",listUserID);
                                            chatIntent.putExtra("userName",username);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        chatsList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public ChatsViewHolder(View itemView) {

            super(itemView);
            mView = itemView;
        }

        public void setUserName(String userName){
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_username);
            userNameDisplay.setText(userName);
        }

        public void setThumbImage(final String thumbImage, final Context ctx) {

            final CircleImageView thumb_Image= (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_Image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile).into(thumb_Image);

                        }
                    });
        }

        public void setUserOnline(String onlineStatus) {
            ImageView onlineStatusView = (ImageView) mView.findViewById(R.id.online_status);

            if(onlineStatus.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }else{

                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus) {
            TextView status = (TextView)mView.findViewById(R.id.all_user_user_status);
            status.setText(userStatus);
        }
    }
}
