package ru.hh.search.nparray.deserializers;

import me.lemire.integercompression.Composition;
import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.IntegerCODEC;
import me.lemire.integercompression.VariableByte;

public class CompressedIntArrayDeserializer {
  public static int[] deserialize(int sizeUncompressed, int[] compressedArray) {
    IntegerCODEC ic = new Composition(new FastPFOR128(), new VariableByte());
    int[] recovered = new int[sizeUncompressed];
    IntWrapper recoffset = new IntWrapper(0);
    ic.uncompress(compressedArray, new IntWrapper(0), compressedArray.length, recovered, recoffset);
    prefixSum(recovered);
    return recovered;
  }

  private static void prefixSum(int[] arr) {
    for (int u = 1; u < arr.length; u++) {
      arr[u] += arr[u - 1];
    }
  }
}
