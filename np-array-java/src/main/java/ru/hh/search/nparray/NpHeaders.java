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


  public NpHeaders(int initSizeInt, int initSizeFloat) {
    super(initSizeInt, initSizeFloat);
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
}
