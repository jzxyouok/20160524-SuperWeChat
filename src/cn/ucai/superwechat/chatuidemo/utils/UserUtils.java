package cn.ucai.superwechat.chatuidemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.chatuidemo.Constant;
import cn.ucai.superwechat.chatuidemo.DemoHXSDKHelper;
import cn.ucai.superwechat.chatuidemo.I;
import cn.ucai.superwechat.chatuidemo.R;
import cn.ucai.superwechat.chatuidemo.SuperwechatApplication;
import cn.ucai.superwechat.chatuidemo.data.ApiParams;
import cn.ucai.superwechat.chatuidemo.domain.EMUser;

import com.android.volley.toolbox.*;
import com.android.volley.toolbox.ImageLoader;
import com.easemob.util.HanziToPinyin;
import com.squareup.picasso.Picasso;

import java.io.File;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     * @param username
     * @return
     */
    public static EMUser getUserInfo(String username){
        EMUser user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(username);
        if(user == null){
            user = new EMUser(username);
        }
            
        if(user != null){
            //demo没有这些数据，临时填充
        	if(TextUtils.isEmpty(user.getNick()))
        		user.setNick(username);
        }
        return user;
    }
    /**
     * 根据username获取相应user
     * @param username 用户名
     * @return 都懂，名字解释一切
     */
	public static Contact getUserBeanInfo(String username) {
		return SuperwechatApplication.getInstance().getUserList().get(username);
	}

	/**
     * 设置用户头像
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EMUser user = getUserInfo(username);
        if(user != null && user.getAvatar() != null){
            Picasso.with(context).load(user.getAvatar()).placeholder(R.drawable.default_avatar).into(imageView);
        }else{
            Picasso.with(context).load(R.drawable.default_avatar).into(imageView);
        }
    }

    /**
     * 设置联系人头像 上面那个方法的重载方法，多一个参数
     * @param username 联系人用户名
     * @param context  不用多说都懂
     * @param defaultImageID 默认头像资源Id
     * @param imageview   需要设置头像的View
     *
     */
	public static void setUserAvatar(Context context,String username, NetworkImageView imageview,int defaultImageID) {
		String url = null;
		try {
			url = new ApiParams()
					.with(I.AVATAR_TYPE, username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_AVATAR);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(context), LruImageCache.instance());
		imageview.setImageUrl(url,imageLoader);
		imageview.setDefaultImageResId(defaultImageID);
		imageview.setErrorImageResId(defaultImageID);
	}

	/**
     * 设置当前用户头像
     */
	public static void setCurrentUserAvatar(Context context, ImageView imageView) {
		EMUser user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
		if (user != null && user.getAvatar() != null) {
			Picasso.with(context).load(user.getAvatar()).placeholder(R.drawable.default_avatar).into(imageView);
		} else {
			Picasso.with(context).load(R.drawable.default_avatar).into(imageView);
		}
	}
    /**
     * 我的设置当前登录用户头像
     * @param context 上下文参数
     * @param imageView 需要设置当前登录用户头像的View
     */
	public static void setMCurrentUserAvatar(Context context, NetworkImageView imageView) {
        String url = null;
        try {
            url = new ApiParams()
                    .with(I.AVATAR_TYPE, SuperwechatApplication.getInstance().getUserName())
                    .getRequestUrl(I.REQUEST_DOWNLOAD_AVATAR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(context), LruImageCache.instance());
        imageView.setImageUrl(url,imageLoader);
        imageView.setDefaultImageResId(R.drawable.default_avatar);
        imageView.setErrorImageResId(R.drawable.default_avatar);
	}
    
    /**
     * 原环信设置用户昵称
     */
    public static void setUserNick(String username,TextView textView){
    	EMUser user = getUserInfo(username);
    	if(user != null){
    		textView.setText(user.getNick());
    	}else{
    		textView.setText(username);
    	}
    }

    /**
     * 我的设置用户昵称
     */
    public static void setMUserNick(String username, TextView textView) {
        Contact c = getUserBeanInfo(username);
        if (c != null) {
            String nick = c.getMUserNick();
            if (nick != null&&!nick.isEmpty()) {
                textView.setText(nick);
            } else {
                textView.setText(username);
            }
        }
    }


	/**
     * 设置当前用户昵称
     */
    public static void setCurrentUserNick(TextView textView){
        EMUser user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
        if(textView != null){
            textView.setText(user.getNick());
        }
    }
    /**
     * 我的设置当前用户昵称
     */
    public static void setMCurrentUserNick(TextView textView){
        if(textView != null){
            if (SuperwechatApplication.currentUserNick != null && !SuperwechatApplication.currentUserNick.isEmpty()) {
                textView.setText(SuperwechatApplication.currentUserNick);
            } else {
                textView.setText(SuperwechatApplication.currentUserNick);
            }

        }
    }
    
    /**
     * 保存或更新某个用户
     * @param newUser
     */
	public static void saveUserInfo(EMUser newUser) {
		if (newUser == null || newUser.getUsername() == null) {
			return;
		}
		((DemoHXSDKHelper) HXSDKHelper.getInstance()).saveContact(newUser);
	}

    /**
     * 根据联系人昵称拼音首字母设置联系人的Header成员变量值，
     *  昵称为空时则根据用户名，数字开头为#，申请与通知、群聊为空
     *
     * @param username
     * @param
     */
    public static void setUserHearder(String username, Contact user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getMUserNick())) {
            headerName = user.getMUserNick();
        } else {
            headerName = user.getMContactUserName();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)
                || username.equals(Constant.GROUP_USERNAME)){
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }


    /**
     * Lru缓存作业ImageLoader的缓存
     * 采用单例模式保证所有的NetWorkImageView的缓存都为一个
     */
    static class LruImageCache implements ImageLoader.ImageCache{

        private LruCache<String, Bitmap> mMemoryCache;

        private static LruImageCache lruImageCache;

        private LruImageCache(){
            // Get the Max available memory
//            int maxMemory = (int) Runtime.getRuntime().maxMemory();
//            int cacheSize = maxMemory / 8;
            //缓存占用内存为10M，显示几个联系人头像应该够用了吧~不用那么大
            int cacheSize = 10*1024*1024;
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
                @Override
                protected int sizeOf(String key, Bitmap bitmap){
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }

        public static LruImageCache instance(){
            if(lruImageCache == null){
                lruImageCache = new LruImageCache();
            }
            return lruImageCache;
        }

        @Override
        public Bitmap getBitmap(String arg0) {
            return mMemoryCache.get(arg0);
        }

        @Override
        public void putBitmap(String arg0, Bitmap arg1) {
            if(getBitmap(arg0) == null){
                mMemoryCache.put(arg0, arg1);
            }
        }

    }
}
