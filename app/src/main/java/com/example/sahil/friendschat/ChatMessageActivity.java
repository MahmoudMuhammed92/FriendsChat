package com.example.sahil.friendschat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.sahil.friendschat.Adapter.ChatMessageAdapter;
import com.example.sahil.friendschat.Common.Common;
import com.example.sahil.friendschat.Holder.QBChatMessageHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements  QBChatDialogMessageListener{

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton;
    EditText editContent;

    ChatMessageAdapter adapter;

    //Variable for Edit/delete message

    int contetexMenuIndexClick = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    ImageView img_online_count;
    TextView txt_online_count;

    //update dialog
    Toolbar  toolbar;

    //Dialog Avater
  //  static final  int SELECT_PICTURE = 7171;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(qbChatDialog.getType() ==  QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.chat_message_group_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.chat_groupt_edit_name:
                editNameGroup();
                break;
            case R.id.chat_groupt_add_user:
                addUser();
                break;
            case R.id.chat_groupt_remove_user:
               remvoeUser();
                break;
            default:
               break;
        }

        return true;
    }

    private void remvoeUser() {

        Intent intent = new Intent(this,ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {

        Intent intent = new Intent(this,ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_ADD_MODE);
        startActivity(intent);

    }

    private void editNameGroup() {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final EditText newName = (EditText)view.findViewById(R.id.edt_new_group_name);

        //Set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        qbChatDialog.setName(newName.getText().toString()); //set new name

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ChatMessageActivity.this,"Gropup Name Edited",Toast.LENGTH_LONG).show();
                                        toolbar.setTitle(qbChatDialog.getName());
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(ChatMessageActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                             dialog.cancel();
                    }
                });

        //create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //get index contex menu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        contetexMenuIndexClick  = info.position;
        switch (item.getItemId())
        {
            case R.id.chat_message_udpate_message:
                udpateMessage();
                break;
            case R.id.chat_message_delete_message:
               deleteMessage();
                break;
            default:
                break;
        }
        return true;
    }

    private void deleteMessage() {

        final ProgressDialog deleteDialog = new ProgressDialog(ChatMessageActivity.this);
        deleteDialog.setMessage("Please Wait...");
        deleteDialog.show();


        editMessage =  QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(contetexMenuIndexClick);

        QBRestChatService.deleteMessage(editMessage.getId(),false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveAllMessage();
                deleteDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void udpateMessage() {

        //set mesaage for edittex

        editMessage =  QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(contetexMenuIndexClick);
        editContent.setText(editMessage.getBody());
        isEditMode = true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu,menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initViews();
        
        initChatDialogs();
        
        retrieveAllMessage();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editContent.getText().toString().isEmpty()) {
                    if (!isEditMode) {
                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setBody(editContent.getText().toString());
                        chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                        chatMessage.setSaveToHistory(true);

                        try {
                            qbChatDialog.sendMessage(chatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        //Fix Private Chat dont show message

                        if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                            //Cache Message
                            QBChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                            ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialogId(chatMessage.getDialogId());

                            adapter = new ChatMessageAdapter(getBaseContext(), messages);
                            lstChatMessages.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        }

                        //remove text from edittext
                        editContent.setText("");
                        editContent.setFocusable(true);
                    } else {

                        final ProgressDialog udpateDialog = new ProgressDialog(ChatMessageActivity.this);
                        udpateDialog.setMessage("Please Wait...");
                        udpateDialog.show();

                        QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                        messageUpdateBuilder.updateText(editContent.getText().toString()).markDelivered().markRead();

                        QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                                .performAsync(new QBEntityCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                        //refresh data
                                        retrieveAllMessage();
                                        isEditMode = false; //reset variable

                                        udpateDialog.dismiss();

                                        //reset edit content
                                        editContent.setText("");
                                        editContent.setFocusable(true);
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(getBaseContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                    }
                                });
                    }
                }

            }
        });
    }

    private void retrieveAllMessage() {

        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(1000);

        if(qbChatDialog != null)
        {
            QBRestChatService.getDialogMessages(qbChatDialog,messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {

                    //put messages to cache

                    QBChatMessageHolder.getInstance().putMessages(qbChatDialog.getDialogId(),qbChatMessages);

                     adapter =  new ChatMessageAdapter(getBaseContext(),qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialogs() {
        
        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);

        /*
        //dialog_avatar
        if(qbChatDialog.getPhoto() != null)
        {
            QBContent.getFile(Integer.parseInt(qbChatDialog.getPhoto()))
                    .performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            String fileURL = qbFile.getPublicUrl();
                            Picasso.with(getBaseContext())
                                    .load(fileURL)
                                    .resize(50,50)
                                    .centerCrop()
                                    .into(dialog_avatar);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.e("ERROR IMAGE",""+e.getMessage());
                        }
                    });
        }
        */


        qbChatDialog.initForChat(QBChatService.getInstance());

        //Register listener incoming message
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });



        //add join group to enable group chat

        if(qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP)
        {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d("ERROR",""+e.getMessage());

                }
            });
        }


        //get online users
        QBChatDialogParticipantListener participantListener =  new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if(dialogId == qbChatDialog.getDialogId())
                {
                    QBRestChatService.getChatDialogById(dialogId)
                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    try {
                                        Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                        TextDrawable.IBuilder builder = TextDrawable.builder()
                                                .beginConfig()
                                                .withBorder(4)
                                                .endConfig()
                                                .round();
                                        TextDrawable online = builder.build("", Color.RED);
                                        img_online_count.setImageDrawable(online);

                                        txt_online_count.setTextColor(Color.WHITE);
                                        txt_online_count.setText(String.format("%d/%d",onlineList.size(),qbChatDialog.getOccupants().size()));
                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });

                }
            }
        };

        qbChatDialog.addParticipantListener(participantListener);




        qbChatDialog.addMessageListener(this);

        //set title for toolbar
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);


    }

    private void initViews() {

            lstChatMessages = (ListView)findViewById(R.id.list_of_message);
            submitButton = (ImageButton)findViewById(R.id.send_button);
            editContent = (EditText)findViewById(R.id.edt_content);

            img_online_count = (ImageView)findViewById(R.id.image_online_count);
            txt_online_count  = (TextView)findViewById(R.id.txt_online_count);


         /*
        //Dialog_Avater

        dialog_avatar = (ImageView)findViewById(R.id.dialog_avatar);
        dialog_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectImage = new Intent();
                selectImage.setType("image/*");
                selectImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(selectImage,"Select Picturre"),SELECT_PICTURE);
            }
        });

          */
        //Add context menu
        registerForContextMenu(lstChatMessages);

        //Add Toolbar
          toolbar = (Toolbar)findViewById(R.id.chat_message_toolbar);

    }



     /*
    //Dialog Avatar
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RESULT_OK)
        {
            if(requestCode == SELECT_PICTURE)
            {
                Uri selectImageUri = data.getData();

                final ProgressDialog progressDialog = new ProgressDialog(ChatMessageActivity.this);
                progressDialog.setMessage("Pleas Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                try
                {
                    //convert uri to file

                    InputStream in = getContentResolver().openInputStream(selectImageUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
                    File file = new File(Environment.getExternalStorageDirectory()+"image.png");
                    FileOutputStream fileOut = new FileOutputStream(file);
                    fileOut.write(bos.toByteArray());
                    fileOut.flush();
                    fileOut.close();


                    int imgageSizeKb = (int)file.length()/1024;
                    if(imgageSizeKb >= (1024*100))
                    {
                        Toast.makeText(this,"Error Image Size",Toast.LENGTH_LONG).show();
                        return;
                    }

                    //Upload File
                    QBContent.uploadFileTask(file,true,null)
                            .performAsync(new QBEntityCallback<QBFile>() {
                                @Override
                                public void onSuccess(QBFile qbFile, Bundle bundle) {
                                    qbChatDialog.setPhoto(qbFile.getId().toString());

                                    //update chat dialog
                                    QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
                                    QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                                @Override
                                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                                    progressDialog.dismiss();
                                                    dialog_avatar.setImageBitmap(bitmap);
                                                }

                                                @Override
                                                public void onError(QBResponseException e) {
                                                     Toast.makeText(ChatMessageActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

*/

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //Cache Message
        QBChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(),messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
     Log.e("ERROR",""+e.getMessage());
    }
}
