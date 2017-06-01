package com.example.sahil.friendschat.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.sahil.friendschat.Holder.QBUnreadMessageHolder;
import com.example.sahil.friendschat.R;
import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;

/**
 * Created by SAHIL on 3/31/2017.
 */

public class ChatDialogAdapter extends BaseAdapter {

    private  Context context;
    private ArrayList<QBChatDialog>  qbChatDialogs;

    public ChatDialogAdapter(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       View view  = convertView;
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog,null);

            TextView textTitle,textMessage;
            final ImageView imageView,image_unread;

            textMessage = (TextView) view.findViewById(R.id.list_chat_dialog_message);
            textTitle = (TextView) view.findViewById(R.id.list_chat_dialog_title);
            imageView = (ImageView)view.findViewById(R.id.chatdialog_image);
            image_unread = (ImageView)view.findViewById(R.id.image_unread);

            textMessage.setText(qbChatDialogs.get(position).getLastMessage());
            textTitle.setText(qbChatDialogs.get(position).getName());

            ColorGenerator generator =  ColorGenerator.MATERIAL;
            int ramdomcolor = generator.getRandomColor();

            //dialog_avatar  // if statement
        //   if(qbChatDialogs.get(position).getPhoto().equals("null")) {

                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();
                //set first character

                TextDrawable drawable = builder.build(textTitle.getText().toString().substring(0, 1).toUpperCase(), ramdomcolor);

                imageView.setImageDrawable(drawable);
          // }
            /*
            else
            {
                //Download bitmap form server and set for dialog
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(position).getPhoto()))
                        .performAsync(new QBEntityCallback<QBFile>() {
                            @Override
                            public void onSuccess(QBFile qbFile, Bundle bundle) {
                                String fileURL = qbFile.getPublicUrl();
                                Picasso.with(context)
                                        .load(fileURL)
                                        .resize(50,50)
                                        .centerCrop()
                                        .into(imageView);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                 Log.e("ERROR IMAGE",""+e.getMessage());
                            }
                        });
            }
             */

            //set imgae for uread count

            TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();
            int unread_count = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if(unread_count > 0)
            {
                TextDrawable uread_drawable = unreadBuilder.build(""+unread_count , Color.GREEN);
                image_unread.setImageDrawable(uread_drawable);

            }

        }
        return  view;
    }
}
