package IRObjects;

import IRParser.ImperatorParser;


public class Government extends ImpObject {
	public String governmentType;

	public Government(String key) {
		super(key, ImperatorParser.localisation.get(key));
	}
	
	public Government(String key, String name, String type) {
		super(key, name);
		governmentType = type;
	}
	
	public String getBracketedName() {
		String displayType = governmentType;
		if (displayType.equals("tribal")) {
			displayType = "tribe";
		}
		String capitalizedType = displayType.substring(0, 1).toUpperCase() + displayType.substring(1);
		String inBracket = name.replaceAll(capitalizedType, "").trim();
		return capitalizedType + " (" + inBracket + ")";
	}
}
