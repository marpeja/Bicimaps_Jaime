package com.example.osmdroid.Extras.Perfil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.osmdroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ChangeNameActivity extends AppCompatActivity {

    private TextView actualName;
    private TextView newName;
    private Button confirm;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_name);

        actualName = findViewById(R.id.actualName);
        newName = findViewById(R.id.newName);
        confirm = findViewById(R.id.confirm);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        actualName.setText(auth.getCurrentUser().getDisplayName());

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newName.getText().toString();
                if(!text.equals("")){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(text)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChangeNameActivity.this, "NOMBRE ACTUALIZADO", Toast.LENGTH_SHORT).show();
                                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                            /*for (UserInfo profile : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                                                // Id of the provider (ex: google.com)
                                                String providerId = profile.getProviderId();
                                                Log.i("PROVIIIIIDERS", providerId);
                                            }*/

                                            if(FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getProviderId().equals("password")){
                                                Log.i("PROVIIIIIDERS", "PWD");
                                            }
                                            if(FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getProviderId().equals("google.com")){
                                                Log.i("PROVIIIIIDERS", "GOOGLE");
                                            }
                                        }
                                        finish();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(ChangeNameActivity.this, "INTRODUCE UN NOMBRE VÁLIDO", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
