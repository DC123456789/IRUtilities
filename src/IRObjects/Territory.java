package IRObjects;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import IRParser.ImperatorParser;


public class Territory implements Comparable<Territory> {
	public int id;
	public Culture culture;
	public Religion religion;
	public String name, tradeGood, status, terrain;
	public Deity holySite;
	public List<Pop> pops = new ArrayList<Pop>();
	public Country owner;
	public boolean isPort = false;
	public boolean isCoastal = false;
	public String territoryType = "inhabitable";
	public int numNobles = 0;
	public int numCitizens = 0;
	public int numFreemen = 0;
	public int numTribesmen = 0;
	public int numSlaves = 0;
	public Province province;
	public int wonderIndex = -1;
	public List<Modifier> modifiers = new ArrayList<Modifier>();
	public List<CulturalName> culturalNames = new ArrayList<CulturalName>();
	public int colour = -1;
	
	public static Hashtable<Territory, Integer> TerritoryToGeoIndex = new Hashtable<Territory, Integer>();

	public Territory(String id) {
		this.id = Integer.parseInt(id);
		this.name = ImperatorParser.localisation.get("PROV" + id);
	}
	
	public Territory(int id) {
		this.id = id;
		this.name = ImperatorParser.localisation.get("PROV" + Integer.toString(id));
	}
	
	public String getStartingName() {
		for (CulturalName culturalName : culturalNames) {
			if (culturalName.cultureOrTag instanceof Country && owner == culturalName.cultureOrTag) {
				return culturalName.name;
			}
			else if (culturalName.cultureOrTag instanceof Culture && owner.culture == culturalName.cultureOrTag) {
				return culturalName.name;
			}
			else if (culturalName.cultureOrTag instanceof CultureGroup && owner.culture.group == culturalName.cultureOrTag) {
				return culturalName.name;
			}
		}
		return name;
	}
	
	public int getPopulation() {
		return numNobles + numCitizens + numFreemen + numTribesmen + numSlaves;
	}
	
	public Wonder getWonder(List<Wonder> wonders) {
		if (wonderIndex == -1)
			return null;
		else
			return wonders.get(wonderIndex);
	}
	
	public boolean inProvince(String provinceKey) {
		return this.province.key.equals(provinceKey);
	}
	
	public boolean inRegion(String regionKey) {
		return this.province.region.key.equals(regionKey);
	}
	
	public int geoCompareTo(Territory otherTerritory) {
    	Integer lhsIndex = Territory.TerritoryToGeoIndex.get(this);
    	Integer rhsIndex = Territory.TerritoryToGeoIndex.get(otherTerritory);
    	if (lhsIndex != null && rhsIndex != null)
    		return Territory.TerritoryToGeoIndex.get(this) - Territory.TerritoryToGeoIndex.get(otherTerritory);
    	else if (lhsIndex != null)
    		return Territory.TerritoryToGeoIndex.get(this) - Integer.MAX_VALUE;
    	else if (rhsIndex != null)
    		return Integer.MAX_VALUE - Territory.TerritoryToGeoIndex.get(otherTerritory);
    	else
    		return this.id - otherTerritory.id;
	}
	
	@Override
	public int compareTo(Territory otherTerritory) {
		return id - otherTerritory.id;
	}
	
	@Override
	public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Territory other = (Territory)o;
	    return id == other.id;
	}
}
