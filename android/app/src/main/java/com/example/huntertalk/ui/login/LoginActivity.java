package com.example.huntertalk.ui.login;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huntertalk.Home_page;
import com.example.huntertalk.R;
import com.example.huntertalk.RegistrationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private Button loginButton;
    private Button registrationButton;
    private Button resetpw;
    private Button sendem;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        resetpw = findViewById(R.id.resetpw);
        sendem = findViewById(R.id.sendem);
        sendem.setVisibility(View.GONE);
        resetpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.password).setVisibility(View.GONE);
                findViewById(R.id.login).setVisibility(View.GONE);
                findViewById(R.id.resetpw).setVisibility(View.GONE);
                findViewById(R.id.registerButton).setVisibility(View.GONE);
                findViewById(R.id.sendem).setVisibility(View.VISIBLE);
               setTitle("Password Reset");

            }
        });

        sendem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = usernameEditText.getText().toString().trim();
                if (emailAddress.equals("") || !emailAddress.contains("@") || !emailAddress.contains(".")){
                    Toast toast= Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 10, 10);
                    toast.show();
                    return;
                }
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });

                findViewById(R.id.password).setVisibility(View.VISIBLE);
                findViewById(R.id.login).setVisibility(View.VISIBLE);
                findViewById(R.id.resetpw).setVisibility(View.VISIBLE);
                findViewById(R.id.registerButton).setVisibility(View.VISIBLE);
                findViewById(R.id.sendem).setVisibility(View.GONE);
                setTitle("Sign In");

            }


        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameEditText.getText().toString().trim();
                if (email.equals("") || !email.contains("@") || !email.contains(".")){
                    Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = passwordEditText.getText().toString().trim();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                       Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                      moveToMainActivity();
                                } else {
                                    // If sign in fails, display a message to the user.
                                       Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

        registrationButton = findViewById(R.id.registerButton);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moveToRegistrationActivity();
            }
        });

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //currentUser = null;  //Uncomment this to get to not remember login.

        if (currentUser!=null){
            moveToMainActivity();
        }
    }


   /* private void updateUiWithUser(LoggedInUserView model) {
        //String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }*/

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void moveToMainActivity(){
        Intent intent = new Intent(LoginActivity.this, Home_page.class);
        startActivity(intent);
    }
    private void moveToRegistrationActivity(){
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }


}

