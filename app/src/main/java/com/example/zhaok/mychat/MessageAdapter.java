package com.example.zhaok.mychat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by zhaok on 4/15/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;

    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user,parent,false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position)
    {

        String messageSenderID = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String username = dataSnapshot.child("userName").getValue().toString();
                String thumbImage = dataSnapshot.child("userThumbImage").getValue().toString();

                Picasso.with(holder.userProfileImage.getContext()).load(thumbImage).placeholder(R.drawable.default_profile)
                        .into(holder.userProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text"))
        {
            holder.messageImage.setVisibility(View.INVISIBLE);
            if(fromUserID.equals(messageSenderID))
            {

                holder.messageText.setBackgroundResource(R.drawable.messaage_text_background_two);


                holder.messageText.setGravity(Gravity.RIGHT);

            }
            else
            {
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.BLACK);
                holder.messageText.setGravity(Gravity.LEFT);
            }
            holder.messageText.setText(messages.getMessage());

        }
        else
        {
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.messageText.setPadding(0,0,0,0);
            Picasso.with(holder.userProfileImage.getContext()).load(messages.getMessage()).placeholder(R.drawable.default_profile)
                    .into(holder.messageImage);
        }


    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView messageText;
        public CircleImageView userProfileImage;
        public ImageView messageImage;

        public MessageViewHolder(View view)
        {
            super(view);

            messageText = (TextView) view.findViewById(R.id.messages_text);
            userProfileImage =(CircleImageView)view.findViewById(R.id.messages_profile_image);
            messageImage =(ImageView)view.findViewById(R.id.messages_image_view);
        }
    }
}
