package com.cm.update;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.lk.event.Event;
import com.lk.event.utils.OKUtils;
import com.unity3d.player.UnityPlayer;

public class EventStat {
	private static EventStat _instance;
	private Class<?> TCAgentClazz = null;
	public EventStat(){
		try {
			TCAgentClazz = Class.forName("com.tendcloud.tenddata.TCAgent");
		} catch (ClassNotFoundException e) {
			Log.d("updater", "com.tendcloud.tenddata.TCAgent is not exit");
			TCAgentClazz = null;
		}
	}

	public void initTalkingdata(Activity context) {
		try {
			if (TCAgentClazz != null) {
				Method method = TCAgentClazz.getMethod("init", Context.class);
				method.invoke(TCAgentClazz, context);
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static final EventStat getInstance() {
		if (_instance == null) {
			_instance = new EventStat();
		}
		return _instance;
	}

	// android部分统计
	public void onEvent(Activity activity, String eventId, String eventPid, String eventDesc, String gameId, String extJson) {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			params.put("gameId", URLEncoder.encode(gameId, "utf-8"));
			params.put("eventId", URLEncoder.encode(eventId, "utf-8"));
			params.put("eventPid", URLEncoder.encode(eventPid, "utf-8"));
			params.put("eventDesc", URLEncoder.encode(eventDesc, "utf-8"));

			String deviceId = "";

			if ((!TextUtils.isEmpty(extJson)) && (extJson.contains("deviceId"))) {
				JSONObject json = new JSONObject(extJson);
				deviceId = json.getString("deviceId");
			} else {
				deviceId = getDeviceId(activity);
			}
			params.put("deviceId", deviceId);

			Log.d("updater", "eventId:" + eventId + " eventPid:" + eventPid + " eventDesc:" + eventDesc + " gameId:" + gameId);

			// TCAgent.onEvent(activity.getApplicationContext(), eventId,
			// "这个是用来细分的标志", params);
			String serverUrl = "";
			if ("135".equals(gameId)) {
				serverUrl = "http://113.196.89.69:8081/dataCenter/eventServlet";
			}
			Event.getInstance().eventNodeCollection(activity, eventId, eventPid, eventDesc, gameId, "0", "0", "0", "0", serverUrl);
			talkingDataOnEvent(activity.getApplicationContext(), eventId, params);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("Event", "Please make sure gameId and roleLevel are int type!");
		}
	}

	// unity统计
	public void onEvent(String eventId, String eventPid, String eventDesc, String gameId, String serverId, String userId, String roleId, String roleLevel, String extJson) {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			params.put("gameId", URLEncoder.encode(gameId, "utf-8"));
			params.put("roleLevel", URLEncoder.encode(roleLevel, "utf-8"));
			params.put("eventId", URLEncoder.encode(eventId, "utf-8"));
			params.put("eventPid", URLEncoder.encode(eventPid, "utf-8"));
			params.put("eventDesc", URLEncoder.encode(eventDesc, "utf-8"));
			params.put("serverId", URLEncoder.encode(serverId, "utf-8"));
			params.put("userId", URLEncoder.encode(userId, "utf-8"));
			params.put("roleId", URLEncoder.encode(roleId, "utf-8"));
			String deviceId = "";
			if ((!TextUtils.isEmpty(extJson)) && (extJson.contains("deviceId"))) {
				JSONObject json = new JSONObject(extJson);
				deviceId = json.getString("deviceId");
			} else {
				deviceId = getDeviceId();
			}
			params.put("deviceId", deviceId);
			Log.d("updater", "eventId:" + eventId + " eventPid:" + eventPid + " eventDesc:" + eventDesc + " gameId:" + gameId);
			// TCAgent.onEvent(UnityPlayer.currentActivity, eventId,
			// "这个是用来细分的标志", params);
			String serverUrl = "";
			if ("135".equals(gameId)) {
				serverUrl = "http://113.196.89.69:8081/dataCenter/eventServlet";
			}
			Event.getInstance().eventNodeCollection(eventId, eventPid, eventDesc, gameId, serverId, userId, roleId, roleLevel, serverUrl);
			talkingDataOnEvent(UnityPlayer.currentActivity, eventId, params);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("Event", "Please make sure gameId and roleLevel are int type!");
		}
	}

	private final String getDeviceId() {
		return OKUtils.id(getUnityCurrentActivity());
	}

	private final String getDeviceId(Activity activity) {
		return OKUtils.id(activity);
	}

	private Activity getUnityCurrentActivity() {
		Activity activity = null;
		try {
			Class<?> unityPlayer = Class.forName("com.unity3d.player.UnityPlayer");
			Field currentActivity = unityPlayer.getField("currentActivity");
			activity = (Activity) currentActivity.get(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalStateException("not in unity environment!");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new IllegalStateException("Unity is not offical!");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		if (activity == null) {
			throw new IllegalStateException("Please call init function after unity has been launched!");
		}

		return activity;
	}

	private void talkingDataOnEvent(Context activity, String eventId, Map<String, Object> params) {
		if (TCAgentClazz != null) {
			Method method;
			try {
				method = TCAgentClazz.getMethod("onEvent", Context.class, String.class, String.class, Map.class);
				method.invoke(null, activity, eventId, "这个是用来细分的标志", params);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			Log.d("updater", "TCAgentClazz is null,TalkingData jar not exist");
		}
	}
}
