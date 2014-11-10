package geometry;

/**
 * Classe de geometrie permettant de creer des segments et de determiner la distance entre deux
 * vecteurs de position. Cela permettra de determiner la distance entre un "mur" et une particule
 * lors de l'execution de la simulation et d'en calculer la prochaine position.
 * 
 * @author Alexandre D'Amboise
 * @version 20 mars 2014
 */
public class Segment {
	// position du segment
	private double x;
	private double y;

	// vecteur directeur
	private Vector d;

	/**
	 * Constructeur principal. Permet de creer un segment sous forme de vecteur.
	 * 
	 * @param x1 La position x initiale.
	 * @param y1 La position y initiale.
	 * @param x2 La position x finale.
	 * @param y2 La position y finale.
	 */
	public Segment(double x1, double y1, double x2, double y2){
		this.x = x1;
		this.y = y1;
		this.d = new Vector (x2 - x1, y2 - y1);
	}

	/**
	 * Methode permettant de calculer la distance entre un segment et un point a partir de vecteurs.
	 * 
	 * @param px La position x du point.
	 * @param py La position y du point.
	 * @return La distance entre le point (px,py) et le segment.
	 */
	public double distance (double px, double py) {
		Vector AP = new Vector(px, py).minus(new Vector(x, y));
		Vector QP = (AP.projectedOn(d)).negative().plus(AP);
		Vector AQ = (AP.plus(QP.negative()));
		Vector OB = new Vector (x, y).plus(d);
		Vector BP = new Vector(px, py).minus(OB);
		
		double distance = QP.getMagnitude();
		
		double k;
		if (d.getX() != 0) {
			k = AQ.getX() / d.getX();
		}
		else {
			k = AQ.getY() / d.getY();
		}
		
		if (k < 0) {
			return AP.getMagnitude();
		}
		else if (k > 1) {
			return BP.getMagnitude();
		}
		else {
			return distance;
		}
	}
	/**
	 * Retourne la valeur de la position en x du point.
	 * 
	 * @return x La position horizontale du point.
	 */
	public double getX() {
		return x;
	}
	/**
	 * Retourne la valeur de la position en y du point.
	 * 
	 * @return y La position verticale du point.
	 */
	public double getY() {
		return y;
	}
	/**
	 * Retourne le vecteur direction, soit l'orientation du segment.
	 * 
	 * @return d Un vecteur direction.
	 */
	public Vector getDirection(){
		return d;
	}
	
	/**
	 * Represent cet objet avec une chaine de caracteres.
	 */
	public String toString() {
		return String.format("Segment(%s, %s)", new Vector(x, y), new Vector(x, y).plus(d));
	}
}
