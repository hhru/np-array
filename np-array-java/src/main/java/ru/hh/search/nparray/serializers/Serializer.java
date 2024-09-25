package ru.hh.search.nparray.serializers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.VarHandle;
import ru.hh.search.nparray.TypeDescriptor;
import ru.hh.search.nparray.arrays.AbstractArray;
import ru.hh.search.nparray.util.ByteArrayViews;

public abstract class Serializer<T extends AbstractArray> {

  protected final byte[] bytes2 = new byte[2];
  protected final byte[] bytes4 = new byte[4];
  protected final byte[] bytes8 = new byte[8];

  protected final RandomAccessFile out;
  protected final VarHandle view;

  public Serializer(RandomAccessFile out, VarHandle view) {
    this.out = out;
    this.view = view;
  }

  protected abstract long writeData(T array) throws IOException;

  public void serialize(T array) throws IOException {
    if (array.getTypeDescriptor() == TypeDescriptor.COMPRESSED_INTEGER) {
      long metadataPosition = out.getFilePointer();
      writeMetadata(array);
      long dataSize = writeData(array);
      long endOfFilePosition = out.getFilePointer();
      array.setDataSize(dataSize);
      writeMetadata(metadataPosition, array);
      out.seek(endOfFilePosition);
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

  private void writeMetadata(long position, T array) throws IOException {
    out.seek(position);
    writeMetadata(array);
  }

  protected void writeIntBE(int v) throws IOException {
    ByteArrayViews.INT_BE.getView().set(bytes4, 0, v);
    out.write(bytes4);
  }

  protected void writeLongBE(long v) throws IOException {
    ByteArrayViews.LONG_BE.getView().set(bytes8, 0, v);
    out.write(bytes8);
  }

  protected long writeString(String v) throws IOException {
    var bytes = v.getBytes();
    int length = bytes.length;
    writeIntBE(length);
    out.write(bytes, 0, length);
    return Integer.BYTES + length;
  }
}
