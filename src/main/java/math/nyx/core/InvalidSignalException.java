package math.nyx.core;

public class InvalidSignalException extends Exception {
	private static final long serialVersionUID = -8781806840069017094L;

	public InvalidSignalException(String msg) {
		super(msg);
	}

	public InvalidSignalException(Exception causedBy) {
		super(causedBy);
	}
}
