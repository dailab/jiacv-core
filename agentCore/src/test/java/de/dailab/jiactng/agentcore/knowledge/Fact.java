package de.dailab.jiactng.agentcore.knowledge;

public class Fact implements IFact {
	private static final long serialVersionUID = -6023201290809361100L;

	public Integer integer;
	public String string;
	public Boolean bool;
	
	public Fact(int integer, String string, boolean bool) {
	    this(Integer.valueOf(integer), string, Boolean.valueOf(bool));
	}
	
	public Fact(Integer integer, String string, Boolean bool) {
		this.integer = integer;
		this.string = string;
		this.bool = bool;
	}
}
