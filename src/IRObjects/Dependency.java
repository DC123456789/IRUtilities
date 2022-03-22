package IRObjects;

public class Dependency {
	public String suzerain, subject, subjectType;
	
	public Dependency(String suzerain, String subject, String subjectType) {
		this.suzerain = suzerain;
		this.subject = subject;
		this.subjectType = subjectType;
	}
}
