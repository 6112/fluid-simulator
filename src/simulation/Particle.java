package simulation;

import geometry.Vector;
import graphics.ColorScale;
import graphics.Drawable;
import graphics.SpriteSet;
import graphics.WorldMatrix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Classe representant une particule de fluide.
 * 
 * Possede une position, accessible avec getPositionVector() (pour avoir un Vector) ou avec getX() 
 * et getY().
 * 
 * De la meme facon, possede une vitesse, accessible avec getVelocityVector(), getVelocityX() et
 * getVelocityY().
 * 
 * Implante graphics.Drawable pour pouvoir etre dessine sur un objet Graphics2D.
 * 
 * @author Nicolas Ouellet-Payeur, Alexandre D'Amboise
 * @version 13 fevrier 2014
 */
public class Particle implements Drawable, Comparable<Particle> {
    // nombre d'instances de cette classe
    private static int particleCount = 0;
    
    // taille d'une particule lorsque dessinee en tout petit
    private final double SMALL_RECTANGLE_SIZE = 3;

    // taille d'une particule qui est dessinee comme un gros cercle quand toutes les autres sont
    // petites
	final double IMPORTANT_PARTICLE_SIZE = 14;
    
    // les constantes suivantes sont statiques car elles sont utilisees pour la creation des images
    // pour le rendu, qui sont partagees par toutes les instances
    
    // nom du fichier de l'image utilisee pour le dessin
    private static final String SPRITE_PATH = "/particle.png"; 
    
    // nombre de teintes de couleurs disponibles
    private static final int HUE_COUNT = 100;
    
    // pression a laquelle la couleur utilisee est la moins foncee
    private static double MINIMUM_PRESSURE = -800;
    
    // pression a laquelle la couleur utilisee est la plus foncee
    private static double MAXIMUM_PRESSURE = 0;
    
    // echelle de couleurs
    private static final ColorScale COLOR_SCALE = new ColorScale(new ColorScale.Pair[] {
        new ColorScale.Pair(Color.RED, 0),
       	new ColorScale.Pair(Color.GREEN, 0.4),
    	new ColorScale.Pair(Color.CYAN, 0.8),
    	new ColorScale.Pair(Color.BLUE, 1)
    });

    // couleur pour les particules mises en evidence
    private static final Color HIGHLIGHTED_COLOR = new Color (255, 255, 255);
    
    // echelle de couleurs pour les particules mises en evidence
    private static final ColorScale HIGHLIGHTED_COLOR_SCALE = new ColorScale(new ColorScale.Pair[] {
    	new ColorScale.Pair(HIGHLIGHTED_COLOR, 0),
    	new ColorScale.Pair(HIGHLIGHTED_COLOR, 1)
    });
 
    // ensemble des images a utiliser pour les particules qui ne sont pas mises en evidence
    private static SpriteSet spriteSet = null;
    
    // image a utiliser pour les particules mises en evidence
    private static SpriteSet highlightedSpriteSet = null;
    
    // identificateur unique pour cette particule
    private int id;
    
    // position dans le monde, en unites physiques
    private double x;
    private double y;
    
    // position a la derniere iteration de la simulation, en unites physiques
    private double previousX;
    private double previousY;
    
    // vitesse, en unites physiques
    private double velocityX;
    private double velocityY;

    // pseudo-pression de la particule: negative si la densite est trop basse, positive si elle est
    // trop haute
    private double pressure;

    // vrai ssi la particule doit etre dessinee en blanc (pour mieux visualiser les mouvements)
    private boolean highlighted;
    
    // vrai si et seulement si cette particule doit etre dessinee comme un petit carre d'un ou deux
    // pixels pour mieux visualiser les particules individuellement
    private boolean small = false;
    
    // vrai si cette particule doit etre dessinee comme un grand cercle meme en mode "small" car
    // l'utilisateur veut pouvoir suivre son progres
    private boolean important = false;
    
    // forces qui sont appliquees sur cette particule
    private Vector viscosityForce = Vector.NIL;
    private Vector normalForce = Vector.NIL;
    private Vector stiffnessForce = Vector.NIL;
    
    /**
     * Constructeur principal.
     * 
     * @param x Position sur l'axe des x.
     * @param y Position sur l'axe des y.
     * @param highlighted Vrai ssi la particule doit etre dessinee en blanc.
     */
    public Particle (double x, double y, boolean highlighted) {
        this.id = ++particleCount;
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.velocityX = 0;
        this.velocityY = 0;
        this.pressure = 0;
        this.highlighted = highlighted;
    }
    
    /**
     * Constructeur alternatif.
     * 
     * @param x Position sur l'axe des x.
     * @param y Position sur l'axe des y.
     */
    public Particle (double x, double y) {
        this (x, y, false);
    }
    
    /**
     * Charge les images utilisees pour le dessin des particules.
     */
    private static void loadSprites () {
        if (spriteSet == null) {
        	// creer les 100 images pour le degrade de couleurs
            spriteSet = new SpriteSet(COLOR_SCALE, HUE_COUNT, SPRITE_PATH);
            // creer l'image pour les particules mises en evidence
            highlightedSpriteSet = new SpriteSet(HIGHLIGHTED_COLOR_SCALE, 1, SPRITE_PATH);
        }
    }

    /**
     * Dessine la particule sur le Graphics2D voulu en utilisant la matrice de conversion world.
     * 
     * @param graphics Objet Graphics2D sur lequel dessiner.
     * @param world Objet WorldMatrix representant les conversions d'unites a utiliser.
     */
    public void draw (Graphics2D graphics, WorldMatrix world) {
        // generer les images si ce n'est pas deja fait
        Particle.loadSprites ();
        // si on doit dessiner les particules en plein
        if (! small) {
        	// position ou dessiner
        	Point2D.Double position = new Point2D.Double (x, y);
        	// position transformee ou dessiner
        	Point2D.Double transformed = (Point2D.Double) world.transform (position, null);
        	// image a utiliser pour le dessin
        	BufferedImage coloredSprite = chooseSprite ();
        	// dessiner cette image
        	graphics.drawImage (coloredSprite, 
        			(int) transformed.getX () - coloredSprite.getWidth () / 2, 
        			(int) transformed.getY () - coloredSprite.getHeight () / 2, 
        			null);
        }
        else {
        	if (! important) {
        		// utiliser un rectangle de taille 2 par 2 pour les particules
        		Ellipse2D.Double rectangle = new Ellipse2D.Double(x, y, 
        				SMALL_RECTANGLE_SIZE, SMALL_RECTANGLE_SIZE);
        		Shape transformed = world.transform(rectangle);
        		// utiliser la couleur qu'on aurait pris pour la meme image 
        		Color color = highlighted 
        				? HIGHLIGHTED_COLOR 
        						: spriteSet.colorOfSprite(chooseSpriteIndex());
        		graphics.setColor(color);
        		// dessiner le petit rectangle
        		graphics.fill(transformed);
        	}
        	else {
        		Ellipse2D.Double rectangle = new Ellipse2D.Double(
        				x - IMPORTANT_PARTICLE_SIZE / 2, y - IMPORTANT_PARTICLE_SIZE / 2, 
        				IMPORTANT_PARTICLE_SIZE, IMPORTANT_PARTICLE_SIZE);
        		Shape transformed = world.transform(rectangle);
        		Color color = spriteSet.colorOfSprite(chooseSpriteIndex());
        		graphics.setColor(color);
        		graphics.fill(transformed);
        	}
        }
    }
    
    // retourne l'indice de la couleur ou de l'image a utiliser pour le dessin
    private int chooseSpriteIndex () {
        double range = MAXIMUM_PRESSURE - MINIMUM_PRESSURE;
        double delta = (pressure - MINIMUM_PRESSURE) / range;
        int spriteIndex = HUE_COUNT - (int) (HUE_COUNT * delta);
        spriteIndex = Math.max (0, Math.min (spriteIndex, HUE_COUNT - 1));
        return spriteIndex;
    }

    // retourne l'image a utiliser pour le dessin
    private BufferedImage chooseSprite () {
        // si mis en evidence
        if (highlighted) {
            //return highlightedSprite;
        	return highlightedSpriteSet.getSprite(0);
        }
        // si pas mis en evidence
        else {
            // utiliser une image dont la couleur depend de la pression locale
            //return spriteTable.get (chooseSpriteIndex ());
        	return spriteSet.getSprite(chooseSpriteIndex());
        }
    }
    
    /**
     * Permet de deplacer la particule selon un certain intervalle de temps 
     * deltaT en fonction de la derniere position sauvegardee.
     * 
     * @param deltaT Intervalle de temps
     */
    public void advance (double deltaT) {
        // sauvegarder la "derniere position"
        setPreviousPositionVector (getPositionVector ());
        // deplacer d'apres la vitesse et l'intervalle de temps
        setPositionVector (getPositionVector ().plus (getVelocity ().times (deltaT)));
    }    

    /**
     * Compare cette particule a une autre, par identifiant unique.
     * 
     * @param that Autre particule.
     * @return Entier qui represente la relation (egal, plus petit, etc.)
     */
    public int compareTo (Particle that) {
        return that.id - this.id;
    }
    
    /**
     * Retourne la position a la derniere iteration de la simulation, en tant que vecteur, en unites
     * physiques.
     * 
     * @return Position qu'avait la particule la derniere fois que la simulation a genere une image.
     */
    public Vector getPreviousPositionVector () {
        return new Vector (previousX, previousY);
    }
    
    /**
     * Affecte la valeur de la position a la derniere iteration de la simulation, en tant que
     * vecteur, en unites physiques.
     * 
     * @param previousPosition Derniere position de la particule.
     */
    public void setPreviousPositionVector (Vector previousPosition) {
        previousX = previousPosition.getX ();
        previousY = previousPosition.getY ();
    }
    
    /**
     * Retourne la position de la particule, en tant que vecteur, en unites physiques.
     * 
     * @return Position de la particule.
     */
    public Vector getPositionVector () {
        return new Vector (x, y);
    }
    
    /**
     * Affecte la valeur de la position de la particule, en tant que vecteur, en unites physiques.
     * 
     * @param position Nouvelle position.
     */
    public void setPositionVector (Vector position) {
        x = position.getX ();
        y = position.getY ();
    }

    /**
     * Retourne la position sur l'axe des x, en unites physiques.
     * 
     * @return x Position sur l'axe des x.
     */
    public double getX () {
        return x;
    }

    /**
     * Affecte la position sur l'axe des x, en unites physiques.
     * 
     * @param x Nouvelle position sur l'axe des x.
     */
    public void setX (double x) {
        this.x = x;
    }

    /**
     * Retourne la position sur l'axe des y, en unites physiques.
     * 
     * @return Position sur l'axe des y.
     */
    public double getY () {
        return y;
    }

    /**
     * Affecte la position sur l'axe des y, en unites physiques.
     * 
     * @param y Nouvelle position sur l'axe des y.
     */
    public void setY (double y) {
        this.y = y;
    }
    
    /**
     * Retourne la vitesse, en tant que vecteur, en unites physiques.
     * 
     * @return Vitesse de la particule.
     */
    public Vector getVelocity () {
        return new Vector (velocityX, velocityY);
    }
    
    /**
     * Affecte la vitesse, en tant que vecteur, en unites physiques.
     * 
     * @param velocity Nouvelle vitesse.
     */
    public void setVelocity (Vector velocity) {
        velocityX = velocity.getX ();
        velocityY = velocity.getY ();
    }
    
    /**
     * Retourne la composante x de la vitesse, en unites physiques.
     * 
     * @return Composante x de la vitesse.
     */
    public double getVelocityX () {
        return velocityX;
    }
    
    /**
     * Affecte la composante x de la vitesse, en unites physiques.
     * 
     * @param velocityX Nouvelle composante x de la vitesse.
     */
    public void setVelocityX (double velocityX) {
        this.velocityX = velocityX;
    }
    
    /**
     * Retourne la composante y de la vitesse, en unites physiques.
     * 
     * @return Composante y de la vitesse.
     */
    public double getVelocityY () {
        return velocityY;
    }
    
    /**
     * Affecte la composante y de la vitesse, en unites physiques.
     * 
     * @param velocityY Nouvelle composante y de la vitesse.
     */
    public void setVelocityY (double velocityY) {
        this.velocityY = velocityY;
    }

    /**
     * Permet de determiner si cette particule doit etre mise en evidence ou non. 
     * 
     * @return vrai ou faux.
     */
    public boolean isHighlighted () {
        return highlighted;
    }
    
    /**
     * Affecte la valeur de verite de highlighted.
     * 
     * @param highlighted Nouvelle valeur de highlighted (vrai ou faux).
     */
    public void setHighlighted (boolean highlighted) {
        this.highlighted = highlighted;
    }
    
    /**
     * Retourne la valeur de la pression, en unites physiques
     * 
     * @return Pression appliquee sur la particule
     */
    public double getPressure () {
        return pressure;
    }
    
    /**
     * Affecte la valeur de la pression appliquee sur la particule.
     * 
     * @param pressure Pression appliquee sur la particule
     */
    public void setPressure (double pressure) {
        this.pressure = pressure;
    }
    
    /**
     * Retourne une chaine de caracteres representant l'emplacement de l'objet Particle.
     */
    public String toString () {
        return String.format ("Particle(%.2f,%.2f)", x, y);
    }
    
	/**
	 * Retourne vrai si cette particule est dessinee comme un petit carre de quelques pixels pour
	 * mieux visualiser les particules individuellement.
	 * 
	 * @return Vrai si la particule est petite.
	 */
	public boolean isSmall() {
		return small;
	}

	/**
	 * Permet de choisir si cette particule doit etre dessine comme un petit carre de quelques
	 * pixels pour mieux visualiser les particules individuellement.
	 * 
	 * @param isSmall Vrai si cette particule doit etre petite.
	 */
	public void setSmall(boolean isSmall) {
		this.small = isSmall;
	}
	
	/**
	 * Retourne un identifiant unique pour cette particule.
	 * 
	 * @return Identifiant unique pour cette particule.
	 */
	public int getId() {
		return id;
	}
	
	public void setImportant(boolean important) {
	    this.important = important;
	}

	/**
	 * Retourne la force de viscosite de la particule.
	 * 
	 * @return viscosityForce la force de viscosite.
	 */
	public Vector getViscosityForce() {
		return viscosityForce;
	}

	/**
	 * Affecte une nouvelle valeur de viscosite a la particule.
	 * 
	 * @param viscosityForce la force de viscosite a appliquer.
	 */
	public void setViscosityForce(Vector viscosityForce) {
		this.viscosityForce = viscosityForce;
	}
	
	/**
	 * Retourne la force normale appliquee sur la particule.
	 * 
	 * @return La force normale exercee sur la particule.
	 */
	public Vector getNormalForce() {
		return normalForce;
	}
	
	/**
	 * Affecte la valeur de la force normale appliquee sur la particule.
	 * 
	 * @param normalForce Nouvelle force normale exercee sur la particule.
	 */
	public void setNormalForce(Vector normalForce) {
		this.normalForce = normalForce;
	}
	
	/**
	 * Retourne la valeur de la force de rigidite appliquee sur la particule.
	 * 
	 * @return Force de rigidite appliquee sur la particule.
	 */
	public Vector getStiffnessForce() {
		return stiffnessForce;
	}
	
	/**
	 * Affecte la valeur de la force de rigidite appliquee sur la particule.
	 * 
	 * @param stiffnessForce Force de rigidite appliquee sur la particule.
	 */
	public void setStiffnessForce(Vector stiffnessForce) {
		this.stiffnessForce = stiffnessForce;
	}
}
