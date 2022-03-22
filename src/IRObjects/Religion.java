package IRObjects;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import IRParser.ImperatorParser;


public class Religion extends ImpObject {
	public int num_pops = 0;
	public int num_provinces = 0;
	public List<Country> countries = new ArrayList<Country>();

	public Religion(String key) {
		super(key, ImperatorParser.localisation.get(key));
	}
	
	public Religion(String key, String name) {
		super(key, name);
	}
}
