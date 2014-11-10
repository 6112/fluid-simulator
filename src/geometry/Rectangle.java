package geometry;

import java.awt.geom.Point2D;

/**
 * Classe permettant de creer facilement un Polygon en forme de rectangle.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 21 avril 2014
 */
public class Rectangle extends Polygon {
	/**
	 * Constructeur principal.
	 * 
	 * @param x Position du rectangle en x.
	 * @param y Position du rectangle en y.
	 * @param width Largeur du rectangle.
	 * @param height Hauteur du rectangle.
	 */
	public Rectangle(double x, double y, double width, double height) {
		super(
			new Point2D.Double(x, y),
			new Point2D.Double(x + width, y),
			new Point2D.Double(x + width, y + height),
			new Point2D.Double(x, y + height)
		);
	}
}
