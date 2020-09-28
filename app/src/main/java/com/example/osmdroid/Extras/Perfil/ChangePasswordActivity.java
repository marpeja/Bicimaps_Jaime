package com.example.osmdroid.Extras.Perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.osmdroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText actualPwd;
    private EditText newPwd;
    private EditText newPwdCheck;
    private Button confirm;
    private AuthCredential credential = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        actualPwd = findViewById(R.id.actualPwd);
        newPwd = findViewById(R.id.newPwd);
        newPwdCheck = findViewById(R.id.newPwdCheck);
        confirm = findViewById(R.id.confirmPwd);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String actualPwdString = actualPwd.getText().toString();
                String newPwdString = newPwd.getText().toString();
                String newPwdCheckString = newPwdCheck.getText().toString();

                if(!actualPwdString.equals("") && !newPwdString.equals("") && !newPwdCheckString.equals("")){
                    if(newPwdString.equals(newPwdCheckString)){
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseUser user = auth.getCurrentUser();
                        if(user != null && user.getEmail() != null) {
                            if (user.getProviderData().get(1).getProviderId().equals("password")) {
                                credential = EmailAuthProvider
                                        .getCredential(user.getEmail(), actualPwdString);
                            }
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChangePasswordActivity.this,
                                                        "RE-AUTH SUCCED", Toast.LENGTH_SHORT).show();
                                                user.updatePassword(newPwdString)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(ChangePasswordActivity.this,
                                                                            "CONTRASEÑA CAMBIADA", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(ChangePasswordActivity.this,
                                                                            "ERROR, VUELVA A INTENTARLO", Toast.LENGTH_SHORT).show();

                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(ChangePasswordActivity.this,
                                                        "RE-AUTH FAILED", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(ChangePasswordActivity.this,
                                "LAS CONTRASEÑAS NO COINCIDEN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this,
                            "INTRODUCE CONTRASEÑAS VÁLIDAS", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
