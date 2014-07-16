package gis.util;

import gis.dao.GisDao;
import gis.obj.DetailCodeInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class DetailCodeInfoParser {

	@Autowired
	GisDao dao;

	BufferedReader in;

	/**
	 * 파일을 읽을 준비를 한 다음. 한줄씩 실행!
	 */
	public void openFile(String file) {
		try {
			in = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			System.err.println(e); // 에러가 있다면 메시지 출력
		}
	}

	/**
	 * json을 파싱해서 객체로 만든다.
	 * 
	 * @param jsonString
	 * @return
	 */
	public List<DetailCodeInfo> parseJson(String jsonString) {
		List<DetailCodeInfo> infoList = new ArrayList<DetailCodeInfo>();
		JSONParser jsonParser = new JSONParser();
		JSONObject obj;
		try {
			obj = (JSONObject) jsonParser.parse(jsonString);
			JSONArray features = (JSONArray) obj.get("features");
			for (Object o : features) {
				DetailCodeInfo info = new DetailCodeInfo();
				JSONObject feature = (JSONObject) o;
				JSONObject geometry = (JSONObject) feature.get("geometry");
				JSONArray coordinates = (JSONArray) geometry.get("coordinates"); // 경계값
																					// 배열.

				JSONObject properties = (JSONObject) feature.get("properties");

				String Description = (String) properties.get("Description"); // 주소.
				String Name = (String) properties.get("Name"); // 코드번호.

				// 자료담기.
				info.setCode(Name);
				info.setAddr(Description);
				info.setCoord_array_string(coordinates.toJSONString());

				infoList.add(info);

			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		return infoList;
	}

	/**
	 * 중심정보를 계산해 넣는다.
	 * 
	 * @param info
	 */
	public void calCenter(DetailCodeInfo info) {

		JSONParser jsonParser = new JSONParser();
		JSONArray coordArray;
		try {
			coordArray = (JSONArray) jsonParser.parse(info
					.getCoord_array_string());
			coordArray = (JSONArray) coordArray.get(0); // 배열이 한번 씌워져있기 때문에.

			//센터를 계산한다!
			int avgX = 0;
			int avgY = 0;
			int cnt = 0;
			for (; cnt < coordArray.size(); cnt++) {
				JSONArray coord = (JSONArray) coordArray.get(cnt);
				// System.out.println(coord);
				Long x = (Long) coord.get(0); // x
				Long y = (Long) coord.get(1); // y
				Integer cx = Integer.parseInt(Long.toString(x));
				Integer cy = Integer.parseInt(Long.toString(y));
				avgX += cx;
				avgY += cy;
			}
			
			avgX /= cnt;
			avgY /= cnt;
			
			//중심 저장.
			info.setCenter_x(avgX);
			info.setCenter_y(avgY);

		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (ClassCastException cce) {
			writeError(info, "error.txt");
		}

	}

	/**
	 * 에러를 기록한다.
	 * 
	 * @param info
	 * @param file
	 */
	private void writeError(DetailCodeInfo info, String file) {
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

	/**
	 * 실행로직
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		// 파일을 준비한다.
		openFile("json.txt");

		// 한줄씩 실행한다.
		String s;
		while ((s = in.readLine()) != null) {
			List<DetailCodeInfo> infoList = parseJson(s);
			for (DetailCodeInfo info : infoList) {
				calCenter(info);

				try {
					dao.insertDetailCodeInfo(info);
				} catch (DataAccessException e) {
					writeError(info, "db_error.txt");
				}

				// 로깅!
				System.out.println(info);
			}

		}

		// 닫는다.
		in.close();

	}
}
