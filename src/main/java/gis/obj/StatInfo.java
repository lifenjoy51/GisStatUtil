package gis.obj;

public class StatInfo {
	private String code;
	private String item;
	private String cnt;

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

	public String getCnt() {
		return cnt;
	}

	public void setCnt(String cnt) {
		this.cnt = cnt;
	}

	@Override
	public String toString() {
		return "StatInfo [code=" + code + ", item=" + item + ", cnt=" + cnt
				+ "]";
	}

}
