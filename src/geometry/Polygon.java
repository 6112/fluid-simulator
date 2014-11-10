package geometry;

import graphics.Drawable;
import graphics.WorldMatrix;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Classe permettant de faire des calculs de distance relies a un polygone et de le dessiner sur
 * un objet Graphics2D.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 21 avril 2014
 */
public class Polygon implements Drawable {
	// tableau contenant les segments qui forment le polygone
	private Segment[] segments;
	
	/**
	 * Constructeur principal.
	 * 
	 * @param points Points qui constituent le polygone, en sens horaire.
	 */
	public Polygon (Point2D.Double... points) {
		segments = new Segment[points.length];
		int i;
		for (i = 0; i < points.length - 1; i++) {
			setSegment(i, points[i], points [i + 1]);
		}
		setSegment(points.length - 1, points[points.length - 1], points[0]);
	}
	
	/**
	 * Retourne la distance entre le polygone et le point donne.
	 * 
	 * @param x x du point donne.
	 * @param y y du point donne.
	 * @return La distance entre le polygone et (x, y) ou 0, si le point est dans le polygone.
	 */
	public double distance(double x, double y) {
		if (contains(x, y)) {
			return 0;
		}
		else {
			Segment nearest = getNearestSegment(x, y);
			return nearest.distance(x, y);
		}
	}
	
	/**
	 * Retourne vrai ssi le point (x, y) est dans le polygone.
	 * 
	 * @param x x du point donne.
	 * @param y y du point donne.
	 * @return Vrai ssi (x, y) est dans le polygone.
	 */
	public boolean contains (double x, double y) {
		Vector point = new Vector(x, y);
		int i;
		for (i = 0; i < segments.length; i++) {
			Segment segment = segments[i];
			Vector origin = new Vector(segment.getX(), segment.getY());
			Vector direction = segment.getDirection();
			Vector displaced = point.minus(origin);
			if (! displaced.isToTheRight(direction)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Retourne le segment du polygone qui est le plus pres d'un point donne.
	 * 
	 * @param x x du point donne.
	 * @param y y du point donne.
	 * @return Segment le plus pres de (x, y).
	 */
	private Segment getNearestSegment (double x, double y) {
		Segment nearest = segments[0];
		int i;
		for(i = 1; i < segments.length; i++) {
			if(segments[i].distance(x, y) < nearest.distance(x, y)) {
				nearest = segments[i];
			}
		}
		return nearest;
	}
	
	/**
	 * Retourne la profondeur de la penetration d'un point dans le polygone.
	 * 
	 * @param x x du point donne.
	 * @param y y du point donne.
	 * @return Profondeur de la penetration du poiminimusnt (x, y) dans le polygone.
	 */
	public double penetrationDepth(double x, double y) {
		Segment nearest = getNearestSegment(x, y);
		return nearest.distance(x, y);
	}
	
	/**
	 * Retourne le vecteur normal au polygone au point (x, y).
	 * 
	 * @param x x du point donne.
	 * @param y y du point donne.
	 * @return Vecteur normal au polygone.
	 */
	public Vector normal(double x, double y) {
		Segment nearest = getNearestSegment(x, y);
		Vector d = nearest.getDirection();
		return new Vector(- d.getY(), d.getX()).normalize(1);
	}
	
	/**
	 * Dessine le polygone sur l'objet Graphics2D avec la matrice de transformation donnee.
	 */
	public void draw(Graphics2D graphics, WorldMatrix world) {
		Path2D.Double path = new Path2D.Double();
		path.moveTo(segments[0].getX(), segments[0].getY());
		int i;
		for(i = 1; i < segments.length; i++) {
			path.lineTo(segments[i].getX(), segments[i].getY());
		}
		Shape transformed = world.transform(path);
		graphics.fill(transformed);
	}
	
	// utilise par le constructeur pour cosntruire les segments
	private void setSegment(int index, Point2D.Double p1, Point2D.Double p2) {
		segments[index] = new Segment(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
}
