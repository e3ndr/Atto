package xyz.e3ndr.atto.highlighter;

import java.io.File;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public class HighlighterConfig {
	private List<HighlightRule> rules;
	private List<String> aliases;
	
	public void sort() {
		Collections.sort(this.rules, (o1, o2) -> {
			return (o1.getPriority() > o2.getPriority()) ? 1 : -1;
		});
	}
	
	public boolean matches(File file) {
		String name = file.getName();
	
		for (String alias : this.aliases) {
			if (name.toLowerCase().endsWith(alias.toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
	
}
