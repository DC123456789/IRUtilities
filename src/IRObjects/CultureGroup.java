package IRObjects;

import java.awt.Color;
import java.util.Hashtable;
import java.util.List;


public class CultureGroup extends ImpObject implements CulturalNamer {
	
	public static final Hashtable<String, Integer> MILITARY_TRADITION_RANKING = new Hashtable<String, Integer>() {{
	    put("celtic", 0);
	    put("latin", 1);
	    put("greek", 2);
	    put("north_african", 3);
	    put("levantine", 4);
	    put("persian", 5);
	    put("mauryan", 6);
	}};
	
	public String graphical_culture, portraits, traditions;
	public List<Culture> cultureList;
	public Color colour;
	public LevyTemplate levyTemplate = null;

	public CultureGroup(String key) {
		super(key, null);
	}
	
	public CultureGroup(String key, String name, String graphical_culture, String portraits,
			List<Culture> cultureList, Color colour, LevyTemplate levyTemplate) {
		super(key, name);
		this.graphical_culture = graphical_culture;
		this.portraits = portraits;
		this.cultureList = cultureList;
		this.colour = colour;
		this.levyTemplate = levyTemplate;
	}
	
	public MilitaryTradition getTradition(List<MilitaryTradition> militaryTraditions, Hashtable<String, String> milTradLoc) {
		int militaryTraditionIndex = militaryTraditions.indexOf(new MilitaryTradition(traditions, milTradLoc));
		if (militaryTraditionIndex > -1) {
			return militaryTraditions.get(militaryTraditionIndex);
		}
		else {
			System.err.println("Could not find military tradition: " + traditions);
			return null;
		}			
	}

	@Override
	public int compareTo(ImpObject otherCultureGroup) {
		if (otherCultureGroup instanceof CultureGroup &&
				!traditions.equals(((CultureGroup)otherCultureGroup).traditions)) {
			String otherTradition = ((CultureGroup)otherCultureGroup).traditions;
			String traditionGroup = traditions.substring(0, traditions.indexOf("philosophy") - 1);
			String otherTraditionGroup = otherTradition.substring(0, otherTradition.indexOf("philosophy") - 1);
			return MILITARY_TRADITION_RANKING.get(traditionGroup) - MILITARY_TRADITION_RANKING.get(otherTraditionGroup);
		}
		return name.compareTo(((ImpObject)otherCultureGroup).name);
	}
}
