package org.kidneyomics.fluidigm;

import java.util.Collection;
import java.util.LinkedList;

public class MakeEntry {
	
	Collection<MakeEntry> dependencies;
	String target;
	Collection<String> commands;
	String comment;
	
	public MakeEntry() {
		this.dependencies = new LinkedList<MakeEntry>();
		this.commands = new LinkedList<String>();
	}
	
	public Collection<MakeEntry> getDependencies() {
		return dependencies;
	}
	
	public String getTarget() {
		return target;
	}
	
	public MakeEntry setTarget(String target) {
		this.target = target;
		return this;
	}
	
	public Collection<String> getCommands() {
		return commands;
	}
	
	public MakeEntry addDependency(MakeEntry dependency) {
		this.dependencies.add(dependency);
		return this;
	}
	
	public MakeEntry addDependencies(Collection<MakeEntry> dependencies) {
		for(MakeEntry dependency : dependencies) {
			this.dependencies.add(dependency);
		}
		return this;
	}
	
	
	public MakeEntry addCommand(String command) {
		this.commands.add(command);
		return this;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
	
	
	
}
