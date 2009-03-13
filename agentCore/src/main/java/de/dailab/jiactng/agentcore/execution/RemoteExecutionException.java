package de.dailab.jiactng.agentcore.execution;

public class RemoteExecutionException extends RuntimeException {
	private static final long serialVersionUID = 4393250788382152711L;

	public RemoteExecutionException() {
		this("Remote execution failed!");
	}

	public RemoteExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteExecutionException(String message) {
		super(message);
	}

	public RemoteExecutionException(Throwable cause) {
		super(cause);
	}
}
