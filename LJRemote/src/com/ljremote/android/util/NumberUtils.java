package com.ljremote.android.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.util.Log;

public class NumberUtils {

	public NumberUtils() {
	}

	public static Integer[] range(int start, int end, int increment) {
		boolean ascendent_order = start <= end ? true : false;
		int trueStart = ascendent_order ? start : end;
		int trueEnd    = ascendent_order ? end : start;
		int trueIncrement = (ascendent_order ? +1 : -1*increment)*Math.abs(increment);
		int size = trueEnd - trueStart + 1;
		Integer[] range = new Integer[size];
		int number = trueStart;
		for (int i = 0; i < size; i++) {
			range[i] = number;
			number += trueIncrement;
		}
		return range;
	}

	public static Integer[] range(int start, int end, int increment, Integer[] exclude) {
//		boolean ascendent_order = start <= end ? true : false;
//		int trueStart = ascendent_order ? start : end;
//		int trueEnd    = ascendent_order ? end : start;
//		int trueIncrement = (ascendent_order ? +1 : -1*increment)*Math.abs(increment);
//		int size = trueEnd - trueStart + 1;
//		Integer[] range = new Integer[size];
//		ArrayList<Integer> excludeInts;
//		if( exclude != null ) {
//			excludeInts = new ArrayList<Integer>(Arrays.asList(exclude));
//			Collections.sort(excludeInts);
//		} else {
//			excludeInts = new ArrayList<Integer>();
//		}
//		int number = trueStart;
//		int found;
//		for (int i = 0; i < size; i++) {
//			if ( (found = excludeInts.indexOf(number)) != -1) {
//				excludeInts.remove(found);
//			} else {
//				range[i] = number;
//			}
//			number += trueIncrement;
//		}
		List<Integer> range = rangeList(start, end, increment, Arrays.asList(exclude));
		return (Integer[]) range.toArray(new Integer[range.size()]);
	}
	
	public static List<Integer> rangeList(int start, int end, int increment, List<Integer> exclude) {
		boolean ascendent_order = start <= end ? true : false;
		int trueStart = ascendent_order ? start : end;
		int trueEnd    = ascendent_order ? end : start;
		int trueIncrement = (ascendent_order ? +1 : -1*increment)*Math.abs(increment);
		int size = trueEnd - trueStart + 1;
		ArrayList<Integer> range = new ArrayList<Integer>();
		ArrayList<Integer> excludeInts;
		if( exclude != null ) {
			excludeInts = new ArrayList<Integer>(exclude);
			Collections.sort(excludeInts);
		} else {
			excludeInts = new ArrayList<Integer>();
		}
		int number = trueStart;
		int found;
		for (int i = 0; i < size; i++) {
			if ( (found = excludeInts.indexOf(number)) != -1) {
				excludeInts.remove(found);
			} else {
				range.add(number);
			}
			number += trueIncrement;
		}
		return range;
	}

	public static Integer[] range(int start, int end, Integer[] exclude) {
		return range(start, end, 1, exclude);
	}
	public static Integer[] range(int start, int end) {
		return range(start, end, 1);
	}
	
	public static List<Integer> rangeList(int start, int end, int increment) {
		return Arrays.asList(range(start, end, increment));
	}

	public static List<Integer> rangeList(int start, int end) {
		return rangeList(start, end, 1);
	}

	public static Integer[] range(int end) {
		return range(0, end);
	}

	public static List<Integer> rangeList(int end) {
		return rangeList(0, end);
	}
}
