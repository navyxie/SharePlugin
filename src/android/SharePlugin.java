package com.kalengo.weathermeida.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.kalengo.weathermeida.R;
import com.kalengo.weathermeida.share.ShareContentCustomize;
import com.kalengo.weathermeida.util.UrlUtil;

/**
 * @author Administrator 分享插件
 */
public class SharePlugin extends CordovaPlugin {
	public static String SHARE = "share";
	long curr = 0;

	public boolean execute(String action, JSONArray data,
			CallbackContext callbackContext) throws JSONException {
		if (SHARE.equals(action)) {
			share(data.getString(0), data.getString(1), data.getString(2),
					data.getString(3), data.getString(4), callbackContext);
			return true;
		}

		return false;
	}

	public void share(final String title, final String text,
			final String imgUrl, final String url, final String type,
			final CallbackContext callbackContext) {
		curr = System.currentTimeMillis();
		UrlUtil.info("david", curr + "");
		final CordovaInterface cordova = this.cordova;
		this.cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (type.equals("imgUrl")) {
					showShare(false, null, title, text, imgUrl, url,
							callbackContext);
					ShareContentCustomize.isImgUrl = true;
				} else {
					GetandSaveCurrentImage(cordova.getActivity(), title, text,
							url, callbackContext);
					UrlUtil.info("david", System.currentTimeMillis() + "------"
							+ (System.currentTimeMillis() - curr));
					UrlUtil.info("but", android.os.Build.VERSION.SDK_INT + "");
				}
			}
		});

	}

	private void showShare(boolean silent, String platform, String title,
			String text, String imgUrl, String url,
			CallbackContext callbackContext) {
		final OnekeyShare oks = new OnekeyShare();
		oks.setNotification(R.drawable.logo, this.cordova.getActivity()
				.getString(R.string.app_name));
		oks.setTitle(title);
		oks.setTitleUrl(url);
		oks.setText(text + "点击进入 →");
		oks.setImageUrl(imgUrl);
		oks.setImagePath(imgUrl);
		oks.setUrl(url);
		oks.setComment(this.cordova.getActivity().getString(R.string.share));
		oks.setSite(this.cordova.getActivity().getString(R.string.app_name));
		oks.setSiteUrl(url);
		oks.setVenueName("天气美搭");
		oks.setVenueDescription("你的天气搭配专家");
		oks.setCallbackContext(callbackContext);

		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}
		// 去除注释，可令编辑页面显示为Dialog模式
		// oks.setDialogMode();
		// 去除注释，在自动授权时可以禁用SSO方式
		// oks.disableSSOWhenAuthorize();
		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		// oks.setCallback(new OneKeyShareCallback());
		oks.setShareContentCustomizeCallback(new ShareContentCustomize());
		// 去除注释，演示在九宫格设置自定义的图标
		// Bitmap logo = BitmapFactory.decodeResource(menu.getResources(),
		// R.drawable.ic_launcher);
		// String label = menu.getResources().getString(R.string.app_name);
		// OnClickListener listener = new OnClickListener() {
		// public void onClick(View v) {
		// String text = "Customer Logo -- ShareSDK " +
		// ShareSDK.getSDKVersionName();
		// Toast.makeText(menu.getContext(), text, Toast.LENGTH_SHORT).show();
		// oks.finish();
		// }
		// };
		// oks.setCustomerLogo(logo, label, listener);
		oks.show(this.cordova.getActivity());
	}

	/**
	 * 获取和保存当前屏幕的截图
	 */
	private void GetandSaveCurrentImage(Activity activity, String title,
			String text, String url, CallbackContext callbackContext) {
		// 1.构建Bitmap
		WindowManager windowManager = activity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight();
		Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		// 2.获取屏幕
		View decorview = activity.getWindow().getDecorView();
		decorview.setDrawingCacheEnabled(true);
		decorview.destroyDrawingCache();
		bmp = decorview.getDrawingCache();
		if (getSDCardPath() == null) {
			Toast.makeText(activity, "未发现可用的SD卡", Toast.LENGTH_SHORT).show();
			return;
		}
		String SavePath = getSDCardPath() + "/tqmd/screenshots";
		// 3.保存Bitmap
		try {
			File path = new File(SavePath);
			// 文件
			String filepath = null;
			filepath = SavePath + "/" + new Random().nextInt(10) + ".jpg";
			File file = new File(filepath);
			if (!path.exists()) {
				path.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			if (null != bos) {
				bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
				bos.flush();
				bos.close();
				fos.close();
				// Toast.makeText(activity,
				// "截屏文件已保存至SDCard"+filepath+"下",
				// Toast.LENGTH_LONG).show();
				showShare(false, null, title, text, filepath, url,
						callbackContext);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 获取SDCard的目录路径功能
	 */
	private String getSDCardPath() {
		File sdcardDir = null;
		// 判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdcardExist) {
			sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.toString();
		} else {
			return null;
		}

	}
}
