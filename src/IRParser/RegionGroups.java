package IRParser;
import java.util.Hashtable;


public class RegionGroups {
	
	public static final String[] GALLIC_BRITANNIC_REGIONS = {
			"caledonia_region",
			"britain_region",
			"armorica_region",
			"central_gaul_region",
			"belgica_region",
			"aquitaine_region",
			"transalpine_gaul_region",
			"germania_superior_region",
			"rhaetia_region"			
	};
	
	public static final String[] IBERIAN_REGIONS = {
			"tarraconensis_region",
			"contestania_region",
			"baetica_region",
			"lusitania_region",
			"gallaecia_region"		
	};

	public static final String[] ITALIAN_AFRICAN_REGIONS = {
			"cisalpine_gaul_region",
			"central_italy_region",
			"magna_graecia_region",
			"africa_region",
			"numidia_region",
			"mauretainia_region",
	};

	public static final String[] GERMANIC_REGIONS = {
			"scandinavia_region",
			"germania_region",
			"venedia_region",
			"bohemia_area"
	};

	public static final String[] DACIAN_ILLYRIAN_REGIONS = {
			"dacia_region",
			"moesia_region",
			"pannonia_region",
			"illyria_region"
	};

	public static final String[] GREEK_ASIAN_REGIONS = {
			"greece_region",
			"macedonia_region",
			"thrace_region",
			"asia_region",
			"cilicia_region",
			"galatia_region",
			"bithynia_region",
			"cappadocia_region",
			"cappadocia_pontica_region"
	};

	public static final String[] EGYPTIAN_SYRIAN_REGIONS = {
			"cyrenaica_region",
			"lower_egypt_region",
			"upper_egypt_region",
			"palestine_region",
			"syria_region"
	};

	public static final String[] EAST_AFRICAN_ARABIAN_REGIONS = {
			"nubia_region",
			"punt_region",
			"arabia_felix_region",
			"arabia_region"
	};
	
	public static final String[] SCYTHIAN_REGIONS = {
			"vistulia_region",
			"sarmatia_europea_region",
			"taurica_region",
			"sarmatia_asiatica_region",
			"scythia_region"
	};
	
	public static final String[] CAUCASIAN_REGIONS = {
			"colchis_region",
			"albania_region",
			"armenia_region",
	};

	public static final String[] PERSIAN_REGIONS = {
			"assyria_region",
			"mesopotamia_region",
			"persis_region",
			"media_region",
			"parthia_region",
			"ariana_region",
			"bactriana_region",
			"gedrosia_region"
	};

	public static final String[] CENTRAL_ASIAN_REGIONS = {
			"sogdiana_region",
			"himalayan_region",
			"tibet_region",
	};

	public static final String[] INDIAN_REGIONS = {
			"gandhara_region",
			"maru_region",
			"avanti_region",
			"madhyadesa_region",
			"pracya_region",
			"vindhyaprstha_region",
			"dravida_region",
			"aparanta_region",
			"karnata_region"
	};
	
	public static final String[][] REGION_GROUPS_LIST = {
		GALLIC_BRITANNIC_REGIONS,
		IBERIAN_REGIONS,
		ITALIAN_AFRICAN_REGIONS,
		GERMANIC_REGIONS,
		DACIAN_ILLYRIAN_REGIONS,
		GREEK_ASIAN_REGIONS,
		EGYPTIAN_SYRIAN_REGIONS,
		EAST_AFRICAN_ARABIAN_REGIONS,
		SCYTHIAN_REGIONS,
		CAUCASIAN_REGIONS,
		PERSIAN_REGIONS,
		CENTRAL_ASIAN_REGIONS,
		INDIAN_REGIONS,
	};

	public static final Hashtable<String, String[]> REGION_GROUPS = new Hashtable<String, String[]>() {{
	    put("gallic_britannic_regions.txt", GALLIC_BRITANNIC_REGIONS);
	    put("iberian_regions.txt", IBERIAN_REGIONS);
	    put("italian_african_regions.txt", ITALIAN_AFRICAN_REGIONS);
	    put("germanic_regions.txt", GERMANIC_REGIONS);
	    put("dacian_illyrian_regions.txt", DACIAN_ILLYRIAN_REGIONS);
	    put("greek_asian_regions.txt", GREEK_ASIAN_REGIONS);
	    put("egyptian_syrian_regions.txt", EGYPTIAN_SYRIAN_REGIONS);
	    put("east_african_arabian_regions.txt", EAST_AFRICAN_ARABIAN_REGIONS);
	    put("scythian_regions.txt", SCYTHIAN_REGIONS);
	    put("caucasian_regions.txt", CAUCASIAN_REGIONS);
	    put("persian_regions.txt", PERSIAN_REGIONS);
	    put("central_asian_regions.txt", CENTRAL_ASIAN_REGIONS);
	    put("indian_regions.txt", INDIAN_REGIONS);
	}};
	
}
