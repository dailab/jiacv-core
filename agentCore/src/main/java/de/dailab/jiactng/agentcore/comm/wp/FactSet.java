package de.dailab.jiactng.agentcore.comm.wp;

import java.util.Set;

import de.dailab.jiactng.agentcore.knowledge.IFact;
	@SuppressWarnings("serial")
	public class FactSet implements IFact{
		Set<IFact> _facts;
		
		public FactSet(Set<IFact> facts){
			_facts = facts;
		}
	}
