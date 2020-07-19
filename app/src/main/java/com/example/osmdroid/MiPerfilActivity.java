package com.example.osmdroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MiPerfilActivity extends AppCompatActivity {

    private TextView changeName;
    private TextView changePwd;
    private TextView forgotPwd;
    private TextView exitAccount;
    private TextView deleteAccount;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private final int RC_SIGN_IN = 44;
    private final int ERROR_RESULT = 77;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_perfil);

        changeName = findViewById(R.id.change_name);
        changePwd = findViewById(R.id.change_pwd);
        forgotPwd = findViewById(R.id.forgot_pwd);

        exitAccount = findViewById(R.id.exit_account);
        deleteAccount = findViewById(R.id.delete_account);


        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MiPerfilActivity.this, ChangeNameActivity.class);
                startActivity(intent);
            }
        });

        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                if (user.getProviderData().get(1).getProviderId().equals("google.com")){
                    Toast.makeText(MiPerfilActivity.this, "NO SE PUEDE CAMBIAR LA CONTRASEÑA DE GOOGLE AQUÍ", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MiPerfilActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                }

            }
        });

        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = auth.getCurrentUser().getEmail();

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("NUEVA_CONTRASEÑA", "Email sent.");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MiPerfilActivity.this, R.style.Theme_AppCompat_Dialog_Alert)
                                            .setTitle("Correo enviado")
                                            .setMessage("Revise su bandeja de entrada para obtener la nueva contraseña")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });

                                    AlertDialog pwd = builder.show();
                                    pwd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            pwd.dismiss();
                                        }
                                    });
                                } else {
                                    Log.d("NUEVA_CONTRASEÑA", "Email failed");
                                }
                            }
                        });
            }
        });


        exitAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {AuthUI.getInstance()
                    .signOut(MiPerfilActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                            showSignInOptions();
                        }
                    });

            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MiPerfilActivity.this, "Usuario Eliminado", Toast.LENGTH_SHORT).show();
                                    showSignInOptions();
                                }
                            }
                        });
            }
        });


    }

    public void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme_NoActionBar_Background)
                        .setLogo(R.drawable.ic_bicimap)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Toast.makeText(MiPerfilActivity.this, "SESIÓN INICIADA", Toast.LENGTH_SHORT).show();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                setResult(ERROR_RESULT);
                finish();
            }
        }
    }
}
