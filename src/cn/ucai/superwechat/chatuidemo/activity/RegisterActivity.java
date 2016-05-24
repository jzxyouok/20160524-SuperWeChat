/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.chatuidemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.chatuidemo.I;
import cn.ucai.superwechat.chatuidemo.R;
import cn.ucai.superwechat.chatuidemo.SuperwechatApplication;
import cn.ucai.superwechat.chatuidemo.data.OkHttpUtils;
import cn.ucai.superwechat.chatuidemo.listener.OnSetAvatarListener;
import cn.ucai.superwechat.chatuidemo.utils.Utils;

/**
 * 注册页
 */
public class RegisterActivity extends BaseActivity {
    Context mContext;
    Activity mActivity;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText confirmPwdEditText;
    private EditText metNick;
    private TextView mtvSetAvatar;
    private Button mbtnRegister;
    private Button mbtnLogin;
    OnSetAvatarListener mOnSetAvatarListener;
    private ImageView mivAvatar;
    String username, nickname, pwd;
    String avatarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = this;
        mActivity = this;
        initView();
        setListener();

    }

    private void setListener() {
        mtvSetAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarName = System.currentTimeMillis() + "";
                mOnSetAvatarListener = new OnSetAvatarListener(mActivity, R.id.ll_register, avatarName, I.AVATAR_TYPE_USER_PATH);
            }
        });
        mbtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setRegisterListener(mbtnRegister);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mOnSetAvatarListener.setAvatar(requestCode, data, mivAvatar);
        }
    }

    private void initView() {
        mtvSetAvatar = (TextView) findViewById(R.id.tvSetAvatar);
        userNameEditText = (EditText) findViewById(R.id.etUserName);
        passwordEditText = (EditText) findViewById(R.id.etPassWord);
        confirmPwdEditText = (EditText) findViewById(R.id.etConfirmPassWord);
        metNick = (EditText) findViewById(R.id.etNickName);
        mbtnRegister = (Button) findViewById(R.id.btnRegister);
        mbtnLogin = (Button) findViewById(R.id.btnRegisterLogin);
        mivAvatar = (ImageView) findViewById(R.id.iv_set_avatar);
    }

    /**
     * 注册
     */
    private void setRegisterListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = userNameEditText.getText().toString().trim();
                nickname = metNick.getText().toString();
                pwd = passwordEditText.getText().toString().trim();
                String confirm_pwd = confirmPwdEditText.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    userNameEditText.requestFocus();
                    userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_empty));
                    return;
                } else if (!username.matches("[a-zA-Z]+[\\da-zA-z_]+")) {
                    userNameEditText.requestFocus();
                    userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_wd));
                    return;
                } else if (TextUtils.isEmpty(nickname)) {
                    confirmPwdEditText.requestFocus();
                    confirmPwdEditText.setError(getResources().getString(R.string.Nick_name_cannot_be_empty));
                    return;
                } else if (TextUtils.isEmpty(pwd)) {
                    passwordEditText.requestFocus();
                    passwordEditText.setError(getResources().getString(R.string.Password_cannot_be_empty));
                    return;
                } else if (TextUtils.isEmpty(confirm_pwd)) {
                    confirmPwdEditText.requestFocus();
                    confirmPwdEditText.setError(getResources().getString(R.string.Confirm_password_cannot_be_empty));
                    return;
                } else if (!pwd.equals(confirm_pwd)) {
                    Toast.makeText(mContext, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
                    final ProgressDialog pd = new ProgressDialog(mContext);
                    pd.setMessage(getResources().getString(R.string.Is_the_registered));
                    pd.show();
                    registerAppServer(pd);
                }
            }
        });
    }

    private void HXRegister(final ProgressDialog pd) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 调用sdk注册方法
                    EMChatManager.getInstance().createAccountOnServer(username, pwd);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(mContext,getResources().getString(R.string.send_successful),Toast.LENGTH_SHORT);
                            pd.dismiss();
                            finish();
                        }
                    });

                    SuperwechatApplication.getInstance().setUserName(username);
                } catch (final EaseMobException e) {
                    unRegisterAppServer(pd);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing()) ;
                                pd.dismiss();
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NONETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.UNAUTHORIZED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.ILLEGAL_USER_NAME) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void registerAppServer(final ProgressDialog pd) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/user_avatar/" + avatarName + I.AVATAR_SUFFIX_JPG);
        Log.i("main", file.getAbsolutePath());
        OkHttpUtils<Message> okHttpUtils = new OkHttpUtils<>();
        okHttpUtils.url(SuperwechatApplication.SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_REGISTER)
                .addParam(I.User.USER_NAME, username)
                .addParam(I.User.PASSWORD, pwd)
                .addParam(I.User.NICK, nickname)
                .addFile(file)
                .targetClass(Message.class)
                .execute(new OkHttpUtils.OnCompleteListener<Message>() {
                    @Override
                    public void onSuccess(final Message result) {
                        HXRegister(pd);
                    }

                    @Override
                    public void onError(final String error) {
                    }
                });
    }

    private void unRegisterAppServer(final ProgressDialog pd) {
        OkHttpUtils<Message> okHttpUtils = new OkHttpUtils<>();
        okHttpUtils.url(SuperwechatApplication.SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_UNREGISTER)
                .addParam(I.User.USER_NAME, username)
                .targetClass(Message.class)
                .execute(new OkHttpUtils.OnCompleteListener<Message>() {
                    @Override
                    public void onSuccess(final Message result) {
                    }

                    @Override
                    public void onError(final String error) {
                    }
                });
    }

    public void back(View view) {
        finish();
    }

}
