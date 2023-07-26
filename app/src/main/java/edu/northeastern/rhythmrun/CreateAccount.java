package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccount extends AppCompatActivity {

	private TextInputEditText inputEmail, inputFirstName, inputPassword;
	private TextInputLayout emailInputLayout, firstNameInputLayout, passwordInputLayout;
	private Button createAccountBtn, logInBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);

		// input fields
		inputEmail = findViewById(R.id.inputEmail);
		inputFirstName = findViewById(R.id.inputFirstName);
		inputPassword = findViewById(R.id.inputPassword);
		// TODO add differnt views such as username, height, weight, confirm password etc.
		// input layouts
		emailInputLayout = findViewById(R.id.emailInputLayout);
		firstNameInputLayout = findViewById(R.id.firstNameInputLayout);
		passwordInputLayout = findViewById(R.id.passwordInputLayout);

		createAccountBtn = findViewById(R.id.createAccountBtn);
		logInBtn = findViewById(R.id.logInBtn);

		createAccountBtn.setOnClickListener(v -> {

			checkUserInputs();

		});

	}

	private void checkUserInputs() {

		String email = inputEmail.getText().toString();
		String firstName = inputFirstName.getText().toString();
		String password = inputPassword.getText().toString();

		if (TextUtils.isEmpty(email)) {
			// Handle empty email
			Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
			emailInputLayout.setError("Email can not be null");
			emailInputLayout.requestFocus();

		} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
			emailInputLayout.setError( " Valid email is Required");
			emailInputLayout.requestFocus();
		}

		else if (TextUtils.isEmpty(firstName)) {
			// Handle empty first name
			Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
			firstNameInputLayout.setError( "First Name is Required");
			firstNameInputLayout.requestFocus();
		}

		else if (TextUtils.isEmpty(password)) {
			// Handle empty password
			Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
			passwordInputLayout.setError("Password is Required");
			passwordInputLayout.requestFocus();
		}
		else {
			registerUser(firstName,email,password);
		}

	}

	private void registerUser(String firstName, String email, String password) {
		FirebaseAuth auth = FirebaseAuth.getInstance();
		// creates authorization instance in firebase
		auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {

					// current currentUser
					FirebaseUser currentUser = auth.getCurrentUser();

					// Create currentUser in Realtime DB
					CreateUserInDB createUserInDB = new CreateUserInDB(firstName,email,password);

					DatabaseReference allUsersRegistered = FirebaseDatabase.getInstance().getReference("Users");

					// creates user in DB
					allUsersRegistered.child(currentUser.getUid()).setValue(createUserInDB).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {

							if(task.isSuccessful()) {

								// For debugging delete after testing
								Toast.makeText(CreateAccount.this, "User created check email ", Toast.LENGTH_LONG).show();

								// Send verify email
								currentUser.sendEmailVerification();

								// Prevent currentUser from clicking back after successful registration
								Intent intent = new Intent(CreateAccount.this, Home.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
										| Intent.FLAG_ACTIVITY_NEW_TASK);

								// start activity
								startActivity(intent);

								// close activity
								finish();

							} else {
								Toast.makeText(CreateAccount.this, "Failed to create user ", Toast.LENGTH_LONG).show();
							}


						}
					});


				} else {
					try {
						throw task.getException();
					} catch (FirebaseAuthInvalidCredentialsException e){
						emailInputLayout.setError("Email is invalid or already in use. Try Again!");
						emailInputLayout.requestFocus();
					} catch (Exception e) {
						Log.e("ERROR", e.getMessage());
					}
				}
			}
		});
	}


}