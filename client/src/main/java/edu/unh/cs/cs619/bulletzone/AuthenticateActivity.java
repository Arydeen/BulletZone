package edu.unh.cs.cs619.bulletzone;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import edu.unh.cs.cs619.bulletzone.util.ResultWrapper;

@EActivity(R.layout.activity_authenticate)
public class AuthenticateActivity extends AppCompatActivity {
    @ViewById
    EditText username_editText;

    @ViewById
    EditText password_editText;

    @ViewById
    TextView status_message;

    @Bean
    AuthenticationController controller;

    long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Since we are using the @EActivity annotation, anything done past this point will
        //be overridden by the work AndroidAnnotations does. If you need to do more setup,
        //add to the methods under @AfterViews (for view items) or @AfterInject (for Bean items) below
    }

    @AfterViews
    protected void afterViewInjection() {
        //Put any view-setup code here (that you might normally put in onCreate)
    }

    @AfterInject
    void afterInject() {
        //Put any Bean-related setup code here (the you might normally put in onCreate)
    }

    /**
     * Registers a new user and logs them in
     */
    @Click(R.id.register_button)
    @Background
    protected void onButtonRegister() {
        String username = username_editText.getText().toString();
        String password = password_editText.getText().toString();

        ResultWrapper<Long> result = controller.register(username, password);

        if (result.isSuccess()) {
            setStatus("Registration successful.");
            // Optionally, you can automatically log in the user here
        } else {
            setStatus("Registration failed: " + result.getMessage());
        }
    }

    /**
     * Logs in an existing user
     */
    @Click(R.id.login_button)
    @Background
    protected void onButtonLogin() {
        String username = username_editText.getText().toString();
        String password = password_editText.getText().toString();

        try {
            ResultWrapper<Long> result = controller.login(username, password);

            if (result.isSuccess()) {
                Long userId = result.getResult();
                setStatus("Login successful. User ID: " + userId);
                onLoginSuccess(userId);
            } else {
                setStatus("Login failed: " + result.getMessage());
            }
        } catch (Exception e) {
            setStatus("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Log the error
        }
    }

    @UiThread
    public void onLoginSuccess(Long userId) {
        Log.d("AuthenticateActivity", "onLoginSuccess called with userId: " + userId);

        // Start the main game activity
        Intent intent = new Intent(this, MenuActivity_.class);
        intent.putExtra("USER_ID", userId);
        Log.d("AuthenticateActivity", "Starting ClientActivity_");
        startActivity(intent);
        Log.d("AuthenticateActivity", "ClientActivity_ started");
        finish(); // Close the login activity
    }

    @UiThread
    protected void setStatus(String message) {
        status_message.setText(message);
        Log.e("AuthenticateActivity", message);
    }
}