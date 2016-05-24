package cn.ucai.superwechat.chatuidemo.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.chatuidemo.I;
import cn.ucai.superwechat.chatuidemo.SuperwechatApplication;
import cn.ucai.superwechat.chatuidemo.activity.BaseActivity;
import cn.ucai.superwechat.chatuidemo.data.ApiParams;
import cn.ucai.superwechat.chatuidemo.data.GsonRequest;
import cn.ucai.superwechat.chatuidemo.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadAllGroupTask extends BaseActivity {
    private Context mContext;
    private String userName;
    private String path;

    public DownloadAllGroupTask(Context mContext, String userName) {
        this.mContext = mContext;
        this.userName = userName;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams().with(I.User.USER_NAME,userName)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,downloadScucessListener(),errorListener()));
    }

    private Response.Listener<Group[]> downloadScucessListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups!=null) {
                    ArrayList<Group> groupList = SuperwechatApplication.getInstance().getGroupList();
                    groupList.clear();
                    groupList.addAll(Utils.array2List(groups));
                    mContext.sendStickyBroadcast(new Intent("update_group_list"));
                }

            }
        };
    }
}
