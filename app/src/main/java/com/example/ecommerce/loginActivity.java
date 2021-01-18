package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Admin.AdminCategoryActivity;
import com.example.ecommerce.Model.Users;
import com.example.ecommerce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class loginActivity extends AppCompatActivity
{
    private EditText inputPhoneNumber, inputPassword;
    private Button loginButton;
    private ProgressDialog loadingBar;
    private String parentDbName="Users";

    private CheckBox chkBoxRememberMe;
    private TextView AdminLink,NotAdminLink, forgetPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginButton=findViewById(R.id.login_btn);

        inputPhoneNumber=findViewById(R.id.login_phone_number_input);
        inputPassword=findViewById(R.id.login_password_input);
        AdminLink=findViewById(R.id.admin_panel_link);
        NotAdminLink=findViewById(R.id.not_admin_panel_link);
        forgetPasswordLink=findViewById(R.id.forget_password_link);

        chkBoxRememberMe=findViewById(R.id.remember_me_chkb);
        Paper.init(this);

        loadingBar=new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                loginUser();

            }
        });

        forgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent=new Intent(loginActivity.this,ResetPasswordActivity.class);
                intent.putExtra("check", "login");
                startActivity(intent);

            }
        });


        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                loginButton.setText("Login Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDbName="Admins";

            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                loginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDbName="Users";

            }
        });

    }

    private void loginUser()
    {
        String phoneNumber=inputPhoneNumber.getText().toString();
        String password=inputPassword.getText().toString();

        if (TextUtils.isEmpty(phoneNumber))
           {
               Toast.makeText(this, "Please write your phone number...", Toast.LENGTH_SHORT).show();
           }
        else if (TextUtils.isEmpty(password))
           {
               Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
           }
        else
        {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            AllowAccessToAccount(phoneNumber, password);
        }


    }

    private void AllowAccessToAccount(String phoneNumber, String password)
    {

        if (chkBoxRememberMe.isChecked())
        {
            Paper.book().write(Prevalent.UserPhoneKey, phoneNumber);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }

        final DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.child(parentDbName).child(phoneNumber).exists())
                {
                    Users userData=snapshot.child(parentDbName).child(phoneNumber).getValue(Users.class);

                    if (userData.getPhone().equals(phoneNumber))
                    {
                        if (userData.getPassword().equals(password))
                        {
                          if (parentDbName.equals("Admins"))
                          {
                              Toast.makeText(loginActivity.this, "Welcome Admin, you are Logged in successfully...", Toast.LENGTH_SHORT).show();
                              loadingBar.dismiss();
                              Intent intent=new Intent(loginActivity.this, AdminCategoryActivity.class);
                              startActivity(intent);
                          }
                          else if (parentDbName.equals("Users"))
                          {
                              Toast.makeText(loginActivity.this, "Logged in successfully...", Toast.LENGTH_SHORT).show();
                              loadingBar.dismiss();

                              Intent intent=new Intent(loginActivity.this,HomeActivity.class);
                              Prevalent.currentOnlineUser=userData;
                              startActivity(intent);
                          }

                        }
                        else 
                        {
                            loadingBar.dismiss();
                            Toast.makeText(loginActivity.this, "Password is incorrect...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(loginActivity.this, "User does not exist, please create new account...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    inputPhoneNumber.setText("");
                    inputPassword.setText("");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}