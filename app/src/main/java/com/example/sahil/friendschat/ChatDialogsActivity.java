package com.example.sahil.friendschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sahil.friendschat.Adapter.ChatDialogAdapter;
import com.example.sahil.friendschat.Common.Common;
import com.example.sahil.friendschat.Holder.QBChatDialogHolder;
import com.example.sahil.friendschat.Holder.QBUnreadMessageHolder;
import com.example.sahil.friendschat.Holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatDialogsActivity extends AppCompatActivity implements QBSystemMessageListener ,QBChatDialogMessageListener{

    FloatingActionButton floatingActionButton;
    ListView listchatdialog;



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();


        switch (item.getItemId())
        {
            case R.id.context_delete_dialog:
                deleteDialog(info.position);
                break;
        }

        return true;
    }

    private void deleteDialog(int index) {

       final QBChatDialog chatDialog = (QBChatDialog)listchatdialog.getAdapter().getItem(index);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(),false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {

                        QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                        ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                        listchatdialog.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.chat_dialog_menu_user:
                showUserProfile();
                break;
            default:
                break;
        }
        return true;
    }

    private void showUserProfile() {

        Intent intent = new Intent(ChatDialogsActivity.this,UserProfile.class);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_chat_dialogs);

        //Add Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.chat_dialog_toolbar);
        toolbar.setTitle("Pro Chat");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);


        createSessionForChat();

        listchatdialog = (ListView)findViewById(R.id.listchatdialoge);

        registerForContextMenu(listchatdialog);

        listchatdialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog)listchatdialog.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialogsActivity.this,ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA,qbChatDialog);
                startActivity(intent);
            }
        });
        
        loadChatDialog();

        floatingActionButton = (FloatingActionButton)findViewById(R.id.chatdialoge_adduser);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDialogsActivity.this,ListUsersActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadChatDialog() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                //put all dialog to cache

                QBChatDialogHolder.getInstance().purDialogs(qbChatDialogs);

              //Unread String
                Set<String> setIds = new HashSet<String>();
                for(QBChatDialog chatDialog:qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                //Get Message Unread

                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {

                                //Save to Cache

                                QBUnreadMessageHolder.getInstance().setBundle(bundle);

                                //Refresh List Dialog

                                ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                                listchatdialog.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });
            }

            @Override
            public void onError(QBResponseException e) {
              Log.e("ERROR",e.getMessage());
            }
        });
    }

    private void createSessionForChat() {

        final ProgressDialog progressdialog = new ProgressDialog(ChatDialogsActivity.this);
        progressdialog.setMessage("Please Wait...");
        progressdialog.setCanceledOnTouchOutside(false);
        progressdialog.show();

        String user,password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        //Load All user and save to cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });


        final QBUser qbuser = new QBUser(user,password);
        QBAuth.createSession(qbuser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbuser.setId(qbSession.getUserId());
                try
                {
                    qbuser.setPassword(BaseService.getBaseService().getToken());
                }catch (BaseServiceException e){
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbuser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        progressdialog.dismiss();

                        //adding listener for system message recieve

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogsActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogsActivity.this);

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }


    @Override
    public void processMessage(QBChatMessage qbChatMessage) {

        //put dialog to cache
        //because we send system message with content is dialogid
        //so we can get dialog by dailogid

        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //put to cache

                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(),adapterSource);
                listchatdialog.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {

        Log.e("ERROR",""+e.getMessage());

    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialog();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
