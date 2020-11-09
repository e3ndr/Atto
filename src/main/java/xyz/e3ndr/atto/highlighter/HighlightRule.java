package xyz.e3ndr.atto.highlighter;

import lombok.Getter;

@Getter
public class HighlightRule {
	private String apply;
	private String match;
	private int priority;

}
