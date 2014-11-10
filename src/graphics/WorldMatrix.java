package graphics;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Classe qui implemente les methodes necessaires a la creation d'une matrice
 * monde-vers-composant, et qui comporte les methodes pouvant etre utiles 
 * dans d'autres classes.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 13 fevrier 2014
 */
public class WorldMatrix extends AffineTransform implements Serializable {
    public static final long serialVersionUID = 1L;
    
    // x minimal du monde
    private double minimumX;
    // y minimal du monde
    private double minimumY;

    // largeur du monde
    private double width;
    // hauteur du monde
    private double height;

    // matrice inverse
    private AffineTransform inverse;
    
    /**
     * Constructeur principal.
     * 
     * @param minimumX Valeur minimale de x
     * @param minimumY Valeur minimale de y
     * @param width Largeur
     * @param height Hauteur 
     * @param componentWidth Largeur du composant 
     * @param componentHeight Hauteur du composant
     */
    public WorldMatrix (double minimumX, double minimumY, double width, double height, 
            double componentWidth, double componentHeight) {
        super ();
        // affecter les valeurs donnees
        this.minimumX = minimumX;
        this.minimumY = minimumY;
        this.width = width;
        this.height = height;
        // pixels par unite physique
        double pixelsPerUnitX = componentWidth / width;
        double pixelsPerUnitY = componentHeight / height;
        // deplacer de la hauteur du composant
        translate (0, componentHeight);
        // inverser le sens
        scale (pixelsPerUnitX, - pixelsPerUnitY);
        // translation selon x/y minimaux
        translate (- minimumX, - minimumY);
        // creer la matrice inverse pour plus tard
        try {
            inverse = createInverse ();
        }
        catch (NoninvertibleTransformException error) {
            System.exit (0);
        }
    }
    
    /**
     * Retourne la matrice identite.
     * 
     * @param componentWidth Largeur du composant.
     * @param componentHeight Hauteur du composant.
     * @return La matrice identite.
     */
    public static WorldMatrix getIdentity (double componentWidth, double componentHeight) {
        return new WorldMatrix (0, 0, componentWidth, componentHeight, 
                componentWidth, componentHeight);
    }
    
    /**
     * Transforme une forme.
     * 
     * @param shape Forme a transformer.
     * @return Forme transformee
     */
    public Shape transform (Shape shape) {
        return createTransformedShape (shape);
    }
    
    /**
     * Effectue l'operation inverse sur un point.
     * 
     * @param point Point a transformer.
     * @return Point transforme par la matrice inverse.
     */
    public Point2D.Double detransform (Point2D.Double point) {
        Point2D.Double detransformedPoint = new Point2D.Double ();
        inverse.transform (point, detransformedPoint);
        return detransformedPoint;
    }
    
    /**
     * Effectue l'operation inverse sur une forme.
     * 
     * @param shape Forme a transforme.
     * @return Forme transformee par la matrice inverse.
     */
    public Shape detransform (Shape shape) {
        return inverse.createTransformedShape (shape);
    }
    
    /**
     * Retourne la largeur du monde.
     * 
     * @return Largeur du monde.
     */
    public double getWidth () {
        return width;
    }
    
    /**
     * Retourne la hauteur du monde.
     * 
     * @return Hauteur du monde.
     */
    public double getHeight () {
        return height;
    }
    
    /**
     * Retourne le x minimal.
     * 
     * @return Le x minimal.
     */
    public double getMinimumX () {
        return minimumX;
    }
    
    /**
     * Retourne le y minimal.
     * 
     * @return Le y minimal.
     */
    public double getMinimumY () {
        return minimumY;
    }
    
    /**
     * Retourne le x maximal.
     * 
     * @return Le x maximal.
     */
    public double getMaximumX () {
        return minimumX + width;
    }
    
    /**
     * Retourne le y maximal.
     * 
     * @return Le y maximal.
     */
    public double getMaximumY () {
        return minimumY + height;
    }
}
