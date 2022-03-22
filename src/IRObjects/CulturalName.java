package IRObjects;

import IRParser.ImperatorParser;


public class CulturalName {
	public CulturalNamer cultureOrTag;
	public String key, name;
	
	public CulturalName(CulturalNamer cultureOrTag, String key) {
		this.cultureOrTag = cultureOrTag;
		this.key = key;
		this.name = ImperatorParser.localisation.get(key);
		// Check for nested keys
		if (this.name.charAt(0) == '$' && this.name.charAt(this.name.length() - 1) == '$') {
			this.name = ImperatorParser.localisation.get(this.name.substring(1, this.name.length() - 1));
		}
	}
}
