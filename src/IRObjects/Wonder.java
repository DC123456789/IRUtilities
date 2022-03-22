package IRObjects;

import IRParser.ImperatorParser;


public class Wonder extends ImpObject {
	public String DLC = null;

	public Wonder(String key, String nameKey) {
		super(key, ImperatorParser.localisation.get(nameKey));
	}
}
