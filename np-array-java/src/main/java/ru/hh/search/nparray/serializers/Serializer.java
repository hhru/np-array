package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.util.ByteArrayViews;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;

public abstract class Serializer<T extends AbstractArray> {

  private static final VarHandle intView = ByteArrayViews.INT.getView();
  private static final VarHandle floatView = ByteArrayViews.FLOAT.getView();
  private static final VarHandle longView = ByteArrayViews.LONG.getView();
  private static final VarHandle shortView = ByteArrayViews.SHORT.getView();

  private final byte[] bytes2 = new byte[2];
  private final byte[] bytes4 = new byte[4];
  private final byte[] bytes8 = new byte[8];

  protected OutputStream out;

  public Serializer(OutputStream out) {
    this.out = out;
  }

  protected abstract void writeData(T array) throws IOException;

  public void serialize(T array) throws IOException {
    writeMetadata(array);
    writeData(array);
  }

  private void writeMetadata(T array) throws IOException {
    writeInt(array.getTypeDescriptor().getValue());
    writeString(array.getName());
    writeInt(array.getRowCount());
    writeInt(array.getColumnCount());
    writeLong(array.getDataSize());
  }

  protected void writeInt(int v) throws IOException {
    intView.set(bytes4, 0, v);
    out.write(bytes4);
  }

  protected void writeFloat(float v) throws IOException {
    floatView.set(bytes4, 0, v);
    out.write(bytes4);
  }

  protected void writeLong(long v) throws IOException {
    longView.set(bytes8, 0, v);
    out.write(bytes8);
  }

  protected void writeString(String v) throws IOException {
    var bytes = v.getBytes();
    int length = bytes.length;
    writeInt(length);
    out.write(bytes, 0, length);
  }

  protected void writeShort(short v) throws IOException {
    shortView.set(bytes2, 0, v);
    out.write(bytes2);
  }
}
