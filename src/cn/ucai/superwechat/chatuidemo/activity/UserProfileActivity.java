package cn.ucai.superwechat.chatuidemo.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.easemob.EMValueCallBack;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import com.easemob.chat.EMChatManager;

import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.chatuidemo.DemoHXSDKHelper;
import cn.ucai.superwechat.chatuidemo.I;
import cn.ucai.superwechat.chatuidemo.R;
import cn.ucai.superwechat.chatuidemo.SuperwechatApplication;
import cn.ucai.superwechat.chatuidemo.data.ApiParams;
import cn.ucai.superwechat.chatuidemo.data.GsonRequest;
import cn.ucai.superwechat.chatuidemo.data.MultipartRequest;
import cn.ucai.superwechat.chatuidemo.data.RequestManager;
import cn.ucai.superwechat.chatuidemo.db.UserDao;
import cn.ucai.superwechat.chatuidemo.domain.EMUser;
import cn.ucai.superwechat.chatuidemo.listener.OnSetAvatarListener;
import cn.ucai.superwechat.chatuidemo.utils.ImageUtils;
import cn.ucai.superwechat.chatuidemo.utils.UserUtils;
import cn.ucai.superwechat.chatuidemo.utils.Utils;

import com.squareup.picasso.Picasso;

public class UserProfileActivity extends BaseActivity implements OnClickListener{

	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	private NetworkImageView headAvatar;
	private ImageView headPhotoUpdate;
	private ImageView iconRightArrow;
	private TextView tvNickName;
	private TextView tvUsername;
	private ProgressDialog dialog;
	private RelativeLayout rlNickName;
	private Context mContext;
	private Activity mActivity;
	String avatarName;
    OnSetAvatarListener mOnSetAvatarListener;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_user_profile);
		initView();
		initListener();
	}

	private void initView() {
		headAvatar = (NetworkImageView) findViewById(R.id.user_head_avatar);
		headPhotoUpdate = (ImageView) findViewById(R.id.user_head_headphoto_update);
		tvUsername = (TextView) findViewById(R.id.user_username);
		tvNickName = (TextView) findViewById(R.id.user_nickname);
		rlNickName = (RelativeLayout) findViewById(R.id.rl_nickname);
		iconRightArrow = (ImageView) findViewById(R.id.ic_right_arrow);
		mContext = this;
		mActivity=this;

	}

	private void initListener() {
		Intent intent = getIntent();
		String username = intent.getStringExtra("username");
		boolean enableUpdate = intent.getBooleanExtra("setting", false);
		if (enableUpdate) {
			headPhotoUpdate.setVisibility(View.VISIBLE);
			iconRightArrow.setVisibility(View.VISIBLE);
			rlNickName.setOnClickListener(this);
			headAvatar.setOnClickListener(this);
		} else {
			headPhotoUpdate.setVisibility(View.GONE);
			iconRightArrow.setVisibility(View.INVISIBLE);
		}
		if (username == null||username.equals(SuperwechatApplication.getInstance().getUserName())) {
			tvUsername.setText(SuperwechatApplication.getInstance().getUserName());
			UserUtils.setMCurrentUserNick(tvNickName);
			UserUtils.setMCurrentUserAvatar(this, headAvatar);
		} else {
			tvUsername.setText(username);
			UserUtils.setMUserNick(username,tvNickName);
			UserUtils.setUserAvatar(this, username, headAvatar,R.drawable.default_avatar);
			//asyncFetchUserInfo(username);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_head_avatar:
			//uploadHeadPhoto();
			mOnSetAvatarListener=new OnSetAvatarListener(mActivity,R.id.layout_upate_avatar,getavatarName(),I.AVATAR_TYPE_USER_PATH);
			break;
		case R.id.rl_nickname:
			final EditText editText = new EditText(this);
			new AlertDialog.Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
					.setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String nickString = editText.getText().toString();
							if (TextUtils.isEmpty(nickString)) {
								Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
								return;
							}
							updateUserNick(nickString);
							//updateRemoteNick(nickString);
						}
					}).setNegativeButton(R.string.dl_cancel, null).show();
			break;
		default:
			break;
		}

	}
	private void updateUserNick(final String nickString) {
		try {
			String path=new ApiParams()
                    .with(I.User.USER_NAME,SuperwechatApplication.getInstance().getUserName())
                    .with(I.User.NICK,nickString)
                    .getRequestUrl(I.REQUEST_UPDATE_USER_NICK);
			executeRequest(new GsonRequest<User>(path,User.class,responseListener(nickString),errorListener()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Response.Listener<User> responseListener(final String nickString) {
		return new Response.Listener<User>() {
			@Override
			public void onResponse(User user) {
				if (user != null && user.isResult()) {
					updateRemoteNick(nickString);
				} else {
					Utils.showToast(mContext, Utils.getResourceString(mContext, user.getMsg()), Toast.LENGTH_LONG);
				}
			}
		};
	}



	public void asyncFetchUserInfo(final String username){
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EMUser>() {

			@Override
			public void onSuccess(EMUser user) {
				if (user != null) {
					tvNickName.setText(user.getNick());
					//UserUtils.setMUserNick(username,tvNickName);
					//UserUtils.setMCurrentUserAvatar(mContext,headAvatar);
					if(!TextUtils.isEmpty(user.getAvatar())){
						 Picasso.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.default_avatar).into(headAvatar);
					}else{
						Picasso.with(UserProfileActivity.this).load(R.drawable.default_avatar).into(headAvatar);
					}
					UserUtils.saveUserInfo(user);
				}
			}

			@Override
			public void onError(int error, String errorMsg) {
			}
		});
	}



	private void uploadHeadPhoto() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(R.string.dl_title_upload_photo);
		builder.setItems(new String[] { getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload) },
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
									Toast.LENGTH_SHORT).show();
							break;
						case 1:
							Intent pickIntent = new Intent(Intent.ACTION_PICK,null);
							pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							startActivityForResult(pickIntent, REQUESTCODE_PICK);
							break;
						default:
							break;
						}
					}
				});
		builder.create().show();
	}



	private void updateRemoteNick(final String nickName) {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean updatenick = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().updateParseNickName(nickName);
				if (UserProfileActivity.this.isFinishing()) {
					return;
				}
				if (!updatenick) {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
									.show();
							dialog.dismiss();
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
									.show();
							tvNickName.setText(nickName);
							SuperwechatApplication.currentUserNick=nickName;
							User user=SuperwechatApplication.getInstance().getUser();
							Log.e("myerror",user.toString());
							user.setMUserNick(nickName);
							UserDao userDao=new UserDao(mContext);
							userDao.updateUser(user);
						}
					});
				}
			}
		}).start();
	}
	private final String boundary = "apiclient-" + System.currentTimeMillis();
	private final String mimeType = "multipart/form-data;boundary=" + boundary;
     byte[] mutipartBody;
	 Bitmap bitmap;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//		case REQUESTCODE_PICK:
//			if (data == null || data.getData() == null) {
//				return;
//			}
//			startPhotoZoom(data.getData());
//			break;
//		case REQUESTCODE_CUTTING:
//			if (data != null) {
//				setPicToView(data);
//			}
//			break;
//		default:
//			break;
//		}

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		mOnSetAvatarListener.setAvatar(requestCode,data,headAvatar);
		if (requestCode == OnSetAvatarListener.REQUEST_CROP_PHOTO) {
			updateUserAvatar();
		}
	}

	private void updateUserAvatar() {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
		dialog.show();
		File file=new File(ImageUtils.getAvatarPath(mContext,I.AVATAR_TYPE_USER_PATH),avatarName+I.AVATAR_SUFFIX_JPG);
		String path=file.getAbsolutePath();
		bitmap= BitmapFactory.decodeFile(path);
		mutipartBody=getImageBytes(bitmap);
		String url=null;
		try {
			url = new ApiParams()
					.with(I.AVATAR_TYPE, I.AVATAR_TYPE_USER_PATH)
					.with(I.User.USER_NAME, SuperwechatApplication.getInstance().getUserName())
					.getRequestUrl(I.REQUEST_UPLOAD_AVATAR);
			executeRequest(new MultipartRequest<Message>(url, Message.class, null,
					responseUpdateUserAvatarListener(path), errorListener(), mimeType, mutipartBody));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Response.Listener<Message> responseUpdateUserAvatarListener(final String path) {
		return new Response.Listener<Message>() {
			@Override
			public void onResponse(Message message) {
				if(message.isResult()){
					RequestManager.getRequestQueue().getCache().remove(UserUtils.getAvatarPath(SuperwechatApplication.getInstance().getUserName()));
					UserUtils.setMCurrentUserAvatar(mContext,headAvatar);
				}else{
					UserUtils.setMCurrentUserAvatar(mContext,headAvatar);
						Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
								Toast.LENGTH_SHORT).show();
				}
					dialog.dismiss();
			}
		};
	}

	public byte[] getImageBytes(Bitmap bmp){
		if(bmp==null)return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG,100,baos);
		byte[] imageBytes = baos.toByteArray();
		return imageBytes;
	}
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}

	/**
	 * save the picture data
	 *
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(getResources(), photo);
			headAvatar.setImageDrawable(drawable);
			//UserUtils.setMCurrentUserAvatar(mContext,headAvatar);
			uploadUserAvatar(Bitmap2Bytes(photo));
		}

	}

	private void uploadUserAvatar(final byte[] data) {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
		new Thread(new Runnable() {

			@Override
			public void run() {
				final String avatarUrl = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().uploadUserAvatar(data);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						if (avatarUrl != null) {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
									Toast.LENGTH_SHORT).show();
						}

					}
				});

			}
		}).start();

		dialog.show();
	}


	public byte[] Bitmap2Bytes(Bitmap bm){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public String getavatarName() {
		 avatarName= System.currentTimeMillis()+"";return avatarName;
	}
}
