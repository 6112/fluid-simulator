package graphics;

import java.awt.Graphics2D;

/**
 * Interface permettant aux objets de se dessiner selon la matrice
 * monde-vers-composant (world).
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 13 fevrier 2014
 * 
 */
public interface Drawable {
    /**
     * Dessine la particule sur le Graphics2D voulu en utilisant la matrice de conversion world.
     * 
     * @param graphics Objet Graphics2D sur lequel dessiner.
     * @param world Objet WorldMatrix representant les conversions d'unites a utiliser.
     */
    public void draw (Graphics2D graphics, WorldMatrix world);
}
