package ru.hh.search.nparray.serializers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import ru.hh.search.nparray.TypeDescriptor;
import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.arrays.CompressedIntArray;
import ru.hh.search.nparray.util.ByteArrayViews;

public abstract class Serializer<T extends AbstractArray> {

  protected final byte[] bytes2 = new byte[2];
  protected final byte[] bytes4 = new byte[4];
  protected final byte[] bytes8 = new byte[8];

  protected OutputStream out;
  protected final VarHandle view;

  public Serializer(OutputStream out, VarHandle view) {
    this.out = out;
    this.view = view;
  }

  protected abstract long writeData(T array) throws IOException;

  public void serialize(T array) throws IOException {
    if (array.getTypeDescriptor() == TypeDescriptor.COMPRESSED_INTEGER) {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      var compressedIntArraySerializer = new CompressedIntArraySerializer(byteArrayOutputStream, view);
      compressedIntArraySerializer.writeData((CompressedIntArray) array);
      var byteArray = byteArrayOutputStream.toByteArray();
      byteArrayOutputStream.close();
      array.setDataSize(byteArray.length);
      writeMetadata(array);
      writeBytes(byteArray);
    } else {
      writeMetadata(array);
      writeData(array);
    }
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

  protected void writeBytes(byte[] bytes) throws IOException {
    out.write(bytes);
  }

  protected long writeString(String v) throws IOException {
    var bytes = v.getBytes();
    int length = bytes.length;
    writeIntBE(length);
    out.write(bytes, 0, length);
    return Integer.BYTES + length;
  }
}
