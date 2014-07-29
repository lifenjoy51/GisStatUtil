package gis.obj;

public class CompInfo {
	private String code;
	private String item;
	private String comp_nm;
	private String number;
	private String ufid;
	private String x;
	private String y;

	public CompInfo(String code, String item, String comp_nm, String number,
			String ufid, String x, String y) {
		super();
		this.code = code;
		this.item = item;
		this.comp_nm = comp_nm;
		this.number = number;
		this.ufid = ufid;
		this.x = x;
		this.y = y;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getComp_nm() {
		return comp_nm;
	}

	public void setComp_nm(String comp_nm) {
		this.comp_nm = comp_nm;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getUfid() {
		return ufid;
	}

	public void setUfid(String ufid) {
		this.ufid = ufid;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "CompInfo [code=" + code + ", item=" + item + ", comp_nm="
				+ comp_nm + ", number=" + number + ", ufid=" + ufid + ", x="
				+ x + ", y=" + y + "]";
	}

}
