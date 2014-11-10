package geometry;

import graphics.Drawable;
import graphics.WorldMatrix;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Classe immuable representant un vecteur geometrique.
 * 
 * Possede deux accesseurs, getX() et getY(), pour obtenir la valeur de ces composantes.
 * 
 * Permet la plupart des operations usuelles sur les vecteurs: multiplication par un scalaire avec
 * .times(), addition avec .plus()...
 * 
 * Pour evaluer une expression contenant des vecteurs, on utilise des methodes enchainees de la meme
 * facon que si on devait lire l'expression a voix haute en anglais. Par exemple, l'equation:
 * 
 *   u = v + 2w - (v . w) v
 *   
 * se traduit:
 * 
 *   Vector u = v.plus (w.times (2)).minus (v.times (v.scalarProduct (w)));
 *   
 * @author Nicolas Ouellet-Payeur
 * @version 13 fevrier 2014
 */
public class Vector implements Drawable {
    // vecteur nul
    public static final Vector NIL = new Vector (0, 0);
    
    // taille du bout de la fleche du vecteur lorsque dessine
    private final double ARROW_TIP_SIZE = 10;
    
    // composantes du vecteur sur l'axe des x et l'axe des y
    private double x;
    private double y;
    
    /**
     * Constructeur principal. Cree un vecteur avec les composantes specifiees.
     * 
     * @param x Composante x.
     * @param y Composante y.
     */
    public Vector (double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Constructeur alternatif. Cree un vecteur a partir d'un Point2D.Double.
     * 
     * @param point Le point qui correspond aux composantes du vecteur.
     */
    public Vector (Point2D.Double point) {
        this (point.getX (), point.getY ());
    }

    /**
     * Retourne l'angle du vecteur, en radians.
     * 
     * @return Angle du vecteur, en radians.
     */
    public double getAngle() {
    	return Math.atan2(y, x);
    }
    
    /**
     * Retourne la norme du vecteur.
     * 
     * @return Norme du vecteur.
     */
    public double getMagnitude () {
        return Math.sqrt (x * x + y * y);
    }
    
    // Operations de base sur les vecteurs commencent ici

    /**
     * Retourne l'oppose de ce vecteur.
     * 
     * @return Vecteur oppose.
     */
    public Vector negative () {
        return times (-1);
    }
    
    /**
     * Retourne la somme de ce vecteur et d'un autre vecteur.
     * 
     * @param addend Autre terme de l'addition.
     * @return Somme des vecteurs.
     */
    public Vector plus (Vector addend) {
        return new Vector (x + addend.x, y + addend.y);
    }

    /**
     * Retourne la difference de ce vecteur et d'un autre vecteur.
     * 
     * @param subtrahend Terme a soustraire.
     * @return Difference des deux vecteurs.
     */
    public Vector minus (Vector subtrahend) {
        return this.plus (subtrahend.negative ());
    }

    /**
     * Retourne ce vecteur multiplie par un nombre reel.
     * 
     * @param multiplicand Facteur de la multiplication.
     * @return Le resultat du produit.
     */
    public Vector times (double multiplicand) {
        return new Vector (multiplicand * x, multiplicand * y);
    }
    
    /**
     * Retourne la projection orthogonale de ce vecteur sur un autre.
     * 
     * @param base Le vecteur sur lequel projeter ce vecteur.
     * @return Ce vecteur, projete sur base.
     */
    public Vector projectedOn (Vector base) {
        double coefficient = this.scalarProduct (base) / base.scalarProduct (base);
        return base.times (coefficient);
    }
    
    /**
     * Retourne ce vecteur, ajuste pour avoir la norme voulue.
     * 
     * @param magnitude Norme voulue.
     * @return Ce vecteur, ajuste pour avoir la norme voulue.
     */
    public Vector normalize (double magnitude) {
        return this.times (magnitude / getMagnitude ());
    }
    
    /**
     * Retourne le produit scalaire de ce vecteur et d'un autre vecteur.
     * 
     * @param multiplier Autre facteur du produit scalaire.
     * @return Produit scalaire des deux vecteurs.
     */
    public double scalarProduct (Vector multiplier) {
        return x * multiplier.x + y * multiplier.y;
    }
    
    /**
     * Test d'egalite entre ce vecteur et un objet.
     * 
     * @param thatObject Objet a comparer avec ce vecteur.
     * @return true ssi les deux vecteurs sont egaux.
     */
    public boolean equals (Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (! (thatObject instanceof Vector)) {
            return false;
        }
        Vector that = (Vector) thatObject;
        return x == that.x && y == that.y;
    }
    
    /**
     * Fonction utilisee pour dessiner le vecteur.
     */
    public void draw(Graphics2D graphics, WorldMatrix world) {
    	Path2D.Double path = new Path2D.Double();
    	path.moveTo(0, 0);
    	path.lineTo(x, y);
    	double a = getAngle() - 7 * Math.PI / 8;
    	path.lineTo(x + ARROW_TIP_SIZE * Math.cos(a), y + ARROW_TIP_SIZE * Math.sin(a));
    	path.moveTo(x, y);
    	a = getAngle() + 7 * Math.PI / 8;
    	path.lineTo(x + ARROW_TIP_SIZE * Math.cos(a), y + ARROW_TIP_SIZE * Math.sin(a));
    	Shape transformedShape = world.transform(path);
    	graphics.draw(transformedShape);
    }
    
    /**
     * Retourne la composante x de ce vecteur.
     * 
     * @return Composante x de ce vecteur.
     */
    public double getX () {
        return x;
    }
    
    /**
     * Retourne la composante y de ce vecteur.
     * 
     * @return Composante y de ce vecteur.
     */
    public double getY () {
        return y;
    }

    /**
     * Convertit ce vecteur en chaine de caracteres de la forme "(x y)".
     * 
     * @return Chaine de caracteres representant ce vecteur.
     */
    public String toString () {
        return String.format ("(%.2f %.2f)", x, y);
    }
    
    /**
     * Retourne vrai si le vecteur 'that' est a droite de ce vecteur.
     * 
     * @param that Autre vecteur.
     * @return Vrai si 'that' est a droite de ce vecteur.
     */
    public boolean isToTheRight(Vector that){
    	return (this.x * that.y - this.y * that.x) < 0 ;	
    }    
}