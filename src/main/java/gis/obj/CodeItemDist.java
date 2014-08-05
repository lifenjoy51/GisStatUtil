package gis.obj;

public class CodeItemDist {
	private String code;
	private String item;
	private Integer dist;

	public CodeItemDist(String code, String item, Integer dist) {
		super();
		this.code = code;
		this.item = item;
		this.dist = dist;
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

	public Integer getDist() {
		return dist;
	}

	public void setDist(Integer dist) {
		this.dist = dist;
	}

}
