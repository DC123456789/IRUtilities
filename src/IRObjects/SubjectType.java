package IRObjects;

import IRParser.ImperatorParser;


public class SubjectType extends ImpObject {
	
	public SubjectType(String key) {
		super(key, ImperatorParser.localisation.get(key));
	}
	
	public SubjectType(String key, String name) {
		super(key, name);
	}
}
