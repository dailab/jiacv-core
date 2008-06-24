package de.dailab.jiactng.agentcore.comm.wp.exceptions;
@SuppressWarnings("serial")
	public class TimeoutException extends RuntimeException{
		public TimeoutException(String s){
			super(s);
		}
	}