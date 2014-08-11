package gis.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

public class CodeBoundaryCollector {

	final String url = "http://sgis.kostat.go.kr/OpenAPI2/adminUnitBoundary.do";

	BufferedWriter out;
	BufferedWriter jsonOut;

	/**
	 * 실제 실행부분.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run() throws IOException, InterruptedException {

		// file 오픈
		out = new BufferedWriter(new FileWriter("out.txt", true));
		jsonOut = new BufferedWriter(new FileWriter("json.txt", true));

		List<String> completedCode = readCodes("checkpoint");

		// 파일에서 읽어와서 실행 고고씽.
		for (String code : readCodes("codes.txt")) {
			// 작업한건지 체크한다.
			if (completedCode.contains(code)) {
				continue;
			}

			try {
				String result = get(code);
				jsonOut.write(result);
				jsonOut.newLine();
				parseCode(code, result);

				// 완료되면? 체크파일에 저장!
				checkpoint(code);
			} catch (IOException e) {
				Thread.sleep(10000);
			}
		}
		// file닫기
		out.close();
		jsonOut.close();

	}

	/**
	 * 작업한 파일은 집어넣는다.
	 * 
	 * @param code
	 * @throws IOException
	 */
	private void checkpoint(String code) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(
				"checkpoint.txt", true));
		out.write(code);
		out.newLine();
		out.close();
	}

	// 코드를 읽는다.
	public List<String> readCodes(String file) {
		List<String> codes = new ArrayList<String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String s;

			while ((s = in.readLine()) != null) {
				// System.out.println(s);
				codes.add(s);
			}
			in.close();
		} catch (IOException e) {
			System.err.println(e); // 에러가 있다면 메시지 출력
		}

		return codes;
	}

	/**
	 * json 파싱
	 * 
	 * @param json
	 * @return
	 */
	private void parseCode(String code, String json) {
		//System.out.println(json);
		// 서버로부터 받은 문자열을 json객체로.
		JSONParser jsonParser = new JSONParser();
		JSONObject obj;
		try {
			obj = (JSONObject) jsonParser.parse(json);
			JSONObject features = (JSONObject) ((JSONArray) obj.get("features")).get(0);
			JSONObject geometry = (JSONObject) features.get("geometry");
			JSONArray coordinates = (JSONArray) geometry.get("coordinates");
			
			// write to file
			out.write(code);
			out.write("\t");
			out.write(coordinates.toJSONString());
			out.newLine();
			
			System.out.println(code +"\t"+ coordinates.toJSONString());
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * html요청을 받아온다.
	 * 
	 * @param url
	 * @param nvps
	 * @return
	 * @throws IOException
	 */
	public String get(String code) throws IOException {

		// 타입.
		String step = "";
		if (code.length() == 2) {
			step = "1";
		} else if (code.length() == 5) {
			step = "2";
		} else if (code.length() == 7) {
			step = "3";
		}
		// 파라미터 조립
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("apikey", "ESGA2014070972683917")); // api-key
		nvps.add(new BasicNameValuePair("format", "geojson"));// 출력형식
		nvps.add(new BasicNameValuePair("step", step));// 검색유형
		nvps.add(new BasicNameValuePair("code", code));// 코드. 이거 계속 바뀜.
		String param = URLEncodedUtils.format(nvps, "ascii");

		// 요청준비
		CloseableHttpClient httpclient = HttpClients.createDefault();
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
			e.printStackTrace();
			throw e;
		} finally {

		}

		return sb.toString();
	}

	public void test() throws IOException {
		// file 오픈
		out = new BufferedWriter(new FileWriter("out.txt", true));

		// 파일에서 읽어와서 실행 고고씽.
		for (int i = 0; i < 1000; i++) {
			out.write(String.valueOf(i));
			out.newLine();
		}

		// file닫기
		out.close();
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {
		CodeBoundaryCollector dcc = new CodeBoundaryCollector();
		dcc.run();

		// dcc.readCodes();
		// dcc.write("test");
		// dcc.test();

	}

}