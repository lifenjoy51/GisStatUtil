package gis.dump;

import gis.obj.RawData;

import java.io.IOException;

public interface DataImporter {

	/* (non-Javadoc)
	 * @see gis.dump.DataImporterIF#run()
	 */
	public abstract void run() throws IOException;

	/**
	 * 폴더 하위에 있는 텍스트파일들을 모두 읽어온다.
	 * 
	 * @param rootPath
	 */
	public abstract void readFileList(String rootPath);

	/**
	 * 데이터 파싱
	 * 
	 * @param line
	 * @return
	 */
	public abstract RawData parseRow(String line);

}