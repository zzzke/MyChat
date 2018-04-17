package com.example.zhaok.mychat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private RecyclerView requestsList;
    private View mMainView;

    private DatabaseReference friendsRequestReference;
    private DatabaseReference usersReference;
    private DatabaseReference friendsReference;
    private DatabaseReference friendsReqReference;
    private FirebaseAuth mAuth;
    String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        requestsList = (RecyclerView) mMainView.findViewById(R.id.reqeusts_list);
        // Inflate the layout for this fragment


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(currentUserID);
        usersReference =FirebaseDatabase.getInstance().getReference().child("Users");

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsReqReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

        requestsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        requestsList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests,RequestsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>
                (
                        Requests.class,
                        R.layout.friend_request_all_users_layout,
                        RequestsViewHolder.class,
                        friendsRequestReference

                ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position)
            {
                final String listUserID = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("requests_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.exists())
                        {
                            String requestType = dataSnapshot.getValue().toString();
                            if(requestType.equals("received"))
                            {

                                usersReference.child(listUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        final String username = dataSnapshot.child("userName").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("userThumbImage").getValue().toString();
                                        final String userStatus = dataSnapshot.child("userStatus").getValue().toString();


                                        viewHolder.setUserName(username);
                                        viewHolder.setUserThumbImage(thumbImage, getContext());
                                        viewHolder.setUserStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options[]= new CharSequence[]
                                                        {
                                                               "Accept Friend Request",
                                                                "Cancel Friend Request"

                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Options");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position)
                                                    {

                                                        if(position == 0)
                                                        {
                                                            acceptFriendsRequest(listUserID);
                                                        }

                                                        if(position == 1)
                                                        {

                                                            cancelFriendsRequest(listUserID);
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(requestType.equals("sent"))
                            {

                                Button reqSendBtn = viewHolder.mView.findViewById(R.id.request_accept_btn);
                                reqSendBtn.setText("Req Sent");

                                viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                usersReference.child(listUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        final String username = dataSnapshot.child("userName").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("userThumbImage").getValue().toString();
                                        final String userStatus = dataSnapshot.child("userStatus").getValue().toString();


                                        viewHolder.setUserName(username);
                                        viewHolder.setUserThumbImage(thumbImage, getContext());
                                        viewHolder.setUserStatus(userStatus);


                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options[]= new CharSequence[]
                                                        {
                                                                "Cancel Friend Request"


                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Request Sent");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position)
                                                    {


                                                        if(position == 0)
                                                        {

                                                            cancelFriendsRequest(listUserID);
                                                        }
                                                    }
                                                });
                                                builder.show();

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        };

        requestsList.setAdapter(firebaseRecyclerAdapter);
    }

    private void acceptFriendsRequest(final String listUserID) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
        final String saveCurrentDate = currentDate.format(calendar.getTime());

        friendsReference.child(currentUserID).child(listUserID).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        friendsReference.child(listUserID).child(currentUserID).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        friendsReqReference.child(currentUserID).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    friendsReqReference.child(listUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){

                                                                Toast.makeText(getContext(), "Friends Request Accepted successfully", Toast.LENGTH_SHORT).show();
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


    private void cancelFriendsRequest(final String listUserID) {

        friendsReqReference.child(currentUserID).child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsReqReference.child(listUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), "Friends Request Cancel successfully", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

            }
        });
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String username) {
            TextView name = (TextView) mView.findViewById(R.id.request_profile_name);
            name.setText(username);
        }

        public void setUserThumbImage(final String thumbImage, final Context ctx) {
            final CircleImageView thumb_Image= (CircleImageView) mView.findViewById(R.id.request_profile_image);
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

        public void setUserStatus(String userStatus) {
            TextView status = (TextView)mView.findViewById(R.id.request_profile_status);
            status.setText(userStatus);
        }
    }
}
