package ru.hh.search.nparray.serializers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import me.lemire.integercompression.Composition;
import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.IntegerCODEC;
import me.lemire.integercompression.VariableByte;
import ru.hh.search.nparray.arrays.CompressedIntArray;

public class CompressedIntArraySerializer extends Serializer<CompressedIntArray> {

  public CompressedIntArraySerializer(OutputStream out, VarHandle view) {
    super(out, view);
  }

  @Override
  protected long writeData(CompressedIntArray array) throws IOException {
    var data = array.getData();
    IntegerCODEC ic = new Composition(new FastPFOR128(), new VariableByte());
    long dataSize = 0;
    for (int i = 0; i < array.getRowCount(); i++) {
      var uncompressedArray = data.get(i);
      int compressedArraySize = estimateCompressedArraySize(uncompressedArray.length);
      int[] compressedArray = new int[compressedArraySize];
      IntWrapper uncompressedOffset = new IntWrapper(0);
      IntWrapper compressedOffset = new IntWrapper(0);
      applyDeltaEncoding(uncompressedArray);
      ic.compress(uncompressedArray, uncompressedOffset, uncompressedArray.length, compressedArray, compressedOffset);
      writeIntBE(uncompressedArray.length);
      writeIntBE(compressedOffset.intValue());
      dataSize += Integer.BYTES + Integer.BYTES;
      for (int j = 0; j < compressedOffset.intValue(); j++) {
        view.set(bytes4, 0, compressedArray[j]);
        out.write(bytes4);
      }
      dataSize += (long) Integer.BYTES * compressedOffset.intValue();
    }
    return dataSize;
  }

  private static int estimateCompressedArraySize(int sizeUncompressed) {
    if (sizeUncompressed == 1) {
      return 3;
    } else if (sizeUncompressed < 10) {
      return sizeUncompressed * 2;
    } else if (sizeUncompressed < 150) {
      return (int) (sizeUncompressed * 1.5);
    } else {
      return sizeUncompressed;
    }
  }

  private static void applyDeltaEncoding(int[] arr) {
    for (int i = arr.length - 1; i > 0; i--) {
      arr[i] = arr[i] - arr[i - 1];
    }
  }
}
