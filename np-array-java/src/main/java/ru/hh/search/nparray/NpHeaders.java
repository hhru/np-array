package ru.hh.search.nparray;


public class NpHeaders extends NpBase{

  protected int[] rowsInt;
  protected int[] columnInt;
  protected long[] offsetNameInt;
  protected long[] offsetArrayInt;

  protected int[] rowsFloat;
  protected int[] columnFloat;
  protected long[] offsetNameFloat;
  protected long[] offsetArrayFloat;

  protected int[] rowsString;
  protected int[] columnString;
  protected long[] offsetNameString;
  protected long[] offsetArrayString;

  public NpHeaders(String version, int initSizeInt, int initSizeFloat, int initSizeString) {
    super(version, initSizeInt, initSizeFloat, initSizeString);
  }

  public void setRowsInt(int[] rowsInt) {
    this.rowsInt = rowsInt;
  }

  public void setColumnInt(int[] columnInt) {
    this.columnInt = columnInt;
  }

  public void setOffsetNameInt(long[] offsetNameInt) {
    this.offsetNameInt = offsetNameInt;
  }

  public void setOffsetArrayInt(long[] offsetArrayInt) {
    this.offsetArrayInt = offsetArrayInt;
  }

  public void setRowsFloat(int[] rowsFloat) {
    this.rowsFloat = rowsFloat;
  }

  public void setColumnFloat(int[] columnFloat) {
    this.columnFloat = columnFloat;
  }

  public void setOffsetNameFloat(long[] offsetNameFloat) {
    this.offsetNameFloat = offsetNameFloat;
  }

  public void setOffsetArrayFloat(long[] offsetArrayFloat) {
    this.offsetArrayFloat = offsetArrayFloat;
  }

  public void setRowsString(int[] rowsString) {
    this.rowsString = rowsString;
  }

  public void setColumnString(int[] columnString) {
    this.columnString = columnString;
  }

  public void setOffsetNameString(long[] offsetNameString) {
    this.offsetNameString = offsetNameString;
  }

  public void setOffsetArrayString(long[] offsetArrayString) {
    this.offsetArrayString = offsetArrayString;
  }
}
