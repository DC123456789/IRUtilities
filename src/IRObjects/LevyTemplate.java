package IRObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map.Entry;


public class LevyTemplate extends ImpObject {
	
	public static final Hashtable<String, Integer> UNIT_TYPE_RANKING = new Hashtable<String, Integer>() {{
	    put("supply_train", 0);
	    put("warelephant", 1);
	    put("heavy_infantry", 2);
	    put("heavy_cavalry", 3);
	    put("light_infantry", 4);
	    put("light_cavalry", 5);
	    put("horse_archers", 6);
	    put("chariots", 7);
	    put("camels", 8);
	    put("archers", 9);
	}};
	
	public ArrayList<Entry<String, Double>> levies = new ArrayList<Entry<String, Double>>();

	public LevyTemplate(String key) {
		super(key, "");
	}
	
	public String toDisplayString(Hashtable<String, String> unitLoc) {
		// Make sure list is sorted in preferred order
		Collections.sort(levies, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> arg0, Entry<String, Double> arg1) {
				return UNIT_TYPE_RANKING.get(arg0.getKey()) - UNIT_TYPE_RANKING.get(arg1.getKey());
			}
		});
		
		// Add up sum of levy composition values in case it doesn't go to 1
		double totalLevies = 0;
		for (Entry<String, Double> levyType : this.levies) {
			totalLevies += levyType.getValue();				
		}

		// Actually convert to a string array
		ArrayList<String> levyCompositionArray = new ArrayList<String>();
		for (Entry<String, Double> levyType : this.levies) {
			levyCompositionArray.add("{{icon|" + unitLoc.get(levyType.getKey()).toLowerCase() + "}} " +
					String.valueOf(Math.round(levyType.getValue() * 100 / totalLevies)) + "%");
		}
		
		return String.join(", ", levyCompositionArray);
	}
}
