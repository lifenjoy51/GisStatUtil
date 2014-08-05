package gis.util;

import gis.obj.CompInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class CompCollector {

	@Autowired
	SqlSessionFactory sqlSessionFactory;
	final int BATCH_SIZE = 100;

	final String url = "http://sgis.kostat.go.kr/msgis/getCompList.do";
	String cookie;
	List<String> codeList;
	List<String> compResult;

	public CompCollector() {
		codeList = new ArrayList<String>();
		compResult = new ArrayList<String>();
	}

	/**
	 * json을 파싱해서 객체로 만든다.
	 * 
	 * @param jsonString
	 * @return
	 * @throws ParseException
	 */
	public List<CompInfo> parseJson(String jsonString, String code, String item)
			throws ParseException {
		List<CompInfo> list = new ArrayList<CompInfo>();
		JSONParser jsonParser = new JSONParser();
		JSONObject obj = null;
		System.out.println(code + ", " + item);
		try {
			obj = (JSONObject) jsonParser.parse(jsonString);
		} catch (ParseException pe) {
			System.err.println(jsonString);
			ProxyManager.setProxy();
		}
		try {
			JSONArray compList = (JSONArray) obj.get("compList");
			for (int i = 0; i < compList.size(); i++) {
				JSONObject comp = (JSONObject) compList.get(i); // 사업체 정보

				String comp_nm = (String) comp.get("comp_nm"); // name
				String number = (String) comp.get("number"); // ?
				String ufid = (String) comp.get("ufid"); // ufid
				String x = (String) comp.get("x"); // x
				String y = (String) comp.get("y"); // y

				CompInfo info = new CompInfo(code, item, comp_nm, number, ufid,
						x, y);
				list.add(info);
			}
		} catch (Exception e) {
			System.err.println(jsonString);
			// 연결 안되면 프록시 재설정~
			if(!jsonString.contains("noData")){
				ProxyManager.setProxy();
				getCookie();
			}else{
				CompInfo info = new CompInfo(code, item, "empty", "1", "0",
						"0", "0");
				list.add(info);
			}

		}

		return list;
	}

	/**
	 * html요청을 받아온다.
	 * 
	 * @param url
	 * @param nvps
	 * @return
	 * @throws InterruptedException
	 */
	public String get(String code, String item) throws InterruptedException {
		// 요청 날릴 때 인간처럼 보이게 한다.
		// 1. 랜덤하게 쉬기.
		long sleep = (long) (Math.random() * 5000) + 2000;
		// 새벽시간대에는 좀 더 빠르게!
		int hour = Calendar.getInstance(Locale.KOREA).get(Calendar.HOUR_OF_DAY);
		if (hour > 3 && hour < 9) {
			sleep = sleep / 3;
		}
		System.err.println("sleep!!" + sleep);
		Thread.sleep(sleep);

		// 2. 랜덤하게 아이피 바꾸기.
		if (Math.random() * 100 < 3) {
			System.err.println("ProxyManager.setProxy");
			//ProxyManager.setProxy();
			getCookie();
		}

		// 파라미터 조립
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("code", code));// 종류. 1-55.
		nvps.add(new BasicNameValuePair("item", item));// 종류. 1-55.
		nvps.add(new BasicNameValuePair("type", "code"));// 종류. 1-55.
		// nvps.add(new BasicNameValuePair("apikey", apikey));// apikey
		String param = URLEncodedUtils.format(nvps, "ascii");

		// 요청준비
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url + "?" + param);

		// 헤더 조정.
		httpGet.setHeader("Host", "sgis.kostat.go.kr");
		httpGet.setHeader("Referer", "http://sgis.kostat.go.kr/msgis/index.vw");
		// httpGet.setHeader("User-Agent",
		// "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
		httpGet.setHeader("Cookie", cookie);
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
			System.err.println(e.getMessage());
			// 연결 안되면 프록시 재설정~
			ProxyManager.setProxy();
		} finally {

		}

		return sb.toString();
	}

	private void getCookie() {
		try {
			System.out.println("getCookie");
			URL url = new URL("http://sgis.kostat.go.kr/msgis/index.vw");
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(1 * 1000);
			conn.setReadTimeout(3 * 1000);
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
			conn.connect();

			// 쿠키 저장
			try {
				System.out.println("saveCookie");
				cookie = conn.getHeaderField("Set-Cookie");
				System.out.println("cookie1 >> " + cookie);
				cookie = cookie.substring(0, cookie.indexOf(";"));
				System.out.println("cookie2 >> " + cookie);

				if (cookie == null) {
					System.err.println("cookie is null");
					// 연결 안되면 프록시 재설정~
					ProxyManager.setProxy();
					getCookie();
				}
			} catch (NullPointerException npe) {
				System.err.println("cookie is NullPointerException ");
				// 연결 안되면 프록시 재설정~
				ProxyManager.setProxy();
				getCookie();
			}

			System.out.println(cookie);
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

	}

	public void test() throws ParseException, IOException, InterruptedException {
		
		//작업한 결과값들 로딩
		readCompResult();

		// 시군구 배열에서...
		// 파일 읽고
		readCodeList();

		// 쿠키 얻고.
		getCookie();
		
		//나와야하는 모든 경우의 수
		List<String> entireCase =new ArrayList<String>();

		// 코드 하나씩?!
		for (String code : codeList) {
			for (int item = 19; item <= 55; item++) {
				entireCase.add(code + "\t" + String.valueOf(item));
				//System.out.println(code + "\t" + String.valueOf(item));
				
				//이건 처음 작업할 때 사용.
				//getCompInfo(code, String.valueOf(item));
			}
		}
		
		//작업안된거 찾기.
		entireCase.removeAll(compResult);
		
		for(String c : entireCase){
			System.out.println(c);
			String code = c.split("\t")[0];
			String item = c.split("\t")[1];
			getCompInfo(code, item);
		}

	}

	private void readCompResult() throws IOException {
		File codeListFile = new File("comp_result.txt");
		// 파일내용을 읽어서.
		Reader r = new FileReader(codeListFile);
		BufferedReader br = new BufferedReader(r);

		// 한줄마다 데이터를 끊어서
		String line = null;
		while ((line = br.readLine()) != null) {
			compResult.add(line);
		}

		br.close();
		
	}

	private void getCompInfo(String code, String item) throws ParseException,
			InterruptedException {
		// 세션연결
		SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH,
				false);
		int commit = 0;

		String result = get(code, item + ",1,1,1");
		// System.out.println(result);
		for (CompInfo c : parseJson(result, code, item)) {
			// 디비에 저장.
			try {
				session.insert("gis.dao.GisDao.insertCompInfo", c);
				if (++commit % BATCH_SIZE == 0) {
					session.commit();
				}
			} catch (DataAccessException dae) {
				// System.err.println(dae.getMessage());
			} catch (PersistenceException bee) {
				// System.err.println(bee.getMessage());
			}

		}

		try {
			// 커밋
			session.commit();
			session.close();
		} catch (DataAccessException dae) {
			// System.err.println(dae.getMessage());
		} catch (PersistenceException bee) {
			// System.err.println(bee.getMessage());
		}
	}

	public void readCodeList() throws IOException {
		File codeListFile = new File("sgg_code.txt");
		// 파일내용을 읽어서.
		Reader r = new FileReader(codeListFile);
		BufferedReader br = new BufferedReader(r);

		// 한줄마다 데이터를 끊어서
		String line = null;
		while ((line = br.readLine()) != null) {
			codeList.add(line.split("	")[0]);
		}

		br.close();

	}

}
