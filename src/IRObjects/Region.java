package IRObjects;

import java.util.ArrayList;
import java.util.List;

import IRParser.ImperatorParser;


public class Region extends ImpObject {
	public List<Province> provinces = new ArrayList<Province>();

	public Region(String key) {
		super(key, ImperatorParser.localisation.get(key));
	}
	
	public Region(String key, String name) {
		super(key, name);
	}
	
	public String getGeneralArea() {
		if (key.equals("armorica_region") || key.equals("central_gaul_region") || key.equals("belgica_region")
				|| key.equals("aquitaine_region") || key.equals("transalpine_gaul_region")) {
			return ("Gaul");				
		}
		else if (key.equals("caledonia_region") || key.equals("britain_region")) {
			return ("Britannia");				
		}
		else if (key.equals("tarraconensis_region") || key.equals("contestania_region") || key.equals("baetica_region")
				|| key.equals("lusitania_region") || key.equals("gallaecia_region")) {
			return ("Iberia");				
		}
		else if (key.equals("cisalpine_gaul_region") || key.equals("central_italy_region") || key.equals("magna_graecia_region")) {
			return ("Italy");				
		}
		else if (key.equals("africa_region") || key.equals("numidia_region") || key.equals("mauretainia_region")) {
			return ("Africa");				
		}
		else {
			return name;
		}
	}
}