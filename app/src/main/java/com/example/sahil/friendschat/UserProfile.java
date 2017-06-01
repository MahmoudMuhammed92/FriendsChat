package com.example.sahil.friendschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sahil.friendschat.Common.Common;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfile extends AppCompatActivity {

    EditText edtPassword ,edtOldPassword,edtFullName,edtEmail,edtPhone;
    Button btnUpdate;
    Button btnCancel;
    QBChatDialog qbChatDialog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.user_update_log_out:
                logOut();
                break;
            default:
                break;
        }
        return true;
    }

    private void logOut() {

         QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
             @Override
             public void onSuccess(Void aVoid, Bundle bundle) {
                 QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                     @Override
                     public void onSuccess(Void aVoid, Bundle bundle) {
                         Toast.makeText(UserProfile.this,"Your are Logout",Toast.LENGTH_LONG).show();
                         Intent intent = new Intent(UserProfile.this,MainActivity.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         startActivity(intent);
                         finish();
                     }

                     @Override
                     public void onError(QBResponseException e) {

                     }
                 });
             }

             @Override
             public void onError(QBResponseException e) {

             }
         });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar)findViewById(R.id.user_update_toolbar);
        toolbar.setTitle("Pro Chat");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);


        initView();

        loadUserProfile();


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edtPassword.getText().toString();
                String oldPassword = edtOldPassword.getText().toString();
                String email = edtEmail.getText().toString();
                String phone = edtPhone.getText().toString();
                String fullName = edtFullName.getText().toString();


                QBUser usser = new QBUser();
                usser.setId(QBChatService.getInstance().getUser().getId());

                if(!Common.isNullOrEmptyString(oldPassword))
                    usser.setOldPassword(oldPassword);
                if(!Common.isNullOrEmptyString(password))
                    usser.setPassword(password);
                if(!Common.isNullOrEmptyString(fullName))
                    usser.setFullName(fullName);
                if(!Common.isNullOrEmptyString(phone))
                    usser.setPhone(phone);
                if(!Common.isNullOrEmptyString(email))
                    usser.setEmail(email);

                final ProgressDialog progressDialog =  new ProgressDialog(UserProfile.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.show();


                QBUsers.updateUser(usser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfile.this,"User : "+qbUser.getLogin()+"  Updated",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfile.this,"Error !!!"+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }

    private void loadUserProfile() {

        QBUser currentUser = QBChatService.getInstance().getUser();
        String fullName = currentUser.getFullName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhone();

        edtEmail.setText(email);
        edtFullName.setText(fullName);
        edtPhone.setText(phone);
    }

    private void initView() {

        btnCancel = (Button)findViewById(R.id.update_user_btn_cancel);
        btnUpdate= (Button)findViewById(R.id.update_user_btn_update);

        edtEmail = (EditText)findViewById(R.id.update_edt_email);
        edtFullName = (EditText)findViewById(R.id.update_edt_full_name);
        edtPassword = (EditText)findViewById(R.id.update_edt_new_password);
        edtOldPassword = (EditText)findViewById(R.id.update_edt_old_password);
        edtPhone = (EditText)findViewById(R.id.update_edt_pnone);

    }


}
