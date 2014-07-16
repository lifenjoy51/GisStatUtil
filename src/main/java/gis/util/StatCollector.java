package gis.util;

import gis.dao.GisDao;
import gis.obj.DetailCodeInfo;
import gis.obj.StatInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class StatCollector {

	@Autowired
	GisDao dao;

	final String url = "http://sgis.kostat.go.kr/msgis/getResultByPoint.do";

	Pattern p = Pattern.compile("[0-9]+");

	/**
	 * json을 파싱해서 객체로 만든다.
	 * 
	 * @param jsonString
	 * @return
	 */
	public StatInfo parseJson(String jsonString, String item) {
		StatInfo info = new StatInfo();
		JSONParser jsonParser = new JSONParser();
		JSONObject obj;
		try {
			obj = (JSONObject) jsonParser.parse(jsonString);
			JSONArray features = (JSONArray) obj.get("features");
			JSONObject feature = (JSONObject) features.get(0); // 배열.

			JSONObject properties = (JSONObject) feature.get("properties");

			String Name = (String) properties.get("Name"); // 코드번호.
			String ItemValue = (String) properties.get("ItemValue"); // 개수.
			// String BaseYear = (String) properties.get("BaseYear"); //
			// 기준년도.

			// 개수 처리.
			String cnt = getCnt(ItemValue);

			// 자료담기.
			info.setCode(Name);
			info.setItem(item);
			info.setCnt(cnt);

		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		return info;
	}

	private String getCnt(String itemValue) {
		System.out.println(itemValue);
		if (itemValue.contains("N/A")) {
			return "0";
		} else {
			Matcher m = p.matcher(itemValue);

			if (m.find()) {
				return m.group(0);
			} else {
				return "0";
			}

		}
	}

	/**
	 * html요청을 받아온다.
	 * 
	 * @param url
	 * @param nvps
	 * @return
	 */
	public String get(String posX, String posY, String item) {
		// 파라미터 조립
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("point", "POINT(" + posX + " " + posY
				+ ")")); // 위치정보.
		nvps.add(new BasicNameValuePair("item", item));// 종류. 1-55.
		String param = URLEncodedUtils.format(nvps, "ascii");

		// 요청준비
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url + "?" + param);

		// 헤더 조정.
		httpGet.setHeader("Host", "sgis.kostat.go.kr");
		httpGet.setHeader("Referer", "http://sgis.kostat.go.kr/msgis/index.vw");
		httpGet.setHeader(
				"Cookie",
				"JSESSIONID=BdKZjrJbwfBj6ykdokK5twDNZP48baCjGPDalEl4EKlINQOuSxLn1CMLG85nFmun.GSKSWAS2_servlet_engine1");
		// 쿠기값은 적절히 바꿔줌.

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
			e.printStackTrace();
		} finally {

		}

		return sb.toString();
	}
	

	/**
	 * 에러를 기록한다.
	 * 
	 * @param info
	 * @param file
	 */
	private void writeError(StatInfo info, String file) {
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(file, true));
			out.write(info.toString());
			out.newLine();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		String upCode = "11";	//서울만!
		List<DetailCodeInfo> infoList = dao.getDetailCodeList(upCode);
		for (DetailCodeInfo info : infoList) {
			String posX = info.getCenter_x().toString();
			String posY = info.getCenter_y().toString();

			for (int item = 1; item <= 55; item++) {
				String result = get(posX, posY, String.valueOf(item));
				StatInfo statInfo = parseJson(result, String.valueOf(item));
				
				//디비에 저장.
				try {
					dao.insertStatInfo(statInfo);
				} catch (DataAccessException e) {
					writeError(statInfo, "stat_db_error.txt");
				}
				
			}

		}
		// String result = get("191808", "443030", "1");
		// StatInfo info = parseJson(result, "1");
		// System.out.println(info);
	}

}
