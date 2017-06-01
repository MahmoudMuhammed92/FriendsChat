package com.example.sahil.friendschat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sahil.friendschat.Adapter.ListUserAdapter;
import com.example.sahil.friendschat.Common.Common;
import com.example.sahil.friendschat.Holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity {

    ListView listusers;
    Button buttoncreatechat;

    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);


        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);





        listusers = (ListView)findViewById(R.id.listusers);
        listusers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        buttoncreatechat = (Button)findViewById(R.id.button_create_chat);
        buttoncreatechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mode == null) {
                    int countChoice = listusers.getCount();

                    if (listusers.getCheckedItemPositions().size() == 1)
                        createPrivateChat(listusers.getCheckedItemPositions());
                    else if (listusers.getCheckedItemPositions().size() > 1)
                        createGroupChat(listusers.getCheckedItemPositions());
                    else
                        Toast.makeText(ListUsersActivity.this, "Please Select Friend to Chat", Toast.LENGTH_LONG).show();
                } else if (mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoice = listusers.getCount();
                        SparseBooleanArray checkItemPositions = listusers.getCheckedItemPositions();
                        for (int i = 0; i < cntChoice; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser user = (QBUser) listusers.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }
                        //call service

                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(getBaseContext(), "Add User Successfully ", Toast.LENGTH_LONG).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                } else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();

                        int cntChoice = listusers.getCount();
                        SparseBooleanArray checkItemPositions = listusers.getCheckedItemPositions();
                        for (int i = 0; i < cntChoice; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser user = (QBUser) listusers.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }
                        //call service

                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(getBaseContext(), "Remove User Successfully ", Toast.LENGTH_LONG).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });


                    }
                }
            }
        });

        if(mode == null && qbChatDialog == null)
            retrieveAllUser();
        else
        {
            if(mode.equals(Common.UPDATE_ADD_MODE))
                loadListAvailableUser();
            else if (mode.equals(Common.UPDATE_REMOVE_MODE))
                loadListUserInGroup();
        }
    }

    private void loadListUserInGroup() {

        buttoncreatechat.setText("REMOVE USER");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        List<Integer>  occupantsId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInGroup = QBUsersHolder.getInstance().getUserByIds(occupantsId);

                        ArrayList<QBUser> users = new ArrayList<QBUser>();
                        users.addAll(listUserAlreadyInGroup);

                        ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),users);
                        listusers.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        userAdd = users;

                    }

                    @Override
                    public void onError(QBResponseException e) {

                        Toast.makeText(ListUsersActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void loadListAvailableUser() {

        buttoncreatechat.setText("ADD USER");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ArrayList<QBUser> listUsers = QBUsersHolder.getInstance().getAllUsers();

                        List<Integer>  occupantsId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInChatGroup = QBUsersHolder.getInstance().getUserByIds(occupantsId);

                        //remove user already in chat group

                        for(QBUser user:listUserAlreadyInChatGroup)
                            listUsers.remove(user);
                        if(listUsers.size()>0)
                        {
                            ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),listUsers);
                            listusers.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            userAdd = listUsers;
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUsersActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });

    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog progessDialog = new ProgressDialog(ListUsersActivity.this);
        progessDialog.setMessage("Please Wait...");
        progessDialog.setCanceledOnTouchOutside(false);
        progessDialog.show();


        int countChoice = listusers.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for(int i=0;i<countChoice;i++)
        {

            if(checkedItemPositions.get(i)) {
                QBUser user = (QBUser)listusers.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }

        //create chat dialog

        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.crateChatDialogName(occupantIdsList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);


        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                progessDialog.dismiss();
                Toast.makeText(getBaseContext(),"chat dialog created successfully",Toast.LENGTH_LONG).show();

                //Send system message to recipent user id
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for(int i=0;i<qbChatDialog.getOccupants().size();i++)
                {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }



                finish();
            }

            @Override
            public void onError(QBResponseException e) {
               Log.e("ERROR",e.getMessage());
            }
        });
    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog progessDialog = new ProgressDialog(ListUsersActivity.this);
        progessDialog.setMessage("Please Wait...");
        progessDialog.setCanceledOnTouchOutside(false);
        progessDialog.show();

        int countChoice = listusers.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for(int i=0;i<countChoice;i++)
        {

            if(checkedItemPositions.get(i))
            {
                final QBUser user = (QBUser) listusers.getItemAtPosition(i);
               QBChatDialog dialog = DialogUtils.buildPrivateDialog((user.getId()));

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        progessDialog.dismiss();
                        Toast.makeText(getBaseContext(),"chat private dialog created successfully",Toast.LENGTH_LONG).show();

                        //Send system message to recipent user id
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                       qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",e.getMessage());
                    }
                });
            }
        }


    }

    private void retrieveAllUser() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {


                //add to cache
                QBUsersHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbuserwithoutcurrent = new ArrayList<QBUser>();

                for(QBUser user : qbUsers)
                {
                    if(!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()));
                    qbuserwithoutcurrent.add(user);
                }
                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(),qbuserwithoutcurrent);
                listusers.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

                Log.e("ERROR",e.getMessage());
            }
        });
    }
}
