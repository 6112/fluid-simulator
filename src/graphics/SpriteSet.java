package graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Classe qui permet de charger l'image du fluide et de lui donner une couleur sur une echelle
 * specifiee.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 25 mars 2014
 */
public class SpriteSet {
	// echelle de couleurs utilisees
	private ColorScale colorScale;
	
	// image non coloree
	private BufferedImage baseSprite;
	
	// images colorees, dans un tableau
	private BufferedImage[] sprites;
	
	/**
	 * Constructeur principal.
	 * 
	 * @param colorScale Echelle de couleurs utilisee.
	 * @param spriteCount Nombre de couleurs sur l'echelle (indice maximal plus 1).
	 * @param spriteFileName Chemin du fichier de l'image.
	 */
	public SpriteSet(ColorScale colorScale, int spriteCount, String spriteFileName) {
		this.colorScale = colorScale;
		sprites = new BufferedImage[spriteCount];
		loadSprites(spriteFileName);
	}
	
  // charge les images colorees dans le tableau et cree l'image de base
  private void loadSprites(String fileName) {
    // lire le fichier d'image
    URL file = getClass().getResource(fileName);
    ImageIcon icon = new ImageIcon(file);
    baseSprite = new BufferedImage(
        icon.getIconWidth(),
        icon.getIconHeight(),
        BufferedImage.TYPE_INT_ARGB);
    Graphics g = baseSprite.createGraphics();
    icon.paintIcon(null, g, 0, 0);
    g.dispose();
    // generer les images des differentes couleurs
    int hue;
    for (hue = 0; hue < sprites.length; hue++) {
      // image coloree a ajouter a spriteTable
      BufferedImage colored = new BufferedImage (baseSprite.getWidth (), 
          baseSprite.getHeight (), BufferedImage.TYPE_INT_ARGB);
      // pour chaque pixel dans la copie de l'image
      int x, y;
      for (y = 0; y < baseSprite.getHeight (); y++) {
        for (x = 0; x < baseSprite.getWidth (); x++) {
          // changer la couleur, mais pas la composante alpha
          int alpha = new Color (baseSprite.getRGB (x, y), true).getAlpha ();
          // affecter la couleur
          colored.setRGB (x, y, colorOfSpriteWithAlpha (hue, alpha).getRGB ());
        }
      }
      // mettre l'image coloree dans spriteTable
      sprites[hue] = colored;
    }
  }

	/**
	 * Retourne l'image a l'indice specifie. 0 est le premier indice, et spriteCount-1 est le 
	 * dernier.
	 * 
	 * @param index Indice de l'image.
	 * @return Image coloree a l'indice donne.
	 */
	public BufferedImage getSprite(int index) {
		return sprites[index];
	}
	
	// retourne une valeur reelle entre 0 et 1 associee a un indice dans le tableau d'images
	private double indexToFraction(int index) {
	  return (double) index / sprites.length;	
	}
	
	/**
	 * Retourne la couleur de l'image a l'indice specifie.
	 * 
	 * @param index Indice de l'image.
	 * @return Couleur de l'image donnee.
	 */
	public Color colorOfSprite(int index) {
		return colorOfSpriteWithAlpha(index,255);
	}
	
	/**
	 * Retourne la couleur de l'image a l'indice specifie, avec la valeur de transparence (alpha)
	 * specifiee.
	 * 
	 * @param index Indice de l'image.
	 * @param alpha Valeur d'alpha a donner a la couleur.
	 * @return Couleur de l'image donnee.
	 */
	public Color colorOfSpriteWithAlpha(int index, int alpha) {
		return colorScale.getColorWithAlpha(indexToFraction(index), alpha);
	}
}
