package IRObjects;

import IRParser.ImperatorParser;


public class Deity extends ImpObject {
	public Religion religion;
	
	public Deity(String key) {
		super(key, ImperatorParser.localisation.get(key));
	}
	
	public Deity(String key, String name) {
		super(key, name);
	}
}
