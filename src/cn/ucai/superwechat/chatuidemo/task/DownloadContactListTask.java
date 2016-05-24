package cn.ucai.superwechat.chatuidemo.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.chatuidemo.I;
import cn.ucai.superwechat.chatuidemo.SuperwechatApplication;
import cn.ucai.superwechat.chatuidemo.activity.BaseActivity;
import cn.ucai.superwechat.chatuidemo.data.ApiParams;
import cn.ucai.superwechat.chatuidemo.data.GsonRequest;
import cn.ucai.superwechat.chatuidemo.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadContactListTask extends BaseActivity {
    private String userName;
    private Context mContext;
    private String path;

    public DownloadContactListTask(Context mContext, String userName) {
        this.mContext = mContext;
        this.userName = userName;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams().with(I.Contact.USER_NAME, userName)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Contact[]>(path,Contact[].class,downloadScucessListener(),errorListener()));
    }

    private Response.Listener<Contact[]> downloadScucessListener() {
        return new Response.Listener<Contact[]>() {
            @Override
            public void onResponse(Contact[] contacts) {
                if (contacts != null) {
                    ArrayList<Contact> list = Utils.array2List(contacts);
                    ArrayList<Contact> contactList = SuperwechatApplication.getInstance().getContactList();
                    contactList.clear();
                    contactList.addAll(list);
                    HashMap<String, Contact> userList = SuperwechatApplication.getInstance().getUserList();
                    userList.clear();
                    for(Contact c:list){
                        userList.put(c.getMContactCname(),c);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_contact_list"));
                }
            }
        };
    }
}
