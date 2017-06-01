package com.example.sahil.friendschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID = "55703";
    static final String AUTH_KEY = "JfAUqpzYgddqdr2";
    static final String AUTH_SECRET = "YNKKQjvdBwtkAFK";
    static final String ACCOUNT_KEY = "YB5rhTsu3LxxK8Fpn7ne";


  //  static final int REQUEST_CODE = 1000;

    Button buttonlogin,buttonsignup;
    EditText edituser,editpassword;
    TextView textsignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     // requestRunTimePermission();

        initializeFrameWork();

        buttonlogin = (Button)findViewById(R.id.main_btn_login);
        //buttonsignup = (Button)findViewById(R.id.main_btn_signup);
        edituser = (EditText) findViewById(R.id.main_edit_login);
        editpassword = (EditText) findViewById(R.id.main_edit_password);
        textsignup = (TextView) findViewById(R.id.main_edt_signup);

        textsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));
            }
        });


        buttonlogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String user = edituser.getText().toString();
                final String password  = editpassword.getText().toString();

                final ProgressDialog progressDialog =  new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Please Wait...");
                progressDialog.setMessage("Signing");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                QBUser qbUser = new QBUser(user,password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {

                        Toast.makeText(getBaseContext(),"Loging Successfully",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();

                        Intent intent = new Intent(MainActivity.this,ChatDialogsActivity.class);
                        intent.putExtra("user",user);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();//cloase the login activity after logged
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


    /*
     //dialog avatar

    private void requestRunTimePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CODE);

            }
        }


    }

*/
    /*

    //dialog_Avater    //override method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case REQUEST_CODE:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getBaseContext(),"Permission Granted",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getBaseContext(),"Permission Denied",Toast.LENGTH_LONG).show();
            }
            break;
        }
    }
*/

    private void initializeFrameWork() {

        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
