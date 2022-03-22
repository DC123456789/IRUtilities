package IRObjects;

import java.util.ArrayList;
import java.util.List;

import IRParser.ImperatorParser;


public class Modifier extends ImpObject {
	public String description;
	public ArrayList<Territory> territories = new ArrayList<Territory>();
	public ArrayList<ModifierEffect> effects = new ArrayList<ModifierEffect>();	

	public Modifier(String key) {
		super(key, ImperatorParser.localisation.get(key));
		description = ImperatorParser.localisation.get("desc_" + key);
	}
	
	public String getIcon(List<ModifierIcon> modifierIcons) {
		String firstIconKey = effects.get(0).key;
		return modifierIcons.get(modifierIcons.indexOf(new ModifierIcon(firstIconKey))).getIcon();
	}
	
	public int getGeoValue() {
		if (this.territories.size() == 0)
			return Integer.MAX_VALUE;
		Integer geoID = Territory.TerritoryToGeoIndex.get(this.territories.get(0));
		if (geoID == null)
			return Integer.MAX_VALUE;
		return geoID;
	}

	// Sort the modifiers by the "geographical" order of their territories
	@Override
	public int compareTo(ImpObject otherModifier) {
		return this.getGeoValue() - ((Modifier)otherModifier).getGeoValue();
	}
}
