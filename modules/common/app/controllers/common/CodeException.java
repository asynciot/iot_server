package controllers.common;

public class CodeException extends Exception {

	private Integer m_code = 0;
	
	public CodeException(int code) {
		m_code = code;
	}
	
	@Override
	public String getMessage() {
		return "error code:" + m_code.toString();
	}
	
	public Integer getCode() {
		return m_code;
	}
}
