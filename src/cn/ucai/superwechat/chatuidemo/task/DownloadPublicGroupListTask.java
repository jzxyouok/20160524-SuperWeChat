package cn.ucai.superwechat.chatuidemo.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

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
public class DownloadPublicGroupListTask extends BaseActivity {
    private String userName;
    private Context mContext;
    private int pageId;
    private int pageSize;
    private String path;

    public DownloadPublicGroupListTask(Context mContext, String userName, int pageId, int pageSize) {
        this.mContext = mContext;
        this.userName = userName;
        this.pageId = pageId;
        this.pageSize = pageSize;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams().with(I.User.USER_NAME, userName)
                    .with(I.PAGE_ID, pageId + "")
                    .with(I.PAGE_SIZE,pageSize+"")
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path, Group[].class, downloadScucessListener(), errorListener()));
    }

    private Response.Listener<Group[]> downloadScucessListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups != null) {
                    ArrayList<Group> publicGroupList = SuperwechatApplication.getInstance().getPublicGroupList();
                    publicGroupList.clear();
                    //SuperwechatApplication.getInstance().setPublicGroupList(Utils.array2List(groups));
                    publicGroupList.addAll(Utils.array2List(groups));
                    mContext.sendStickyBroadcast(new Intent("update_public_groups"));
                }
            }
        };
    }
}
