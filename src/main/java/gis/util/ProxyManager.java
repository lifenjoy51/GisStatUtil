package gis.util;

import gis.obj.StatInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ProxyManager {

	/**
	 * html요청을 받아온다.
	 * 
	 * @param url
	 * @param nvps
	 * @return
	 */
	public static String getServerList() {
		//프록시 초기화
		System.setProperty("http.proxyHost", "");

		// 파라미터 조립
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("key", "f0e85763252cd9b246862a9c"));// api-key
		nvps.add(new BasicNameValuePair("ps", "http"));// type
		nvps.add(new BasicNameValuePair("as", "tp,ap,dp,hap"));// type = tp, ap, dp, hap
		String param = URLEncodedUtils.format(nvps, "ascii");

		// 요청준비
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = "http://letushide.com/fpapi/";
		HttpGet httpGet = new HttpGet(url + "?" + param);

		// 출력.
		StringBuffer sb = new StringBuffer();

		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();

			InputStream is = entity.getContent();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			// 문자열 더하기
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			EntityUtils.consume(entity);
			response.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {

		}

		return sb.toString();
	}

	/**
	 * json을 파싱해서 객체로 만든다.
	 * 
	 * @param jsonString
	 * @return
	 */
	public static void parseJson(String jsonString) {
		StatInfo info = new StatInfo();
		JSONParser jsonParser = new JSONParser();
		JSONObject obj;
		List<JSONObject> serverList = new ArrayList<JSONObject>();
		try {
			System.out.println(jsonString);
			obj = (JSONObject) jsonParser.parse(jsonString);
			JSONArray data = (JSONArray) obj.get("data");
			for (int i = 0; i < data.size(); i++) {
				// 하나씩 어떻게 체크?
				JSONObject server = (JSONObject) data.get(i); // proxy서버 정보.
				serverList.add(server);
			}
			
			//섞어섞어!!!
			Collections.shuffle(serverList);
			
			//프록시 선택~
			for(JSONObject server : serverList){

				String host = (String) server.get("host");
				String port = (String) server.get("port");

				// 접속테스트.
				if (isLive(host, port)) {
					System.out.format("%s : %s is OK!! \n", host, port);
					return;
				}else{
					System.out.format("%s : %s is ERROR.... \n", host, port);
				}
			}

		} catch (ParseException pe) {
			System.err.println(pe.getMessage());
			//pe.printStackTrace();
		}
		
		//프록시 모두 실패할 경우
		System.setProperty("http.proxyHost", "");
	}

	/**
	 * 서버가 살아있는지 확인한다.
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	private static boolean isLive(String host, String port) {
		int code = 0;
		try {
			System.out.println("isLive");
			
			System.setProperty("http.proxyHost", host);
			System.setProperty("http.proxyPort", port);
			
			URL url = new URL("http://google.com");

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(1*1000);
			connection.setReadTimeout(1*1000);
			connection.connect();
			
			System.out.println("getResponseCode");

			code = connection.getResponseCode();
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
			//e.printStackTrace();
		} catch (ProtocolException e) {
			System.err.println(e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			//e.printStackTrace();
		}

		if (code == 200) {
			return true;
		} else {
			return false;
		}

	}
	
	public static void setProxy(){
		//System.setProperty("http.proxyHost", "");
		String psJson = getServerList();
		parseJson(psJson);
	}

	public static void main(String[] args) {
		ProxyManager pt = new ProxyManager();
		System.out.println("getServerList");
		String psJson = pt.getServerList();
		System.out.println("parseJson");
		pt.parseJson(psJson);
		System.out.println("proxy end");
	}

}
