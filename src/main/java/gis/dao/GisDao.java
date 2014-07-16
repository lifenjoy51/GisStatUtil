package gis.dao;

import gis.obj.DetailCodeInfo;
import gis.obj.StatInfo;

import java.util.List;


public interface GisDao {

	public void insertDetailCodeInfo(DetailCodeInfo info);

	public DetailCodeInfo getDetailCodeInfo();
	
	public List<DetailCodeInfo> getDetailCodeList();

	public void insertStatInfo(StatInfo statInfo);

}
