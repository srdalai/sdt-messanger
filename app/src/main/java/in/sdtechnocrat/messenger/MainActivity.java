package in.sdtechnocrat.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    RecyclerView recyclerView;
    ArrayList<QueryDocumentSnapshot> messageList;
    MessagesAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    int count = 0;
    FirebaseFirestore dataBase;
    DocumentReference docRef;
    String user_id;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        messageList = new ArrayList<>();
        adapter = new MessagesAdapter(this, messageList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        createNotificationChannel();


        FirebaseMessaging.getInstance().subscribeToTopic("test");
        sharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", "");
        dataBase = FirebaseFirestore.getInstance();

        docRef = dataBase.collection("endUserConvs").document("user001_user002");
        /*docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });*/
        getUserAsSender();
        //subscribeToData();

    }

    private void subscribeToData() {
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
                            createNotification(msg);
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

    private void getUserAsSender() {

        dataBase.collection("endUserConvs").whereEqualTo("sender", user_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                messageList.add(document);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        getUserAsReceiver();
                    }
                });
    }

    private void getUserAsReceiver() {
        dataBase.collection("endUserConvs").whereEqualTo("receiver", user_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                messageList.add(document);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        Log.d("Size", messageList.size()+"");
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    public void createNotification(String message) {
        count++;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("RestrictedApi") Person user = new Person.Builder().setIcon(IconCompat.createFromIcon(Icon.createWithResource(getApplicationContext(), R.drawable.user_icon))).setName(userName).build();
        }*/
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "10")
                .setStyle(new NotificationCompat.MessagingStyle("Me").setConversationTitle("Conversation " + count)
                        .addMessage(message, System.currentTimeMillis()/1000, "From Friend " + count) // Pass in null for user.
                        )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(message)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = notificationBuilder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(count, notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Other";
            String description = "Other";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("10", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void formatSnapshotAndAddToDataList(QueryDocumentSnapshot document) {


    }
}
