package gis.trans;

import gis.dao.GisDao;
import gis.obj.CodeItemDist;
import gis.obj.CompInfo;
import gis.obj.DetailCodeInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class DistCalculator {

	private SqlSession session;
	final int BATCH_SIZE = 100;
	int commit = 0;

	@Autowired
	@Qualifier("gisDao")
	GisDao dao;

	@Autowired
	SqlSessionFactory sqlSessionFactory;

	// 모든 동의 위치정보를 담고
	List<DetailCodeInfo> codeInfoList;

	// 처리할 회사정보를 담는다.
	List<CompInfo> compInfoList;

	// 처리할 작업 큐.
	Queue<CodeItemDist> compQueue;

	public DistCalculator() {
		this.codeInfoList = new ArrayList<DetailCodeInfo>();
		this.compInfoList = new ArrayList<CompInfo>();
		compQueue = new ConcurrentLinkedQueue<CodeItemDist>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gis.dump.DataImporterIF#run()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see gis.dump.DataImporter#run()
	 */
	public void run() throws IOException, InterruptedException {
		// 코드를 읽어오고
		codeInfoList = dao.getDetailCodeListAll();
		// 이 코드들이...

		// 회사들을 읽어온다.
		compInfoList = dao.getCompInfoList();
		// 회사들은 현재

		// 세션연결
		session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);

		// thread pool
		ExecutorService executor = Executors.newFixedThreadPool(8);

		// 지역코드 하나씩 작업.
		for (DetailCodeInfo dci : codeInfoList) {
			Runnable w = new DistWorker(dci);
			executor.execute(w);
		}
		executor.shutdown();

		while (!executor.isTerminated()) {
			if (compQueue.isEmpty()) {
				Thread.sleep(1);
				continue;
			}

			CodeItemDist c = compQueue.poll();
			try {
				System.out.println(commit);
				session.insert("gis.dao.GisDao.updateCodeItemDist", c);
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

	private class DistWorker extends Thread {
		DetailCodeInfo dci;

		public DistWorker(DetailCodeInfo dci) {
			this.dci = dci;
		}

		@Override
		public void run() {
			// 거리.
			Integer cx = -dci.getCenter_x();
			Integer cy = -dci.getCenter_y();

			// 거리정보를 담을 맵.
			Map<String, Integer> distMap = new HashMap<String, Integer>();

			// 돌면서 제일 가까운 위치를 판별한다.
			for (CompInfo dc : compInfoList) {
				try {
					String item = dc.getItem();
					Integer x = (int) Float.parseFloat(dc.getX());
					Integer y = (int) Float.parseFloat(dc.getY());

					// 거리계산!
					Integer dx = Math.abs(cx+ x);
					Integer dy = Math.abs(cy+ y);
					Integer d = (int) (Math.pow(dx, 2) + Math.pow(dy, 2));

					// 맵에 넣기.
					if (distMap.containsKey(item)) {
						// 기존것과 비교해서 새로운 거리가 더 작으면.
						if (distMap.get(item) > d) {
							// 바꿔치기
							distMap.remove(item);
							distMap.put(item, d);
						}
					} else {
						distMap.put(item, d);
					}

				} catch (NullPointerException npe) {
					System.err.println("NullPointerException");
				} catch (NumberFormatException nfe) {
					System.err.println(dc);
					System.err.println("NumberFormatException");
				} catch (ArrayIndexOutOfBoundsException aiobe) {

				}
			}

			// 위치계산을 다 끝냈으면?
			// 맵에 저장된 최단거리를 디비에 저장한다.
			try {
				for (Entry<String, Integer> e : distMap.entrySet()) {
					int d = (int) Math.sqrt(e.getValue());
					CodeItemDist cid = new CodeItemDist(dci.getCode(),
							e.getKey(), d);
					// 디비에 저장한다.
					compQueue.add(cid);
				}
			} catch (NullPointerException npe) {
				System.err.println("null");
			}
		}
	}

}
