package in.sdtechnocrat.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class MessagingActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    String convID = "";

    FirebaseFirestore dataBase;
    DocumentReference docRef;
    String TAG = MessagingActivity.class.getSimpleName();
    int count = 0;

    EditText textMessage;
    FloatingActionButton fabSend;
    String receiverID = "";

    String user_id, name, userName, profileUrl;
    SharedPreferences sharedPreferences;
    String finalMessage = "";
    int messageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        recyclerView = findViewById(R.id.recyclerView);
        textMessage = findViewById(R.id.textMessage);
        fabSend = findViewById(R.id.fabSend);

        if (getIntent() != null) {
            convID = getIntent().getStringExtra("convID");
            receiverID = getIntent().getStringExtra("receiverID");
        }

        if (!receiverID.equals("")) {
            String str = receiverID.substring(6);
            Log.d("Str", str);
            count = Integer.parseInt(str);
        }
        sharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);

        user_id = sharedPreferences.getString("user_id", "");

        dataBase = FirebaseFirestore.getInstance();

        docRef = dataBase.collection("endUserConvs").document(convID);

        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = textMessage.getText().toString();
                prepareMessage(msg);
                textMessage.setText("");
            }
        });

        getUserData(receiverID);
    }

    private void getUserData(String user_id) {
        dataBase.collection("userData").document(user_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Document found in the offline cache
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "Cached document data: " + document.getData());
                            name = (String) document.getData().get("name");
                            userName = (String) document.getData().get("userName");
                        } else {
                            Log.d(TAG, "Cached get failed: ", task.getException());
                        }
                        getDataFromServer();
                    }
                });
    }

    private void getDataFromServer() {
        dataBase.collection("endUserConvs").document(convID).collection("textMessages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                //TODO to add message
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        subscribeToDatabase();
                    }
                });
    }

    private void subscribeToDatabase() {
        docRef.collection("textMessages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New city: " + dc.getDocument().getData());
                            String msg = (String) dc.getDocument().getData().get("message");
                            if (finalMessage.equals("")) {
                                finalMessage = msg;
                            } else {
                                finalMessage = finalMessage + "\n" + msg;
                            }
                            messageCount++;
                            createNotification(finalMessage);
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                            break;
                    }
                }
            }
        });
    }

    private void prepareMessage(String msg) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        MessageData messageData = new MessageData();
        messageData.setMessage(msg);
        messageData.setSenderID(user_id);
        messageData.setReceiverID(receiverID);
        messageData.setTime(timeStamp);

        dataBase.collection("endUserConvs").document(convID).collection("textMessages").document(timeStamp).set(messageData);
    }

    public void createNotification(String message) {
        //count++;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("RestrictedApi") Person user = new Person.Builder().setIcon(IconCompat.createFromIcon(Icon.createWithResource(getApplicationContext(), R.drawable.user_icon))).setName(userName).build();
        }*/

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "10")
                .setStyle(new NotificationCompat.MessagingStyle("Me").setConversationTitle(messageCount + " Unread Messages")
                        .addMessage(message, System.currentTimeMillis()/1000, name) // Pass in null for user.
                )
                .setSmallIcon(R.drawable.ic_send_white_24dp)
                .setContentTitle(name)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = notificationBuilder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(count, notification);
    }
}
