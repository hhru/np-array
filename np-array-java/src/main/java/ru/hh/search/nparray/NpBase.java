package ru.hh.search.nparray;


public class NpBase {

  protected int intPosition = 0;
  protected int floatPosition = 0;

  protected String[] nameIntArrays;
  protected String[] nameFloatArrays;

  public NpBase(int initSizeInt, int initSizeFloat) {
    nameFloatArrays = new String[initSizeFloat];
    nameIntArrays = new String[initSizeInt];
  }
}
