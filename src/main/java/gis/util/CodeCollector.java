package gis.util;

import java.io.BufferedReader;
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
 
public class CodeCollector {
 
    final String url = "http://sgis.kostat.go.kr/OpenAPI2/getAddress.do";
 
    /**
     * 실제 실행부분.
     */
    public void run() {
 
        String code = "1";
        request(code);
        // 요청을 보내고. 출력하는 로직. 이걸 자식이 없을 때 까지 계속 해야함.
 
    }
 
    /**
     * 요청을 보낸다.
     * 
     * @param code
     */
    public void request(String code) {
        String json = get(code);
        System.out.println(json);
        if (json.contains("errorCode")) {
            return;
        } else {
            		// 파싱해서 여러번 보냄.
            List<String> codes = parseCode(json);
            for (String cd : codes) {
                request(cd);
            }
        }
    }
 
    /**
     * json 파싱
     * 
     * @param json
     * @return
     */
    private List<String> parseCode(String json) {
        List<String> list = new ArrayList<String>();
        // 서버로부터 받은 문자열을 json객체로.
        JSONParser jsonParser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) jsonParser.parse(json);
            JSONArray array = (JSONArray) obj.get("data");
            for (Object o : array) {
                JSONObject kv = (JSONObject) o;
                String code = (String) kv.get("code");
                String name = (String) kv.get("name");
                 
                //하위코드를 구하기위해 저장.
                if(code.length()<7){
                    list.add(code);
                }
                System.out.println(code + "," + name);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
 
        return list;
    }
 
    /**
     * html요청을 받아온다.
     * 
     * @param url
     * @param nvps
     * @return
     */
    public String get(String code) {
        // 파라미터 조립
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("apikey", "ESGA2014070972683917")); // api-key
        nvps.add(new BasicNameValuePair("code", code));// 코드. 이거 계속 바뀜.
        nvps.add(new BasicNameValuePair("base_year", "2012"));// 기준년도
        String param = URLEncodedUtils.format(nvps, "ascii");
 
        // 요청준비
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url + "?" + param);
 
        /*
         * // 헤더 조정. httpGet.setHeader("Host", "sgis.kostat.go.kr");
         * httpGet.setHeader("Referer",
         * "http://sgis.kostat.go.kr/OpenAPI2/wizard.vw"); // 쿠기값은 적절히 바꿔줌.
         * httpGet.setHeader( "Cookie",
         * "JSESSIONID=ErsBgYZ6gAYVgCMLSzr1ucuKRb3TbaZohLjJoFVpX5aa4nsjS28isNxZaRigm0Pt.GSKSWAS2_servlet_engine1"
         * );
         */
 
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
     * @param args
     */
    public static void main(String[] args) {
        CodeCollector cc = new CodeCollector();
        cc.run();
 
    }
 
}