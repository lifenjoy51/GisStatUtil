package gis.obj;

public class CodeItemCnt {
	private String code;
	private String item;
	private Integer cnt;

	public CodeItemCnt(String code, String item, Integer cnt) {
		super();
		this.code = code;
		this.item = item;
		this.cnt = cnt;
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

	public Integer getCnt() {
		return cnt;
	}

	public void setCnt(Integer cnt) {
		this.cnt = cnt;
	}

}
