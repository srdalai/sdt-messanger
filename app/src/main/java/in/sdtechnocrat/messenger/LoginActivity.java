package in.sdtechnocrat.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    EditText editText;
    Button buttonGo;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText = findViewById(R.id.editText);
        buttonGo = findViewById(R.id.buttonGo);

        sharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);

        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = editText.getText().toString();
                Log.d(getPackageName(), uid);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_id", uid);
                editor.putBoolean("isLoggedIn", true);
                editor.apply();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}
