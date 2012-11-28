package com.ljremote.android.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArrayHelper {

	public static List<HashMap<String, String>> convertToListMap(String[][] data) {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		HashMap<String, String> element;
		// Pour chaque personne dans notre r�pertoire�
		for (int i = 0; i < data.length; i++) {
			// � on cr�e un �l�ment pour la liste�
			element = new HashMap<String, String>();
			/*
			 * � on d�clare que la cl� est � text1 � (j'ai choisi ce mot au
			 * hasard, sans sens technique particulier) pour le nom de la
			 * personne (premi�re dimension du tableau de valeurs)�
			 */
			element.put("text1", data[i][0]);
			/*
			 * � on d�clare que la cl� est � text2 � pour le num�ro de cette
			 * personne (seconde dimension du tableau de valeurs)
			 */
			element.put("text2", data[i][1]);
			list.add(element);
		}
		return list;
	}
}
