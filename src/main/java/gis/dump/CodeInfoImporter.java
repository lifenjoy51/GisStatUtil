package gis.dump;

import gis.obj.CodeInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class CodeInfoImporter {

	private SqlSession session;
	final int BATCH_SIZE = 100;
	int commit = 0;

	@Autowired
	SqlSessionFactory sqlSessionFactory;

	// 처리할 작업 큐.
	Queue<CodeInfo> compQueue;

	public CodeInfoImporter() {
		compQueue = new ConcurrentLinkedQueue<CodeInfo>();
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
		// 파일을 읽고
		String path = "/home/lifenjoy51/git/GisStatUtil/coords.txt";

		// 세션연결
		session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);

		// thread pool
		ExecutorService executor = Executors.newFixedThreadPool(4);

		// 파일내용을 읽어서.
		Reader r = new FileReader(path);
		BufferedReader br = new BufferedReader(r);

		// 한줄마다 데이터를 끊어서
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(compQueue.size());
			CodeInfo data = parseRow(line);
			Runnable w = new CodeInfoWorker(data);
			executor.execute(w);
		}
		// 자원 닫기
		br.close();

		executor.shutdown();

		while (!executor.isTerminated() || !compQueue.isEmpty()) {
			if (compQueue.isEmpty()) {
				Thread.sleep(1);
				continue;
			}

			CodeInfo c = compQueue.poll();

			// 디비에 저장한다.
			try {
				session.insert("gis.dao.GisDao.insertCodeInfo", c);
				System.out.println("insert");
				if (++commit % BATCH_SIZE == 0) {
					System.err.println("commit " + commit);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see gis.dump.DataImporter#parseRow(java.lang.String)
	 */
	public CodeInfo parseRow(String line) {
		// System.out.println(line);
		String[] data = line.split("\t");
		String code = data[0];
		String coordAddr = data[1];

		CodeInfo codeInfo = new CodeInfo(code, coordAddr);

		return codeInfo;
	}

	/**
	 * worker
	 * 
	 * @author lifenjoy51
	 * 
	 */
	private class CodeInfoWorker extends Thread {
		CodeInfo ci;

		public CodeInfoWorker(CodeInfo ci) {
			this.ci = ci;
		}

		@Override
		public void run() {
			// 디비에 저장한다.
			compQueue.add(ci);
		}
	}

}
