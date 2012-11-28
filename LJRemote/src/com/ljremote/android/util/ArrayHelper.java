package com.ljremote.android.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArrayHelper {

	public static List<HashMap<String, String>> convertToListMap(String[][] data) {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		HashMap<String, String> element;
		// Pour chaque personne dans notre répertoire…
		for (int i = 0; i < data.length; i++) {
			// … on crée un élément pour la liste…
			element = new HashMap<String, String>();
			/*
			 * … on déclare que la clé est « text1 » (j'ai choisi ce mot au
			 * hasard, sans sens technique particulier) pour le nom de la
			 * personne (première dimension du tableau de valeurs)…
			 */
			element.put("text1", data[i][0]);
			/*
			 * … on déclare que la clé est « text2 » pour le numéro de cette
			 * personne (seconde dimension du tableau de valeurs)
			 */
			element.put("text2", data[i][1]);
			list.add(element);
		}
		return list;
	}
}
