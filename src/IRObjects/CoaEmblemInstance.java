package IRObjects;

import java.awt.geom.Point2D;

public class CoaEmblemInstance {
	public double rotation;
	public Point2D.Double scale;
	public Point2D.Double position;
	
	public CoaEmblemInstance(double rotation, Point2D.Double scale, Point2D.Double position) {
		this.rotation = rotation;
		this.scale = scale;
		this.position = position;
	}
}
