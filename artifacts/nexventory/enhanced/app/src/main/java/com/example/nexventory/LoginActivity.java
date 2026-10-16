package com.example.nexventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private boolean signingIn;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isSignedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        loginButton.setOnClickListener(v -> loginUser());
        createAccountButton.setOnClickListener(v -> createAccount());
    }

    private void loginUser() {
        if (signingIn) return;
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (!isInputValid(username,password)) return;
        signingIn=true;
        DatabaseTasks.run(this, () -> databaseHelper.validateUser(username,password), accepted -> {
            signingIn=false;
            if (accepted) openHomeScreen();
            else Toast.makeText(this,"Invalid username or password",Toast.LENGTH_LONG).show();
        }, message -> {signingIn=false;Toast.makeText(this,message,Toast.LENGTH_LONG).show();});
    }

    private void createAccount() {
        if (signingIn) return;
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (!isInputValid(username,password)) return;
        signingIn=true;
        DatabaseTasks.run(this, () -> databaseHelper.createUser(username,password), accepted -> {
            signingIn=false;
            if (accepted) openHomeScreen();
            else Toast.makeText(this,"Username already exists or account could not be saved.",Toast.LENGTH_LONG).show();
        }, message -> {signingIn=false;Toast.makeText(this,message,Toast.LENGTH_LONG).show();});
    }

    private boolean isInputValid(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openHomeScreen() {
        sessionManager.signIn();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
