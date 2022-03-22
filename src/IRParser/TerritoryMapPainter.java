package IRParser;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;
import java.util.function.Function;

import javax.imageio.ImageIO;

import IRObjects.Country;
import IRObjects.Territory;


public class TerritoryMapPainter {
	
	enum MapType {
		BLANK,
		COUNTRY_MISSIONS,
		GROUP_MISSIONS,
		CUSTOM
	}

	public static final Hashtable<MapType, String> TERRITORY_MAP_PATHS = new Hashtable<MapType, String>() {{
	    put(MapType.BLANK, "Blank map.png");
	    put(MapType.COUNTRY_MISSIONS, "National mission map.png");
	    put(MapType.GROUP_MISSIONS, "Group mission map.png");
	    put(MapType.CUSTOM, "Custom map.png");
	}};

	public static final String[] NATIONAL_MISSION_COUNTRIES = {
		"ROM", "CAR", "ATH", "SPA", "SYR", "EPI", "PRY", "SEL", "EGY", "MAC", "TRE"
	};
	
	public int getNationalMissionColour(Country owner) {
		if (Objects.isNull(owner)) {
			return Color.WHITE.getRGB();								
		}
		else if (Arrays.stream(NATIONAL_MISSION_COUNTRIES).anyMatch(owner.key::equals)) {
			return owner.colour;
		}
		else if (owner.culture.key.equals("carthaginian")) {
			return Color.GREEN.getRGB();								
		}
		else {
			return Color.WHITE.getRGB();								
		}
	}

	public static final int BLACK_SEA_MISSION_COLOUR = 12034552;
	public static final String[] PONTIC_CULTURES = { "bosporan", "greco_pontic", "thracian"};
	public static final String[] BLACK_SEA_REGIONS = { "bithynia_region", "colchis_region", "taurica_region" };
	public static final String[] BLACK_SEA_PROVINCES = { 
		"olbia_area", "scythia_area", "moesia_orientalis_area", "haemimontus_area", "europa_area"
	};
	public static final String[] NEAR_BLACK_SEA_REGIONS = { "asia_region", "bithynia_region", "thrace_region" };
	
	public static final int GREECE_MISSION_COLOUR = (new Color(118, 190, 232)).getRGB();
	
	public static final int MAGNA_GRAECIA_MISSION_COLOUR = (new Color(109, 43, 175)).getRGB();
	
	public static final int WESTERN_GREEKS_MISSION_COLOUR = (new Color(16, 99, 103)).getRGB();
	public static final String[] WESTERN_GREEKS_REGIONS = { 
		"transalpine_gaul_region", "tarraconensis_region", "contestania_region", "baetica_region" 
	};

	public static final int DIADOCHI_MISSION_COLOUR = (new Color(34, 108, 210)).getRGB();
	public static final String[] DIADOCHI_COUNTRIES = {
		"PRY", "SEL", "EGY", "MAC", "TRE"
	};

	public ArrayList<Integer> getGroupMissionColour(Country owner) {
		ArrayList<Integer> colourList = new ArrayList<Integer>();
		boolean isPotentialOnly = false;
		
		if (Objects.isNull(owner)) {
			colourList.add(Color.WHITE.getRGB());
			return colourList;
		}
		// Black Sea missions
		if (!Arrays.stream(DIADOCHI_COUNTRIES).anyMatch(owner.key::equals)) {
			if (Arrays.stream(PONTIC_CULTURES).anyMatch(owner.culture.key::equals) && 
				(Arrays.stream(BLACK_SEA_REGIONS).anyMatch(owner.getCapitalRegion().key::equals) ||
				Arrays.stream(BLACK_SEA_PROVINCES).anyMatch(owner.getCapitalProvince().key::equals))) {
				colourList.add(BLACK_SEA_MISSION_COLOUR);
				if (!Objects.isNull(owner.subjectType))
					isPotentialOnly = true;
			}
			else if (owner.culture.group.key.equals("hellenic")) {
				for (Territory territory : owner.territories) {
					if (territory.isCoastal && Arrays.stream(NEAR_BLACK_SEA_REGIONS).anyMatch(territory.province.region.key::equals)) {
						colourList.add(BLACK_SEA_MISSION_COLOUR);
						if (!Objects.isNull(owner.subjectType) ||
								!(owner.key.equals("MGA") || owner.key.equals("MLO") || owner.territories.size() >= 25)) {
							isPotentialOnly = true;
						}
						break;
					}
				}
			}
		}
		
		// Greece missions
		if (owner.culture.group.key.equals("hellenic") && 
				!Arrays.stream(DIADOCHI_COUNTRIES).anyMatch(owner.key::equals) && !owner.key.equals("EPI") &&
				((owner.getCapitalRegion().key.equals("greece_region") && !owner.getCapitalProvince().key.equals("epirus_area"))) ||
				owner.getCapitalProvince().key.equals("thessaly_area")) {
			colourList.add(GREECE_MISSION_COLOUR);
			if (!Objects.isNull(owner.subjectType) || owner.key.equals("ATH") || owner.key.equals("SPA") ||
					owner.culture.key.equals("cretan")) {
				isPotentialOnly = true;
			}
		}
		
		// Magna Graecia missions
		if (owner.culture.key.equals("italiotian") && 
				(owner.getCapitalRegion().key.equals("magna_graecia_region") || owner.getCapitalRegion().key.equals("central_italy_region")) &&
				!Arrays.stream(DIADOCHI_COUNTRIES).anyMatch(owner.key::equals) && !owner.key.equals("SYR")) {
			colourList.add(MAGNA_GRAECIA_MISSION_COLOUR);
		}
		
		// Western Greeks missions
		if (!Arrays.stream(DIADOCHI_COUNTRIES).anyMatch(owner.key::equals)) {
			if (owner.culture.key.equals("massalian") 
					&& Arrays.stream(WESTERN_GREEKS_REGIONS).anyMatch(owner.getCapitalRegion().key::equals)) {
				colourList.add(WESTERN_GREEKS_MISSION_COLOUR);
				if (!Objects.isNull(owner.subjectType))
					isPotentialOnly = true;				
			}
			else if (owner.culture.group.key.equals("hellenic") && owner.key.equals("ELE")) {
				colourList.add(WESTERN_GREEKS_MISSION_COLOUR);
				if (!(owner.territories.size() >= 5)) {
					isPotentialOnly = true;
				}
				else {
					boolean hasItalianPort = false;
					for (Territory territory : owner.territories) {
						if (territory.isPort && (territory.inRegion("italia_region") || territory.inRegion("magna_graecia_region"))) {
							hasItalianPort = true;
							break;
						}
					}
					if (!hasItalianPort)
						isPotentialOnly = true;
				}
				
			}
		}
		
		// Diadochi missions
		if (Arrays.stream(DIADOCHI_COUNTRIES).anyMatch(owner.key::equals) || owner.key.equals("EPI")) {
			colourList.add(DIADOCHI_MISSION_COLOUR);
			isPotentialOnly = true;
		}
		
		if (colourList.size() < 1 || (colourList.size() == 1 && isPotentialOnly))
			colourList.add(Color.WHITE.getRGB());
		
		return colourList;
	}
	
	public static final Integer[] MARE_NOSTRUM_TERRITORIES = {
		3061, 3085, 3080, 3083, 3086, 3090, 3091, 3092, 3095, 3101, 3109, 3106, 3108, 3111, 1347, 1348, 1350, 1361, 1362, 
		1366, 1367, 1283, 1282, 1278, 1276, 670, 666, 668, 667, 665, 664, 663, 662, 661, 660, 659, 658, 657, 656, 535, 
		509, 510, 508, 522, 523, 533, 530, 7691, 7690, 7688, 529, 516, 453, 445, 248, 245, 246, 243, 242, 240, 1453, 345, 
		346, 347, 350, 349, 297, 270, 1774, 354, 357, 362
	};
	public static final String[] MARE_NOSTRUM_REGIONS = {
		"contestania_region", "tarraconensis_region", "transalpine_gaul_region", "cisalpine_gaul_region",
		"central_italy_region", "magna_graecia_region", "illyria_region", "greece_region", "macedonia_region",
		"asia_region", "cilicia_region", "syria_region", "cyrenaica_region", "africa_region", "numidia_region"
	};
	public static final int[] MARE_NOSTRUM_EXCLUDED_TERRITORIES = { 1017, 1083, 1099, 1106, 1107 };
	public static final String[] ARGEAD_EMPIRE_REGIONS = {
		"parthia_region", "syria_region", "ariana_region", "bactriana_region", "media_region", "persis_region",
		"gedrosia_region", "mesopotamia_region", "assyria_region", "bithynia_region", "cappadocia_region",
		"galatia_region", "cilicia_region", "asia_region", "greece_region", "palestine_region", "gandhara_region",
		"lower_egypt_region", "upper_egypt_region", "macedonia_region", "thrace_region"
	};
	public static final int[] ARGEAD_EMPIRE_TERRITORIES = { 
		379, 418, 416, 350, 292, 516, 500, 659, 743, 790, 5537, 918, 911, 4799, 1595, 7314, 6821 
	};
	
	public ArrayList<Integer> getCustomColour(Territory territory) {
		ArrayList<Integer> colourList = new ArrayList<Integer>();
		int[] territoryList = {200, 1829, 1827, 1828, 201, 203, 232, 211, 206, 1816, 1815, 1814, 199, 1831};
		if (Arrays.stream(territoryList).anyMatch(i -> i == territory.id)) {
			colourList.add(Color.HSBtoRGB(0.83f, 1f, 0.52f));
		}
		else {
			colourList.add(Color.WHITE.getRGB());
		}
		return colourList;
	}
			
	public void createProvinceMap(BufferedImage provinceMap, Hashtable<Integer, Territory> colourToTerritory, MapType type) {
		try {
			BufferedImage blankMap = new BufferedImage(provinceMap.getWidth(), provinceMap.getHeight(), provinceMap.getType());
			for (int x = 0; x < blankMap.getWidth(); x++) {
				for (int y = 0; y < blankMap.getHeight(); y++) {
					int currentColour = provinceMap.getRGB(x, y);
					if ((y > 0 && provinceMap.getRGB(x, y - 1) != currentColour)
							|| (y > 0 && x < blankMap.getWidth() - 1 && provinceMap.getRGB(x + 1, y - 1) != currentColour) ) {
						blankMap.setRGB(x, y, Color.BLACK.getRGB());
					}
					else {
						String territoryType = colourToTerritory.get(currentColour).territoryType;
						Territory territory = colourToTerritory.get(currentColour);
						Country owner = colourToTerritory.get(currentColour).owner;
						if (territoryType.equals("inhabitable")) {
							if (type == MapType.BLANK) {
								blankMap.setRGB(x, y, Color.WHITE.getRGB());								
							}
							else if (type == MapType.COUNTRY_MISSIONS) {
								blankMap.setRGB(x, y, getNationalMissionColour(owner));
							}
							else if (type == MapType.GROUP_MISSIONS) {
								ArrayList<Integer> groupColours = getGroupMissionColour(owner);
								blankMap.setRGB(x, y, groupColours.get(((x + y) / 10) % groupColours.size()));
							}
							else if (type == MapType.CUSTOM) {
								ArrayList<Integer> groupColours = getCustomColour(territory);
//								if (territory.inRegion("asia_region") && !(territory.owner.key.equals("PRY"))) {
//									blankMap.setRGB(x, y, groupColours.get(
//											((x / 10) % groupColours.size() + (y / 10) % groupColours.size()) % groupColours.size()
//									));	
//								}
								blankMap.setRGB(x, y, groupColours.get(((x + y) / 10) % groupColours.size()));	
							}
						}
						else if (territoryType.equals("sea_zones") 
								|| territoryType.equals("lakes") 
								|| territoryType.equals("river_provinces")
								|| territoryType.equals("sea_wasteland"))
							blankMap.setRGB(x, y, 4484003);
						else if (territoryType.equals("uninhabitable"))
							blankMap.setRGB(x, y, 12171705);
						else if (territoryType.equals("impassable_terrain") || territoryType.equals("wasteland") )
							blankMap.setRGB(x, y, 6250335);
					}
				}
			}
			ImageIO.write(blankMap, "png", new File(TERRITORY_MAP_PATHS.get(type)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ImperatorParser newParser = new ImperatorParser();
		System.out.println("Parsing...");
		newParser.parse();
		System.out.println("Parsing done!");
		//newParser.writeTerritoryTables();
		//newParser.printCultureTable();
		//newParser.printCountryTable();
		newParser.createProvinceMap(MapType.CUSTOM);
	}

}
