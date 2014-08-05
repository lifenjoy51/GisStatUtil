package gis.trans;

import gis.dao.GisDao;
import gis.obj.CompInfo;
import gis.obj.DetailCodeInfo;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class PositionCalculator {

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
	
	//처리할 작업 큐.
	Queue<CompInfo> compQueue;

	public PositionCalculator() {
		this.codeInfoList = new ArrayList<DetailCodeInfo>();
		this.compInfoList = new ArrayList<CompInfo>();
		compQueue = new ConcurrentLinkedQueue<CompInfo>();
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

		// 현재 상위코드정보를 담고있는 변수
		String code = "";
		
		//thread pool
		ExecutorService executor = Executors.newFixedThreadPool(4);

		// 파일 하나씩 작업
		for (CompInfo c : compInfoList) {
			if (!c.getCode().equals(code)) {
				code = c.getCode();
				codeInfoList = dao.getDetailCodeListBy5Code(code);
			}
			
			Runnable w = new Worker(c, codeInfoList);
			executor.execute(w);
		}	
		executor.shutdown();
		
		while(!executor.isTerminated()){
			if(compQueue.isEmpty()){
				Thread.sleep(1);
				continue;
			}
			
			CompInfo c = compQueue.poll();
			try {
				session.update("gis.dao.GisDao.updateCompInfoCode", c);
				if (++commit % BATCH_SIZE == 0) {
					session.commit();
				}
				// dao.insertRawData(data);
			} catch (DataAccessException dae) {
				System.err.println(dae.getMessage());
			} catch (PersistenceException bee) {
				System.err.println(bee.getMessage());
			}
		}
		
        System.err.println("Finished all threads");

		// 커밋
		session.commit();
		session.close();

	}

	private class Worker extends Thread {
		CompInfo c;
		private List<DetailCodeInfo> codeList;
		

		public Worker(CompInfo c, List<DetailCodeInfo> codeInfoList) {
			this.c = c;
			this.codeList = new ArrayList<DetailCodeInfo>(codeInfoList);
		}

		@Override
		public void run() {

			DetailCodeInfo near = null;
			int minDist = Integer.MAX_VALUE;

			// 제일 가까운 위치를 판별한다.
			for (DetailCodeInfo dc : codeList) {
				try {
					int dx = (int) (Float.valueOf(c.getX()) - dc.getCenter_x());
					int dy = (int) (Float.valueOf(c.getY()) - dc.getCenter_y());
					int dist = (int) (Math.pow(dx, 2) + Math.pow(dy, 2));

					if (dist < minDist) {
						minDist = dist;
						near = dc;
					}
				} catch (NullPointerException npe) {
					//npe.printStackTrace();
					System.err.println("NullPointerException");
				}catch (NumberFormatException nfe){
					System.err.println("NumberFormatException");
				}catch (ArrayIndexOutOfBoundsException aiobe){
					
				}
			}


			try {
				c.setNear_code(near.getCode());
				//System.out.println(c);
				//System.out.println(near);
				//System.out.println();

				// 디비에 저장한다.
				compQueue.add(c);
			} catch (NullPointerException npe) {
				System.out.println(c);
				System.out.println(codeList);
				System.err.println("null");
			}
		}
	}

}
