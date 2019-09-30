package ru.hh.search.nparray;

import java.util.Arrays;

public class NpArrays extends NpBase {

  private static final String NAME_NOT_FOUND_DESCRIPTION = "Name not found";

  protected float[][][] floatsArrays;
  protected int[][][] intsArrays;
  protected String[][][] stringsArrays;

  public NpArrays(String version, int initSizeInt, int initSizeFloat, int initSizeString) {
    super(version, initSizeInt, initSizeFloat, initSizeString);
    floatsArrays = new float[initSizeFloat][][];
    intsArrays = new int[initSizeInt][][];
    stringsArrays = new String[initSizeString][][];
  }

  public NpArrays() {
    this(NpBase.ACTUAL_VERSION, 10, 10, 10);
  }

  public void add(float[][] array, String name) {
    checkNeedResizeFloats();
    nameFloatArrays[floatPosition] = name;
    floatsArrays[floatPosition] = array;
    floatPosition++;
  }

  public void add(int[][] array, String name) {
    checkNeedResizeInts();
    nameIntArrays[intPosition] = name;
    intsArrays[intPosition] = array;
    intPosition++;
  }

  public void add(String[][] array, String name) {
    checkNeedResizeStrings();
    nameStringArrays[stringPosition] = name;
    stringsArrays[stringPosition] = array;
    stringPosition++;
  }

  private void checkNeedResizeFloats() {
    if (nameFloatArrays.length - 1 == floatPosition) {
      floatsArrays = Arrays.copyOf(floatsArrays, floatPosition * 2);
      nameFloatArrays = Arrays.copyOf(nameFloatArrays, floatPosition * 2);
    }
  }

  private void checkNeedResizeInts() {
    if (nameIntArrays.length - 1 == intPosition) {
      intsArrays = Arrays.copyOf(intsArrays, intPosition * 2);
      nameIntArrays = Arrays.copyOf(nameIntArrays, intPosition * 2);
    }
  }

  private void checkNeedResizeStrings() {
    if (nameStringArrays.length - 1 == stringPosition) {
      stringsArrays = Arrays.copyOf(stringsArrays, stringPosition * 2);
      nameStringArrays = Arrays.copyOf(nameStringArrays, stringPosition * 2);
    }
  }

  public float[][] getFloatArray(String name) {
    int i = 0;
    for (String nameArr : nameFloatArrays) {
      if (nameArr.equals(name)) {
        return floatsArrays[i];
      }
      i++;
    }
    throw new IllegalArgumentException(NAME_NOT_FOUND_DESCRIPTION);
  }

  public int[][] getIntArray(String name) {
    int i = 0;
    for (String nameArr : nameIntArrays) {
      if (nameArr.equals(name)) {
        return intsArrays[i];
      }
      i++;
    }
    throw new IllegalArgumentException(NAME_NOT_FOUND_DESCRIPTION);
  }

  public String[][] getStringArray(String name) {
    int i = 0;
    for (String nameArr : nameStringArrays) {
      if (nameArr.equals(name)) {
        return stringsArrays[i];
      }
      i++;
    }
    throw new IllegalArgumentException(NAME_NOT_FOUND_DESCRIPTION);
  }

}
