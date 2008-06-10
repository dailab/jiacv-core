package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import java.util.Set;
import java.util.HashSet;

import de.dailab.jiactng.agentcore.knowledge.IFact;
	@SuppressWarnings("serial")
	public class FactSet implements IFact{
		HashSet<IFact> _facts = new HashSet <IFact>();
		
		public FactSet(){
			
		}
		
		public FactSet(Set<IFact> facts){
			_facts.addAll(facts);
		}
		
		public void add(IFact fact){
			_facts.add(fact);
		}
		
		public void add(Set<IFact> factsToAdd){
			_facts.addAll(factsToAdd);
		}
		
		public HashSet<IFact> getFacts(){
			return _facts;
		}
		
		public boolean remove(IFact fact){
			return _facts.remove(fact);
		}
		
		public boolean remove(Set<IFact> facts){
			return _facts.removeAll(facts);
		}
		
		public void clear(){
			_facts.clear();
		}
		
		public boolean isEmpty(){
			return _facts.isEmpty();
		}
	}
