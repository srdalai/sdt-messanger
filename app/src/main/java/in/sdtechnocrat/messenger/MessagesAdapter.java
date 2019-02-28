package in.sdtechnocrat.messenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private Context mContext;
    //private ArrayList<MessageData> messageList;
    ArrayList<QueryDocumentSnapshot> messageList;
    SharedPreferences sharedPreferences;

    public MessagesAdapter(Context mContext, ArrayList<QueryDocumentSnapshot> messageList) {
        this.mContext = mContext;
        this.messageList = messageList;
        sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messages_adapter, parent, false);
        return new MessagesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        final QueryDocumentSnapshot snapshot = messageList.get(holder.getAdapterPosition());
        holder.textViewID.setText(snapshot.getId());

        String sender = (String) snapshot.get("sender");
        final String receiver = (String) snapshot.get("receiver");

        if (sharedPreferences.getString("user_id", "").equals(sender)) {
            holder.textViewName.setText(receiver);
        } else {
            holder.textViewName.setText(sender);
        }

        Glide.with(mContext).load(R.drawable.user_icon)
                .circleCrop()
                .into(holder.imageViewProfile);

        holder.linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessagingActivity.class);
                intent.putExtra("receiverID", receiver);
                intent.putExtra("convID", snapshot.getId());
                mContext.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        TextView textViewID, textViewName, textViewLastMsg;
        ImageView imageViewProfile;
        LinearLayout linear;
        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewID = itemView.findViewById(R.id.txtViewID);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLastMsg = itemView.findViewById(R.id.textViewLastMsg);
            linear = itemView.findViewById(R.id.linear);
        }
    }
}
