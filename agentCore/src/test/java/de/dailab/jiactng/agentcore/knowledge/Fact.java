package de.dailab.jiactng.agentcore.knowledge;

public class Fact implements IFact {
	public Integer integer;
	public String string;
	public Boolean bool;
	
	public Fact(Integer integer, String string, Boolean bool) {
		this.integer = integer;
		this.string = string;
		this.bool = bool;
	}
}
