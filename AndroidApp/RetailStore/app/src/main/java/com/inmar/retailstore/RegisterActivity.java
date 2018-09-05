package com.inmar.retailstore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inmar.retailstore.common.Constants;
import com.inmar.retailstore.common.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Register screen.
 */
public class RegisterActivity extends AppCompatActivity  {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    // Keep track of the registration task to ensure we can cancel it if requested.
    private UserRegistrationTask mRegistrationTask = null;

    // UI references.
    private EditText mName, mEmailView, mPasswordView, mConfirmPasswordView;
    private View mProgressView, mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // Set up the register form.
        mName = findViewById(R.id.name);
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mConfirmPasswordView = findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if (mRegistrationTask != null) {
            return;
        }

        // Reset errors.
        mName.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mName.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for name.
        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        } else if (!isPasswordValid(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check if both passwords are same or not
        if (!TextUtils.isEmpty(password) && !password.equals(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_confirm_pwd_different));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mRegistrationTask = new UserRegistrationTask(name, email, password);
            mRegistrationTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= Constants.PASSWORD_MIN_LENGTH;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegistrationTask extends AsyncTask<Void, Void, Integer> {

        private final String mName;
        private final String mEmail;
        private final String mPassword;

        UserRegistrationTask(String name, String email, String password) {
            mName = name;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            String url_login = Constants.BASE_URL + "/register";

            String paramString = getParamString(mName, mEmail, mPassword);

            String responseData = Util.postServerData(url_login, paramString);

            return parseRegisterResponse(responseData);

        }

        @Override
        protected void onPostExecute(final Integer result) {
            mRegistrationTask = null;
            showProgress(false);


            if (result == Constants.RESULT_CODE_SUCCESS) {
                Toast.makeText(RegisterActivity.this, R.string.user_registered,
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mRegistrationTask = null;
            showProgress(false);
        }
    }

    private String getParamString(String name, String email, String password) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(Constants.JSON_KEY_NAME, name);
        paramMap.put(Constants.JSON_KEY_EMAIL, email);
        paramMap.put(Constants.JSON_KEY_PASSWORD, password);
        return  Util.getQueryStringForParameters(paramMap);
    }

    /**
     * Parse JSON response and check for the result codes.
     * @param responseData
     * @return int
     */
    private int parseRegisterResponse(String responseData) {
        Log.i(TAG, "parseLoginData");

        int result = Constants.RESULT_CODE_FAIL;

        if(responseData == null || responseData.isEmpty()) {
            Log.e(TAG, "Invalid response");
            return result;
        }

        try {
            JSONObject jObj = new JSONObject(responseData);
            if (jObj == null || jObj.length() == 0) {
                return result;
            }

            String resultCode = jObj.getString(Constants.JSON_KEY_RESULT);
            if (resultCode != null && !resultCode.isEmpty()) {
                Log.i(TAG, "ResultCode:" + resultCode);
                result = Integer.parseInt(resultCode);
            }
        } catch (JSONException e) {
            Log.e("InsertSku", "unexpected JSON exception", e);
        }

        return result;
    }

}

