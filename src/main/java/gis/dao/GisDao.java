package gis.dao;

import gis.obj.CompInfo;
import gis.obj.DetailCodeInfo;
import gis.obj.RawData;
import gis.obj.StatInfo;

import java.util.List;


public interface GisDao {

	public void insertDetailCodeInfo(DetailCodeInfo info);

	public DetailCodeInfo getDetailCodeInfo();
	
	public List<DetailCodeInfo> getDetailCodeList(String upCode);
	
	public List<DetailCodeInfo> getDetailCodeListAll();

	public void insertStatInfo(StatInfo statInfo);
	
	public String getRemainCode();

	public void updateStatus(DetailCodeInfo info);

	public void insertRawData(RawData data);

	public void insertCompInfo(CompInfo c);
	
	public List<CompInfo> getCompInfoList();
	
	public void updateCompInfoCode(CompInfo c);

	public List<DetailCodeInfo> getDetailCodeListBy5Code(String code);

}
