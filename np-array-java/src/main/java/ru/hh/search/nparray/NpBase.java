package ru.hh.search.nparray;


public class NpBase {

  public static final String ACTUAL_VERSION = "swkFx7VJ";

  private final String version;

  protected int intPosition = 0;
  protected int floatPosition = 0;
  protected int stringPosition = 0;

  protected String[] nameIntArrays;
  protected String[] nameFloatArrays;
  protected String[] nameStringArrays;

  public NpBase(String version, int initSizeInt, int initSizeFloat, int initSizeString) {
    this.version = version;
    nameFloatArrays = new String[initSizeFloat];
    nameIntArrays = new String[initSizeInt];
    nameStringArrays = new String[initSizeString];
  }

  public String getVersion() {
    return version;
  }
}
