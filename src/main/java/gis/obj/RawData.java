package gis.obj;

public class RawData {
	private String year;
	private String code;
	private String tp_cd;
	private String cnt;

	public RawData(String year, String code, String tp_cd, String cnt) {
		super();
		this.year = year;
		this.code = code;
		this.tp_cd = tp_cd;
		this.cnt = cnt;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTp_cd() {
		return tp_cd;
	}

	public void setTp_cd(String tp_cd) {
		this.tp_cd = tp_cd;
	}

	public String getCnt() {
		return cnt;
	}

	public void setCnt(String cnt) {
		this.cnt = cnt;
	}

}
