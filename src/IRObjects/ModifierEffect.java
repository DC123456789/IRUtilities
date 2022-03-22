package IRObjects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import IRParser.ImperatorParser;


public class ModifierEffect extends ImpObject {
	public String iconName;
	public String valueString;

	public static final Hashtable<String, String> ICON_NAMES = new Hashtable<String, String>() {{
	    put("country civilization value", "civilization");
	    put("defensive", "fort defense");
	    put("happiness for same religion", "state religion happiness");
	    put("goods from slaves", "slaves for surplus");
	    put("monthly state loyalty", "province loyalty");
	    put("population happiness", "happiness");
	}};
	
	public static List<ModifierIcon> modifierIcons;
	
	public static final Set<String> NEGATIVE_GOOD_MODIFIERS = Stream.of("local_goods_from_slaves").collect(Collectors.toCollection(HashSet::new));

	public ModifierEffect(String key, String value, Hashtable<String, Double> scriptValues) {
		super(key, ImperatorParser.localisation.get(key));
		checkName();
		setValueString(value, scriptValues);
		setIconName();
	}
	
	private static int getIconPriority(ModifierEffect effect) {
		// Special rules - certain icons seem to be more preferred?
		if (effect.key.equals("pirate_haven")) {
			return -2;
		}
		else if (effect.key.equals("base_resources") || effect.key.equals("local_population_capacity")) {
			return -1;
		}
		else {
			int index = modifierIcons.indexOf(new ModifierIcon(effect.key));
			if (index != -1)
				return index;
			else {
				// If it's an invalid icon, put it last?
				System.err.println("Could not find icon for modifier effect " + effect.key);
				return Integer.MAX_VALUE;
			}
		}
	}
	
	@Override
	public int compareTo(ImpObject otherObject) {
		return getIconPriority(this) - getIconPriority((ModifierEffect)otherObject);
	}
	
	private void checkName() {
		// For many modifiers, the name has a different format
		if (this.name == null) {
			if (this.key.equals("mercenary_land_maintenance_cost")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_LAND_MERCENARY_MAINTENANCE_COST");					
			}
			else if (this.key.equals("mercenary_naval_maintenance_cost")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_NAVAL_MERCENARY_MAINTENANCE_COST");					
			}
			else if (this.key.equals("levy_size_multiplier")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_LEVY_SIZE_MULT");					
			}
			else if (this.key.equals("base_resources")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_BASE_RESOURCE");					
			}
			else if (this.key.equals("local_hostile_food_multiplier")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_HOSTILE_FOOD_MULTIPLIER");					
			}
			else if (this.key.equals("local_combat_width_modifier")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_LOCAL_COMBAT_WIDTH");					
			}
			else if (this.key.equals("max_mercenary_stacks")) {
				this.name = ImperatorParser.localisation.get("MODIFIER_MAX_MERC_STACKS");					
			}
			else if (this.key.equals("price_revoke_city_status_cost_modifier")) {
				this.name = ImperatorParser.localisation.get("price_revoke_city_status");					
			}
			else if (this.key.equals("price_revoke_metropolis_status_cost_modifier")) {
				this.name = ImperatorParser.localisation.get("price_revoke_metropolis_status");					
			}
			else {
				this.name = ImperatorParser.localisation.get("MODIFIER_" + key.toUpperCase());				
			}			
		}
		if (this.name == null) {
			System.err.println("Could not find localization key " + "MODIFIER_" + key.toUpperCase() + " in modifier " + this.key);
			this.name = this.key;
		}
	}
	
	private void setValueString(String value, Hashtable<String, Double> scriptValues) {
		double numberValue = 0;
		try {
			numberValue = Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			// If the value's not a number, try to find it in the script values
			Double scriptNumberValue = scriptValues.get(value);
			if (scriptNumberValue != null) {
				numberValue = scriptNumberValue;
			}
			else if (value.equals("yes") || value.equals("no")) {	// A few modifiers are booleans, just return it back for now
				valueString = value;				
			}
			else {
				if (!value.equals("yes")) {	// Actual errors
					System.err.println("Could not find script value " + value + " in modifier effect " + this.key);					
				}
				valueString = "";				
			}
		}
		
		String processedValue = Double.toString(numberValue);
		
		// Don't display decimals unless actually necessary
		int roundedNumberValue = (int)numberValue;
		if (roundedNumberValue == numberValue) {
			processedValue = Integer.toString(roundedNumberValue);
		}
		
		// Effects with "modifier", "happyness"/"happiness", or "defensive" in their key are 
		// most likely displayed as percentages
		if (this.key.indexOf("modifier") > -1 || this.key.indexOf("happiness") > -1 || 
				this.key.indexOf("happyness") > -1 || this.key.indexOf("defensive") > -1) {
			processedValue = Double.toString(numberValue * 100);
			int roundedPercentValue = (int)(numberValue * 100);
			if (roundedPercentValue == (numberValue * 100)) {
				processedValue = Integer.toString(roundedPercentValue);
			}
			processedValue = processedValue + "%";
		}			
		// Civilization modifiers are also displayed as percentages, but shouldn't be multiplied
		else if (this.key.indexOf("civilization") > -1) {
			processedValue = processedValue + "%";				
		}
		
		// Make sure positive modifiers have a "+" in front of them
		if (numberValue > 0) {
			processedValue = "+" + processedValue;
		}
		
		if (numberValue >= 0 ^ NEGATIVE_GOOD_MODIFIERS.contains(this.key)) {
			valueString = "{{green|" + processedValue + "}}";
		}
		else {
			valueString = "{{red|" + processedValue + "}}";				
		}
	}
	
	private void setIconName() {
		iconName = key.replace('_', ' ').replace("local", " ").replace("modifier", " ").replace("happyness", "happiness").replace("tribesmen", "tribesman").strip();
		String specificDictName = ICON_NAMES.get(iconName);
		if (specificDictName != null) {
			iconName = specificDictName;
		}
	}
}
