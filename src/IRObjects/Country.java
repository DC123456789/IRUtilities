package IRObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import IRParser.ImperatorParser;

public class Country extends ImpObject implements CulturalNamer {
	public List<Territory> territories = new ArrayList<Territory>();
	public Territory capital;
	public Government government;
	public Culture culture;
	public Religion religion;
	public SubjectType subjectType;
	public Country suzerain;
	public String properName;
	public int colour;

	public Country(String key) {
		super(key, ImperatorParser.localisation.get(key));
	}
	
	public Country(String key, String name) {
		super(key, name);
	}
	
	public int getTotalPopulation() {
		int population = 0;
		for (Territory territory: territories) {
			population += territory.getPopulation();
		}
		return population;
	}
	
	public String getProperName() {
		return properName;
	}
	
	public Province getCapitalProvince() {
		return this.capital.province;
	}
	
	public Region getCapitalRegion() {
		return this.capital.province.region;
	}
	
	public MilitaryTradition[] getTraditions(List<MilitaryTradition> militaryTraditions) {
		// Special cases
		if (this.key.equals("BAC") || this.key.equals("ARS")) {
			MilitaryTradition[] greco_persian_traditions = new MilitaryTradition[2];
			int greekTraditionIndex = militaryTraditions.indexOf(new MilitaryTradition("greek_philosophy", ImperatorParser.localisation));
			if (greekTraditionIndex > -1) {
				greco_persian_traditions[0] = militaryTraditions.get(greekTraditionIndex);
			}
			else {
				System.err.println("Could not find military tradition: persian_philosophy");
				return null;
			}
			int persianTraditionIndex = militaryTraditions.indexOf(new MilitaryTradition("persian_philosophy", ImperatorParser.localisation));
			if (persianTraditionIndex > -1) {
				greco_persian_traditions[1] = militaryTraditions.get(persianTraditionIndex);
			}
			else {
				System.err.println("Could not find military tradition: persian_philosophy");
				return null;
			}
			return greco_persian_traditions;
		}
		else if (this.culture.key.equals("carthaginian")) {
			int militaryTraditionIndex = militaryTraditions.indexOf(new MilitaryTradition("north_african_philosophy", ImperatorParser.localisation));
			if (militaryTraditionIndex > -1) {
				return new MilitaryTradition[] {militaryTraditions.get(militaryTraditionIndex)};
			}
			else {
				System.err.println("Could not find military tradition: north_african_philosophy");
				return null;
			}
		}
		else {
			int militaryTraditionIndex = militaryTraditions.indexOf(new MilitaryTradition(this.culture.group.traditions, ImperatorParser.localisation));
			if (militaryTraditionIndex > -1) {
				return new MilitaryTradition[] {militaryTraditions.get(militaryTraditionIndex)};
			}
			else {
				System.err.println("Could not find military tradition: " + this.culture.group.traditions);
				return null;
			}	
		}
	}
	
	public String getTraditionNames(List<MilitaryTradition> militaryTraditions) {
		MilitaryTradition[] traditions = this.getTraditions(militaryTraditions);
		String[] traditionNames = new String[traditions.length];
		for (int i = 0; i < traditions.length; i++)
			traditionNames[i] = traditions[i].getDisplayName();
		return String.join(", ", traditionNames);
	}
	
	@Override
	public int compareTo(ImpObject otherCultureGroup) {
		if (Objects.isNull(properName) || Objects.isNull(((Country)otherCultureGroup).properName)) {
			System.err.println("Missing country name!");
		}
		return properName.compareTo(((Country)otherCultureGroup).properName);
	}
}
