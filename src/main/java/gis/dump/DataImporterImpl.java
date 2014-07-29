package gis.dump;

import gis.obj.RawData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service("dataImport")
public class DataImporterImpl implements DataImporter {

	final int BATCH_SIZE = 100;

	@Autowired
	SqlSessionFactory sqlSessionFactory;

	// 작업할 파일 목록
	List<File> fileList;

	public DataImporterImpl() {
		this.fileList = new ArrayList<File>();
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
	public void run() throws IOException {
		// 파일을 읽고
		String rootPath = "/home/lifenjoy51/Downloads/gis/additional";
		readFileList(rootPath);

		System.err.println(fileList.size());

		// 세션연결
		SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH,
				false);
		int commit = 0;

		// 파일 하나씩 작업
		for (File f : fileList) {
			System.out.println(f.getAbsolutePath());
			// 파일내용을 읽어서.
			Reader r = new FileReader(f);
			BufferedReader br = new BufferedReader(r);

			// 한줄마다 데이터를 끊어서
			String line = null;
			while ((line = br.readLine()) != null) {
				RawData data = parseRow(line);

				// 디비에 저장한다.
				try {
					session.insert("gis.dao.GisDao.insertRawData", data);
					if (++commit % BATCH_SIZE == 0) {
						session.commit();
					}
					// dao.insertRawData(data);
				} catch (DataAccessException dae) {
					// System.err.println(dae.getMessage());
				} catch (PersistenceException bee) {
					// System.err.println(bee.getMessage());
				}
			}
			// 자원 닫기
			br.close();

		}

		// 커밋
		session.commit();
		session.close();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gis.dump.DataImporter#readFileList(java.lang.String)
	 */
	public void readFileList(String rootPath) {
		File root = new File(rootPath);

		for (File f : root.listFiles()) {
			fileList.add(f);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gis.dump.DataImporter#parseRow(java.lang.String)
	 */
	public RawData parseRow(String line) {
		// System.out.println(line);
		String[] data = line.split("\\^");
		String year = data[0];
		String code = data[1];
		String tpcd = data[2];
		String cnt = data[3];

		// 소수점 정제
		if (cnt.contains(".")) {
			cnt = cnt.substring(0, cnt.indexOf("."));
		}

		// N/A 정제
		if (cnt.contains("N/A")) {
			cnt = "0";
		}

		RawData rawData = new RawData(year, code, tpcd, cnt);

		return rawData;
	}

}
