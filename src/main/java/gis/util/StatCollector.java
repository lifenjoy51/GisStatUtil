package gis.util;

import gis.dao.GisDao;
import gis.obj.DetailCodeInfo;
import gis.obj.StatInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class StatCollector {
	
	@Autowired
	@Qualifier("gisBatchDao")
	GisDao gisBatchDao;

	@Autowired
	@Qualifier("gisDao")
	GisDao dao;
	
	@Autowired
	SqlSessionFactory sessionFactoryBean;

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
			System.out.println(jsonString);
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
		nvps.add(new BasicNameValuePair("apikey", "key"));// apikey
		String param = URLEncodedUtils.format(nvps, "ascii");

		// 요청준비
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url + "?" + param);

		// 헤더 조정.
		httpGet.setHeader("Host", "sgis.kostat.go.kr");
		httpGet.setHeader("Referer", "http://sgis.kostat.go.kr/msgis/index.vw");
		//httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
		httpGet.setHeader(
				"Cookie",
				"JSESSIONID=y5yfrMkjaPN9qGkQ9aI16im3hDQ4OPnTHGRuwpp1las6fZ3tSsHOaiKyc1NSveej.GSKSWAS2_servlet_engine1");
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


	public void run() throws InterruptedException {
		// 작업 안한거 골라오고.
		String code = dao.getRemainCode();

		// 내가 누군지
		String worker = System.getProperty("user.name");
		String ip = "localhost";
		try {
			ip = InetAddress.getLocalHost().getHostName();
			worker = worker + "@" + ip;
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		// 작업중으로 업데이트
		dao.updateStatus(new DetailCodeInfo(code, worker, "W"));
		
		//배치 인서트를 위한 매퍼
		//SqlSession sqlSession = sessionFactoryBean.openSession(ExecutorType.BATCH);
		//GisDao gisDao = sqlSession.getMapper(GisDao.class);

		//해당 코드만 가져오기.
		List<DetailCodeInfo> infoList = dao.getDetailCodeList(code);
		for (DetailCodeInfo info : infoList) {
			//System.out.println(info);
			System.out.println(info.getCode());
			try{
			String posX = info.getCenter_x().toString();
			String posY = info.getCenter_y().toString();

			for (int item = 1; item <= 55; item++) {
				String result = get(posX, posY, String.valueOf(item));
				StatInfo statInfo = parseJson(result, String.valueOf(item));

				// 디비에 저장.
				try {
					gisBatchDao.insertStatInfo(statInfo);
					//System.out.println(statInfo);
				} catch (DataAccessException e) {
					//writeError(statInfo, "stat_db_error.txt");
				}

			}

			// 쉬었다가 하자
			Thread.sleep(100);
			}catch(NullPointerException ne){
				continue;
			}

		}
		
		//배치 커밋
		//sqlSession.flushStatements();
		//sqlSession.commit();
		//sqlSession.close();

		// 작업완료 업데이트.
		dao.updateStatus(new DetailCodeInfo(code, worker, "Y"));
	}

}
