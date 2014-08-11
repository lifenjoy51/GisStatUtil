package gis.obj;

public class CodeInfo {
	private String code;
	private String coord_array;
	
	public CodeInfo(){
		
	}

	public CodeInfo(String code, String coord_array) {
		super();
		this.code = code;
		this.coord_array = coord_array;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCoord_array() {
		return coord_array;
	}

	public void setCoord_array(String coord_array) {
		this.coord_array = coord_array;
	}
}
