package IRObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Culture extends ImpObject implements CulturalNamer {
	public int num_pops = 0;
	public int num_provinces = 0;
	public List<Country> countries = new ArrayList<Country>();
	public CultureGroup group;
	public LevyTemplate levyTemplate = null;

	public Culture(String key) {
		super(key, "");
	}
	
	public Culture(String key, String name) {
		super(key, name);
	}
	
	public LevyTemplate getLevyTemplate() {
		if (!Objects.isNull(levyTemplate)) {
			return levyTemplate;				
		}
		else
			return group.levyTemplate;
	}
}
