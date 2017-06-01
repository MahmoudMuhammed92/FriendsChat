package com.example.sahil.friendschat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpActivity extends AppCompatActivity {

    Button buttonsignup,buttoncancel;
    EditText sedituser,seditpassword,editfullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        registerSession();

        buttonsignup = (Button)findViewById(R.id.signup_btn_signup);
        buttoncancel = (Button)findViewById(R.id.signup_btn_cancel);
        sedituser= (EditText)findViewById(R.id.signup_edit_login);
        seditpassword = (EditText)findViewById(R.id.signup_edit_password);
        editfullname  = (EditText)findViewById(R.id.signup_edit_fullname);


        buttoncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        buttonsignup.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String suser = sedituser.getText().toString();
                String spassword = seditpassword.getText().toString();

                final ProgressDialog progressDialog =  new ProgressDialog(SignUpActivity.this);
                progressDialog.setTitle("Please Wait...");
                progressDialog.setMessage("Sign Up");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                QBUser qbUser = new QBUser(suser,spassword);

                qbUser.setFullName(editfullname.getText().toString());

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {

                        Toast.makeText(getBaseContext(),"Signup Successfully",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
            }
        });


    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());

            }
        });
    }
}
