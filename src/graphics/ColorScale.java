package graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Decrit une echelle de couleur qui associe des valeurs reelles entre 0 et 1 a des couleurs sur une
 * echelle degradee. Permet de specifier plus que deux couleurs pour le degrade, a des positions
 * precises sur l'echelle de couleurs.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 7 avril 2014
 */
public class ColorScale {
	// liste des paires de couleurs constituant l'echelle
	private ArrayList<Pair> pairs;
	
	/**
	 * Constructeur principal. Prend comme parametre un liste des paires valeur-couleur sur 
	 * l'echelle. Voir la classe ColorScale.Pair pour voir comment creer une telle paire.
	 * 
	 * @param pairs Liste des paires de couleurs et des valeurs sur l'echelle qui leur sont 
	 *   associees.
	 */
	public ColorScale (Pair[] pairs) {
		// creer un ArrayList des paires de couleurs
		this.pairs = new ArrayList<Pair>();
		int i;
		for (i = 0; i < pairs.length; i++) {
			Pair pair = pairs[i];
			this.pairs.add(pair);
		}
		// ordonner l'ArrayList par position sur l'echelle
		Collections.sort(this.pairs);
	}
	
	// retourne les deux couleurs "autour" du point sur l'echelle
	private Range getRange(double point) {
		// trouver la premiere couleur qui a une valeur plus grande que le point
		int index = 0;
		while (index < pairs.size() && pairs.get(index).point <= point) {
			index++;
		}
		index--;
		// s'assurer qu'il n'y aura pas d'erreur d'indice
		index = Math.min(Math.max(index, 0), pairs.size() - 2);
		// retourner les deux couleurs
		return new Range(pairs.get(index), pairs.get(index + 1));
	}

	/**
	 * Retourne la couleur d'une valeur sur l'echelle, avec la valeur d'alpha maximale. Voir aussi
	 * getColorWithAlpha().
	 * 
	 * @param point Point sur l'achelle de couleurs.
	 * @return Couleur sur l'echelle au point donne.
	 */
	public Color getColor(double point) {
		return getColorWithAlpha(point, 255);
	}
	
	/**
	 * Retourne la couleur d'une valeur sur l'echelle, avec la valeur d'alpha desiree.
	 * 
	 * @param point Point sur l'echelle de couleurs.
	 * @param alpha Valeur d'alpha voulue pour la couleur.
	 * @return Couleur sur l'echelle au point donne, avec la valeur d'alpha donnee.
	 */
	public Color getColorWithAlpha(double point, int alpha) {
		// lancer une exception si la position n'est pas sur l'echelle
		if (point < 0 || point > 1) {
			throw new IllegalArgumentException("point doit etre entre 0 et 1.");
		}
		// couleurs du degrade
		Range range = getRange(point);
		Color startColor = range.start.color;
		Color endColor = range.end.color;
		// deplacer le point pour qu'il soit "entre" startColor et endColor
		point = point - range.start.point;
		// difference de la position entre les deux couleurs sur l'echelle
		double rangeSize = range.end.point - range.start.point;
		// difference de couleur entre les deux couleurs
		int deltaRed = endColor.getRed() - startColor.getRed();
		int deltaGreen = endColor.getGreen() - startColor.getGreen();
		int deltaBlue = endColor.getBlue() - startColor.getBlue();
		// valeurs des composantes de la couleur retournee
		int red = startColor.getRed() + (int) (deltaRed * point / rangeSize);
		int green = startColor.getGreen() + (int) (deltaGreen * point / rangeSize);
		int blue = startColor.getBlue() + (int) (deltaBlue * point / rangeSize);
		// retourner la couleur, avec la valuer d'alpha specifiee
		return new Color(red, green, blue, alpha);
	}
	
	// Classe qui decrit deux paires de couleur qui sont "autour" d'une certaine valeur sur 
	//l'echelle.
	private static class Range {
		// couleur du debut
		private Pair start;
		
		// couleur de la fin
		private Pair end;
		
		// constructeur principal
		private Range (Pair start, Pair end) {
			// lancer une exception si la position n'est pas sur l'echelle
			this.start = start;
			this.end = end;
		}
	}
	
	/**
	 * Classe representant une couleur sur l'echelle. Associe une position sur l'echelle de couleurs
	 * (valeur reelle entre 0 et 1) a une couleur.
	 * 
	 * @author Nicolas Ouellet-Payeur
	 * @version 7 mars 2014
	 */
	public static class Pair implements Comparable<Pair> {
		// couleur associee
		private Color color;
		
		// position sur l'echelle de couleurs
		private double point;
		
		/**
		 * Constructeur principal.
		 * 
		 * @param color Couleur associee a la position sur l'echelle.
		 * @param point Position sur l'echelle de couleurs.
		 */
		public Pair (Color color, double point) {
			if (point < 0 || point > 1) {
				throw new IllegalArgumentException("point doit etre entre 0 et 1.");
			}
			this.color = color;
			this.point = point;
		}
		
		/**
		 * Permet d'ordonner les couleurs par leur position sur l'echelle.
		 */
		public int compareTo (Pair that) {
			if (this.point < that.point) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}
}