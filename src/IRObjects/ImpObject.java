package IRObjects;
import java.util.Objects;


public abstract class ImpObject implements Comparable<ImpObject> {
	public String name, key;
	
	public ImpObject(String key, String name) {
		this.key = key;
		this.name = name;			
	}
	
	@Override
	public int compareTo(ImpObject otherObject) {
		if (Objects.isNull(name)) {
			System.out.println("Broken imp object name for " + this.key);
		}
		if (Objects.isNull(otherObject.name)) {
			System.out.println("Broken imp object name for " + otherObject.key);
		}
		return name.compareTo(((ImpObject)otherObject).name);
	}
	
	@Override
	public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
	    ImpObject other = (ImpObject)o;
	    return other.key.equals(this.key);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
