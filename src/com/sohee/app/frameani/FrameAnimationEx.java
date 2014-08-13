package com.sohee.app.frameani;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class FrameAnimationEx {
	final String TAG = "FrameAnimationEx";

	final int MSG_EVENT_DRAW = 1;

	ArrayList<AniInfo> mAniInfo = new ArrayList<AniInfo>();
	ImageView mImage = null;
	Thread mThread = null;
	int mCurrImg = 0;
	boolean mRunning = false;
	boolean mOneshot = true;

	public FrameAnimationEx(Context c, ImageView v, int res){
		mImage = v;
		Resources r = c.getResources();

		XmlResourceParser xpp = r.getXml(res);
		try {
			xpp.next();

			String elementName = null;
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				elementName = xpp.getName();

				if(eventType == XmlPullParser.START_DOCUMENT) {
					Log.d(TAG, "parse ani xml");
				}
				else if(eventType == XmlPullParser.START_TAG) {
					Log.d(TAG, "start tag, elementName:" + elementName);
					if("item".equalsIgnoreCase(elementName)){
						int attCnt = xpp.getAttributeCount();
						if(attCnt > 0){
							String drawable = null;
							String duration = null;
							for(int i=0; i<attCnt; i++){
								String attName = xpp.getAttributeName(i);
								if("drawable".equalsIgnoreCase(attName)){
									drawable = xpp.getAttributeValue(i);
								} else if("duration".equalsIgnoreCase(attName)){
									duration = xpp.getAttributeValue(i);
								}
							}
							Log.d(TAG, "add aniInfo, drawable:" + drawable + ", duration:" + duration);
							String d = drawable.substring(1);
							AniInfo aniInfo = new AniInfo(Integer.parseInt(d), Integer.parseInt(duration));
							mAniInfo.add(aniInfo);
						}
					} else if("animation-list".equalsIgnoreCase(elementName)){
						int attCnt = xpp.getAttributeCount();
						if(attCnt > 0){
							String value = null;
							for(int i=0; i<attCnt; i++){
								String attName = xpp.getAttributeName(i);
								if("oneshot".equalsIgnoreCase(attName)){
									value = xpp.getAttributeValue(i);
								} 
							}
							Log.d(TAG, "add aniInfo, oneshot:" + value);
							mOneshot = "false".equalsIgnoreCase(value) ? false : true; 
						}
					}
				}
				else if(eventType == XmlPullParser.END_TAG) {
					Log.d(TAG, "end tag, elementName:" + elementName);
				}
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(){
		if(mAniInfo.size() > 0){
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(mRunning){
						if(mRunning == false){
							break;
						}

						if(mCurrImg == mAniInfo.size() ){
							if(mOneshot == false){
								mCurrImg = 0;
								Message msg = Message.obtain(mHandler, MSG_EVENT_DRAW, 0, 0);
								mHandler.sendMessage(msg);
								try {
									AniInfo aniInfo = mAniInfo.get(mCurrImg);
									Thread.sleep(aniInfo.mDuration);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}else{
								if(mThread != null){
									mThread.interrupt();
									mThread = null;
								}
								mRunning = false;
								break;
							}
						} else if(mCurrImg < mAniInfo.size()){ 
							Message msg = Message.obtain(mHandler, MSG_EVENT_DRAW, 0, 0);
							mHandler.sendMessage(msg);
							try {
								if(mCurrImg < mAniInfo.size()){
									AniInfo aniInfo = mAniInfo.get(mCurrImg);
									Thread.sleep(aniInfo.mDuration);
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (IndexOutOfBoundsException e){
								e.printStackTrace();
							}
						}
					}
				}
			});

			mRunning = true;
			mThread.start();
		}
	}

	public void stop(){
		if(mThread != null){
			mThread.interrupt();
			mThread = null;
		}
		mRunning = false;
	}

	public boolean isRunning(){
		return mRunning;
	}

	private class AniInfo{
		int mDrawable;
		int mDuration;

		public AniInfo(int drawable, int duration){
			mDrawable = drawable;
			mDuration = duration;
		}
	}

	class DurationTimer extends TimerTask {
		@Override
		public void run() {
			if(mRunning == false){
				return;
			}

			if(mCurrImg == mAniInfo.size() ){
				if(mOneshot == false){
					mCurrImg = 0;
					Message msg = Message.obtain(mHandler, MSG_EVENT_DRAW, 0, 0);
					mHandler.sendMessage(msg);
				}else{
				}
			} else { 
				Message msg = Message.obtain(mHandler, MSG_EVENT_DRAW, 0, 0);
				mHandler.sendMessage(msg);
			}
		}
	}

	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_EVENT_DRAW:
				if(mRunning && mCurrImg < mAniInfo.size()){
					AniInfo aniInfo = mAniInfo.get(mCurrImg);
					mImage.setBackgroundResource(aniInfo.mDrawable);
					mCurrImg++;
				}
				break;
			}
		}
	};
}