package com.example.kekeo_gimun;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class RestClient {

	public static String getMessages(int roomId) {
		HttpContext localContext = new BasicHttpContext();
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet("http://kekeo-gimun.appspot.com/messages?room_id=" + roomId);
		try {
				HttpResponse response = client.execute(get, localContext);
				String result = EntityUtils.toString(response.getEntity());
				return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void sendMessages(int userId, String content, int roomId) {
		HttpContext localContext = new BasicHttpContext();
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://kekeo-gimun.appspot.com/messages");
		
		ArrayList<NameValuePair> NameValuePairs = new ArrayList<NameValuePair>();
		NameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(userId)));
		NameValuePairs.add(new BasicNameValuePair("content", content));
		NameValuePairs.add(new BasicNameValuePair("room_id", String.valueOf(roomId)));
		
		try {
				HttpEntity entity = new UrlEncodedFormEntity(NameValuePairs, "UTF-8");
				post.setEntity(entity);
				HttpResponse response = client.execute(post, localContext);
				String result = EntityUtils.toString(response.getEntity());
				System.out.println(result);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
