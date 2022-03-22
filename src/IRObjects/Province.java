package IRObjects;

import java.util.ArrayList;
import java.util.List;

import IRParser.ImperatorParser;


public class Province extends ImpObject {
	public List<Territory> territories = new ArrayList<Territory>();
	public Region region;

	public Province(String key) {
		super(key, ImperatorParser.localisation.get(key));
		
		if (key.equals("{")) {
			System.err.println("Broken province key!");
		}
	}
	
	public Province(String key, String name) {
		super(key, name);
		
		if (key.equals("{")) {
			System.err.println("Broken province key!");
		}
	}
}
