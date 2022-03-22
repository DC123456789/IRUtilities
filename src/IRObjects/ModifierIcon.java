package IRObjects;


public class ModifierIcon {
	public String key;
	private String icon;

	public ModifierIcon(String key) {
		this.key = key;
	}

	public void setIcon(String icon) {
		icon = icon.replace("happyness", "happiness");
		icon = icon.replace("population happiness", "happiness");
		icon = icon.replace("manpower modifier", "manpower");
		icon = icon.replace("population capacity modifier", "population capacity");
		icon = icon.replace("local noble", "pop noble");
		icon = icon.replace("local citizen", "pop citizen");
		icon = icon.replace("local freeman", "pop freemen");
		icon = icon.replace("local tribesmen", "pop tribesmen");
		icon = icon.replace("local slave", "pop slaves");
		if (!icon.equals("local defensive"))
			icon = icon.replaceFirst("local ", "");
		if (icon.equals("country civilization value")) {
			this.icon = "civilization";
		}
		else if (icon.equals("pirate spawn chance")) {
			this.icon = "pirate haven";
		}
		else if (icon.equals("barbarian growth")) {
			this.icon = "barbarian power";
		}
		else {
			this.icon = icon;
		}
	}

	public String getIcon() {
		return icon;
	}
	
	@Override
	public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModifierIcon other = (ModifierIcon)o;
	    return other.key.equals(this.key);
	}
}
