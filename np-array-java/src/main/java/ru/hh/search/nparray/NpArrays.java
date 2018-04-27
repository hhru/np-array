package ru.hh.search.nparray;

import java.util.Arrays;

public class NpArrays extends NpBase {

  protected float[][][] floatsArrays;
  protected int[][][] intsArrays;

  public NpArrays(int initSizeInt, int initSizeFloat) {
    super(initSizeInt, initSizeFloat);
    floatsArrays = new float[initSizeFloat][][];
    intsArrays = new int[initSizeInt][][];
  }

  public NpArrays() {
    this(10, 10);
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

  public float[][] getFloatArray(String name) {
    int i = 0;
    for (String nameArr : nameFloatArrays) {
      if (nameArr.equals(name)) {
        return floatsArrays[i];
      }
      i++;
    }
    throw new IllegalArgumentException("Name not found");
  }

  public int[][] getIntArray(String name) {
    int i = 0;
    for (String nameArr : nameIntArrays) {
      if (nameArr.equals(name)) {
        return intsArrays[i];
      }
      i++;
    }
    throw new IllegalArgumentException("Name not found");
  }


}
