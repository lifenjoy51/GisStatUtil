package gis.obj;

public class DetailCodeInfo {
	private String code;
	private String addr;
	private String coord_array_string;
	private Integer center_x;
	private Integer center_y;
	private String status;
	private String worker;

	public DetailCodeInfo() {

	}

	public DetailCodeInfo(String code, String worker, String status) {
		this.code = code;
		this.worker = worker;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getCoord_array_string() {
		return coord_array_string;
	}

	public void setCoord_array_string(String coord_array_string) {
		this.coord_array_string = coord_array_string;
	}

	public Integer getCenter_x() {
		return center_x;
	}

	public void setCenter_x(Integer center_x) {
		this.center_x = center_x;
	}

	public Integer getCenter_y() {
		return center_y;
	}

	public void setCenter_y(Integer center_y) {
		this.center_y = center_y;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getWorker() {
		return worker;
	}

	public void setWorker(String worker) {
		this.worker = worker;
	}

	@Override
	public String toString() {
		return "DetailCodeInfo [code=" + code + ", addr=" + addr
				+ ", coord_array_string=" + coord_array_string + ", center_x="
				+ center_x + ", center_y=" + center_y + "]";
	}

}
