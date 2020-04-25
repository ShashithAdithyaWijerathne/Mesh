package com.example.mesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mesh.Model.Users;
import com.example.mesh.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private EditText InputPhoneNumber,InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    private TextView AdminLink, NotAdminLink;

    private String parentDbName = "Users";

    private CheckBox RememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton =(Button) findViewById(R.id.login_btn);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        InputPhoneNumber =(EditText) findViewById(R.id.login_phone_number_input);

        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);

        RememberMeCheckBox = (CheckBox) findViewById(R.id.remember_me_checkBox);
        Paper.init(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginUser();
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//If user click I am Admin, Then👇
                LoginButton.setText("Admin Login");//Login button will change as Admin Login
                AdminLink.setVisibility(View.INVISIBLE);//Link will be disappear
                NotAdminLink.setVisibility(View.VISIBLE);//I am not admin link will be visible
                parentDbName = "Admins";

            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//If user click I am Admin by mistakenly, Then👇
                LoginButton.setText("Login");//Admin Login button will change as  Login
                AdminLink.setVisibility(View.VISIBLE);//Link will be visible
                NotAdminLink.setVisibility(View.INVISIBLE);//I am not admin link will be disappear
                parentDbName = "Users";

            }
        });
    }

    private void LoginUser() {
        String Phone = InputPhoneNumber.getText().toString();
        String Password = InputPassword.getText().toString();

        if (TextUtils.isEmpty(Phone))
        {
            Toast.makeText(this, "Please write your phone number...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Password))
        {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            AllowAccessToAccount(Phone, Password);
        }
    }

    private void AllowAccessToAccount(final String Phone, final String Password) {

        if (RememberMeCheckBox.isChecked()) { //Check is RememberMeBox is click or not
            Paper.book().write(Prevalent.UserPhoneKey, Phone);
            Paper.book().write(Prevalent.UserPasswordKey, Password);

        }

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDbName).child(Phone).exists()){

                    Users usersData =dataSnapshot.child(parentDbName).child(Phone).getValue(Users.class);

                    if(usersData.getPhone().equals(Phone)){//Validate phone number and password
                        if(usersData.getPassword().equals(Password)){

                            if (parentDbName.equals("Admins")) {

                                Toast.makeText(LoginActivity.this, "Welcome Admin,You are logged Successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, AdminAddNewProductActivity.class);
                                startActivity(intent);
                            } else if (parentDbName.equals("Users")) {

                                Toast.makeText(LoginActivity.this, "Logged Successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Password is Incorrect!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Phone number is Incorrect!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }else{
                    Toast.makeText(LoginActivity.this, "Account with this"+Phone+"number do not exists!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
