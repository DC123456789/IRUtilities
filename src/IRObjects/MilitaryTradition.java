package IRObjects;

import java.util.Hashtable;


public class MilitaryTradition extends ImpObject {
	public MilitaryTradition(String key, Hashtable<String, String> milTradLoc) {
		super(key, milTradLoc.get(key));
	}
	
	public MilitaryTradition(String key, String name) {
		super(key, name);
	}
	
	public String getDisplayName() {
		String name_start = this.name.split(" ")[0];
		if (name_start.equals("Celtic") || name_start.equals("Britannic"))
			return "Barbarian";
		else if (name_start.equals("Roman"))
			return "Italic";
		else if (name_start.equals("Punic") || name_start.equals("Numidian"))
			return "North African";
		else
			return name_start;
	}
	
	public String getCodeName() {
		int codeNameEnd = this.key.indexOf("philosophy") + "philosophy".length();
		return this.key.substring(0, codeNameEnd);
	}
}
