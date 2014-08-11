package gis.trans;

import gis.obj.CodeInfo;
import gis.obj.CodeItemDist;

import java.util.List;
import java.util.Queue;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CoordArrayConverter {


	private SqlSession session;
	final int BATCH_SIZE = 100;
	int commit = 0;
	


	@Autowired
	SqlSessionFactory sqlSessionFactory;

	// 모든 동의 위치정보를 담고
	List<CodeInfo> codeInfoList;
	

	// 처리할 작업 큐.
	Queue<CodeItemDist> compQueue;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
