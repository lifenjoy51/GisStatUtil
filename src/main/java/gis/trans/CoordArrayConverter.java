package gis.trans;

import gis.dao.GisDao;
import gis.obj.CodeInfo;
import gis.util.GeoConverter;
import gis.util.PointF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class CoordArrayConverter {

	private SqlSession session;
	final int BATCH_SIZE = 100;
	int commit = 0;

	@Autowired
	@Qualifier("gisDao")
	GisDao dao;

	@Autowired
	SqlSessionFactory sqlSessionFactory;

	// 모든 동의 위치정보를 담고
	List<CodeInfo> codeInfoList;

	// 처리할 작업 큐.
	Queue<CodeInfo> compQueue;

	public CoordArrayConverter() {
		this.codeInfoList = new ArrayList<CodeInfo>();
		compQueue = new ConcurrentLinkedQueue<CodeInfo>();
	}

	public void run() throws IOException, InterruptedException {
		// 코드를 읽어오고
		codeInfoList = dao.getCodeInfoListAll();
		// 이 코드들이...

		// 회사들은 현재

		// 세션연결
		session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);

		// thread pool
		ExecutorService executor = Executors.newFixedThreadPool(8);

		// 지역코드 하나씩 작업.
		for (CodeInfo ci : codeInfoList) {
			Runnable w = new CodeInfoCoordWorker(ci);
			executor.execute(w);
		}
		executor.shutdown();

		while (!executor.isTerminated() || !compQueue.isEmpty()) {
			if (compQueue.isEmpty()) {
				Thread.sleep(1);
				continue;
			}

			CodeInfo c = compQueue.poll();
			try {
				System.out.println(commit);
				session.insert("gis.dao.GisDao.insertCodeInfoMod", c);
				if (++commit % BATCH_SIZE == 0) {
					session.commit();
				}
			} catch (DataAccessException dae) {
				System.err.println(dae.getMessage());
			} catch (PersistenceException bee) {
				System.err.println(bee.getMessage());
			}
		}

		System.err.println("Finished all threads");

		// 커밋
		try {
			session.commit();
			session.close();
		} catch (DataAccessException dae) {
			System.err.println(dae.getMessage());
		} catch (PersistenceException bee) {
			System.err.println(bee.getMessage());
		}

	}

	private class CodeInfoCoordWorker extends Thread {
		CodeInfo ci;

		public CodeInfoCoordWorker(CodeInfo ci) {
			this.ci = ci;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// json 배열을 읽는다.
			String orgJsonArrayString = ci.getCoord_array();
			JSONParser jsonParser = new JSONParser();
			JSONArray orgJsonArray;
			try {
				orgJsonArray = (JSONArray) jsonParser.parse(orgJsonArrayString);

				JSONArray newJsonArray = new JSONArray();
				// 공간정보가 여러개일 수도 있다.
				// [[[이렇게 3개로 시작하는 경우, [[[[이렇게 4개로 시작하는 경우로 나뉜다.

				if (orgJsonArrayString.startsWith("[[[[")) {
					// 4개짜리.
					// [
					// [
					// [
					// 배열
					// ]
					// ],
					// [
					// [
					// 배열
					// ]
					// ]
					// ]
					for (int i = 0; i < orgJsonArray.size(); i++) {
						JSONArray array = (JSONArray) orgJsonArray.get(i);
						JSONArray newArray = convert(array);
						newJsonArray.add(newArray);
					}
				} else {
					// 3개짜리.
					// [
					// [
					// 배열
					// ]
					// ]
					JSONArray newArray = convert(orgJsonArray);
					newJsonArray.add(newArray);
				}

				// 하나씩 좌표를 바꾼다.
				// 다시 json배열로 묶는다.
				ci.setCoord_array(newJsonArray.toJSONString());
				// 그다음에 변환한놈을 큐에 넣는다.

				// 작업할 큐에 넣는다.
				compQueue.add(ci);
				System.out.println(ci);

			} catch (NullPointerException npe) {
				System.err.println("null");

			} catch (ParseException pe) {
				pe.printStackTrace();
			}
		}

		/**
		 * 파싱해서, 좌표정보를 변환한 후에, 스트링으로 다시 묶는다.
		 * 
		 * @param orgJsonArray
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private JSONArray convert(JSONArray orgJsonArray) {
			// [
			// [
			// 배열
			// ]
			// ]
			JSONArray arrayOne = (JSONArray) orgJsonArray.get(0);
			//System.out.println("arrayOne = "+arrayOne.toJSONString());
			JSONArray newArray = new JSONArray();
			for (int i = 0; i < arrayOne.size(); i++) {
				JSONArray coordArray = (JSONArray) arrayOne.get(i);
				double latitude = Double.valueOf((Long) coordArray.get(0));
				double longitude = Double.valueOf((Long) coordArray.get(1));
				try {
					// 좌표 변환하고.
					PointF p = GeoConverter.tm2wgs(latitude, longitude);
					//위도 경도를 어레이로 만들어서 
					JSONArray coord = new JSONArray();
					coord.add(p.x);
					coord.add(p.y);
					//다시 어레이에 담는다.
					newArray.add(coord);
				} catch (MismatchedDimensionException e) {
					e.printStackTrace();
				} catch (TransformException e) {
					e.printStackTrace();
				}
			}
			return newArray;

		}
	}

	public static void main(String[] args) throws ParseException {
		String coord = "[[[201656,466516],[203096,465536],[204349,465326],[205725,465379],[206050,465773],[207141,465997],[208081,465259],[208463,462063],[209356,460302],[209748,460002],[210382,456122],[210104,454578],[209007,453278],[208867,450865],[210239,450866],[210741,451340],[211724,451788],[214205,452848],[215447,453123],[215971,451067],[216086,449382],[214352,449207],[212445,445937],[212409,444813],[212804,444589],[213766,444421],[213867,444553],[214200,444238],[214056,443499],[213143,441816],[210322,440006],[208498,439720],[206545,437810],[206166,436439],[204686,436339],[203090,437699],[203236,438268],[203080,439483],[202928,439992],[202572,440143],[199684,440196],[197634,438389],[196234,437485],[194768,437172],[193980,438078],[192037,437166],[190973,437474],[189863,439364],[188747,442463],[188957,442767],[188214,443458],[186297,441682],[186248,441302],[184194,441523],[183983,441771],[183704,443972],[184154,444442],[184405,445127],[184647,447009],[184212,448728],[182507,448998],[182197,448583],[181700,448596],[179708,449630],[179404,450238],[179880,450601],[180734,452385],[181705,453276],[182162,454010],[182005,455149],[182433,455832],[182755,455725],[184374,454120],[186762,452602],[187358,452490],[188966,453040],[191084,454177],[192214,460311],[195260,461846],[196115,461142],[197178,459591],[197440,458881],[197701,458810],[198540,459332],[199394,462752],[199387,464208],[200984,466201],[201334,466514],[201656,466516]]]";
		JSONParser jsonParser = new JSONParser();
		JSONArray features = (JSONArray) jsonParser.parse(coord);
		System.out.println(features);
	}

}
