package IRObjects;

import java.awt.Color;
import java.awt.geom.Point2D;


public class CoaEmblem {
	public String pattern;
	public Color color1;
	public Color color2;
	public double rotation;
	public Point2D.Double scale;
	public Point2D.Double position;
	public boolean[] mask; 
	
	public CoaEmblem(String pattern, Color color1, Color color2, double rotation, Point2D.Double scale, 
			Point2D.Double position, boolean[] mask) {
		this.pattern = pattern;
		this.color1 = color1;
		this.color2 = color2;
		this.rotation = rotation;
		this.scale = scale;
		this.position = position;
		this.mask = mask;
	}
}
