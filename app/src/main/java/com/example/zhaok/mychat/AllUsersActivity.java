package com.example.zhaok.mychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView allUserList;
    private DatabaseReference allUsersReference;

    private EditText searchInput;
    private ImageButton searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);


        mToolBar = (Toolbar)findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        allUserList = (RecyclerView)findViewById(R.id.all_user_list);
        allUserList.setHasFixedSize(true);
        allUserList.setLayoutManager(new LinearLayoutManager(this));

        searchBtn = (ImageButton) findViewById(R.id.search_btn);
        searchInput = (EditText)findViewById(R.id.search_input);

        allUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        allUsersReference.keepSynced(true);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String searchUsername = searchInput.getText().toString();

                if(TextUtils.isEmpty(searchUsername))
                {
                    Toast.makeText(AllUsersActivity.this, "Please search a user.", Toast.LENGTH_SHORT).show();
                }
                searchUsers(searchUsername);
            }
        });
    }

   private void searchUsers(String searchUsername)
    {


        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        Query searchPeople = allUsersReference.orderByChild("userName").startAt(searchUsername)
                .endAt(searchUsername + "\uf8ff");

        FirebaseRecyclerAdapter<AllUsers,AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (AllUsers.class,
                 R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        searchPeople
                        ) {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
                    viewHolder.setUserName(model.getUserName());
                    viewHolder.setUserStatus(model.getUserStatus());
                    viewHolder.setUserThumbImage(getApplicationContext(),model.getUserThumbImage());

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String visitUserID = getRef(position).getKey();
                            Intent profileIntent = new Intent(getApplicationContext(),ProfileActivity.class);
                            profileIntent.putExtra("visitUserID",visitUserID);
                            startActivity(profileIntent);
                        }
                    });
            }
        };

        allUserList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class AllUsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUserName(String userName){
            TextView name = (TextView) mView.findViewById(R.id.all_users_username);
            name.setText(userName);

        }
        public void setUserStatus(String userStatus){
            TextView status = (TextView) mView.findViewById(R.id.all_user_user_status);
            status.setText(userStatus);

        }

        public void setUserThumbImage(final Context ctx, final String userThumbImage){
            final CircleImageView thumbImage= (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(userThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumbImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(userThumbImage).placeholder(R.drawable.default_profile).into(thumbImage);

                        }
                    });


        }


    }
}
