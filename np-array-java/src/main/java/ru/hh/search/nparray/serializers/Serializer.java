package ru.hh.search.nparray.serializers;

import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.util.ByteArrayViews;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;

public abstract class Serializer<T extends AbstractArray> {

  protected final byte[] bytes2 = new byte[2];
  protected final byte[] bytes4 = new byte[4];
  protected final byte[] bytes8 = new byte[8];

  protected final OutputStream out;
  protected final VarHandle view;

  public Serializer(OutputStream out, VarHandle view) {
    this.out = out;
    this.view = view;
  }

  protected abstract void writeData(T array) throws IOException;

  public void serialize(T array) throws IOException {
    writeMetadata(array);
    writeData(array);
  }

  private void writeMetadata(T array) throws IOException {
    writeIntBE(array.getTypeDescriptor().getValue());
    writeString(array.getName());
    writeIntBE(array.getRowCount());
    writeIntBE(array.getColumnCount());
    writeLongBE(array.getDataSize());
  }

  protected void writeIntBE(int v) throws IOException {
    ByteArrayViews.INT_BE.getView().set(bytes4, 0, v);
    out.write(bytes4);
  }

  protected void writeLongBE(long v) throws IOException {
    ByteArrayViews.LONG_BE.getView().set(bytes8, 0, v);
    out.write(bytes8);
  }

  protected void writeString(String v) throws IOException {
    var bytes = v.getBytes();
    int length = bytes.length;
    writeIntBE(length);
    out.write(bytes, 0, length);
  }
}
