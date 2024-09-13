from pyfastpfor import *
from struct import pack
from typing import Any

import numpy as np

from nparray import (BYTE_ORDER_SELECT_VERSION, BIG_ENDIAN, LITTLE_ENDIAN, Metadata, TypeDescriptor,
                     STRING_TYPE, NUMBER_SIZE, SHORT_SIZE, CompressedIntArray)

MAX_ARRAY_LEN = 2 ** 31 - 9


def to_bytes(data: str):
    return bytes(data, 'utf-8')


class Serializer:
    def __init__(self, filename: str, byte_order=BIG_ENDIAN):
        self.filename = filename
        self.version = None
        self.last_used_name = None

        if byte_order not in {BIG_ENDIAN, LITTLE_ENDIAN}:
            raise ValueError('Invalid byte order')

        self.byte_order = byte_order

    def __enter__(self):
        self.fp = open(self.filename, 'wb')
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.fp.close()

    @staticmethod
    def _calc_data_size(arr):
        if arr.dtype.type == STRING_TYPE:
            return sum(map(lambda h: len(to_bytes(h)), arr.ravel())) + arr.size * NUMBER_SIZE
        elif arr.dtype.type == np.int16 or arr.dtype.type == np.float16:
            return arr.size * SHORT_SIZE
        elif isinstance(arr, CompressedIntArray):
            raise ValueError('Size for compressed int array can not be precalculated')
        else:
            return arr.size * NUMBER_SIZE

    def _write_metadata(self, metadata: Metadata) -> None:
        self.fp.write(pack('>ii', metadata.type_descriptor, len(to_bytes(metadata.array_name))))
        self.fp.write(to_bytes(metadata.array_name))
        self.fp.write(pack('>iiq', metadata.rows, metadata.columns, metadata.data_size))

    def _write_string_array(self, arr):
        for string in arr.ravel():
            string_bytes = to_bytes(string)
            self.fp.write((len(string_bytes)).to_bytes(NUMBER_SIZE, byteorder='big'))
            self.fp.write(string_bytes)

    def _write_compressed_int_array(self, array: CompressedIntArray) -> int:
        codec = getCodec('fastpfor128')
        data_size = 0
        for e in array.data:
            original_array = e.copy()
            array_len = len(original_array)
            compressed_array = np.zeros(array_len, dtype=np.uint32, order='C').ravel()
            delta1(original_array, array_len)
            compressed_array_len = codec.encodeArray(original_array, array_len, compressed_array, len(compressed_array))
            compressed_array = compressed_array[0:compressed_array_len]
            self.fp.write(array_len.to_bytes(NUMBER_SIZE, byteorder='big'))
            self.fp.write(compressed_array_len.to_bytes(NUMBER_SIZE, byteorder='big'))
            compressed_array.astype('{}i4'.format(self.byte_order), copy=False).tofile(self.fp)
            data_size += NUMBER_SIZE + NUMBER_SIZE + NUMBER_SIZE * compressed_array_len

        return data_size

    def _write_version_if_necessary(self) -> None:
        if self.version is not None:
            return
        self.version = BYTE_ORDER_SELECT_VERSION
        self.fp.write(to_bytes(self.version))
        self.fp.write(to_bytes(self.byte_order))

    def _check_name(self, name: str) -> None:
        if self.last_used_name is not None and name < self.last_used_name:
            raise ValueError('Incorrect name order')

    def serialize(self, **kwargs) -> None:
        for name, arr in sorted(kwargs.items()):
            self.write_array(name, arr)

    def write_array(self, name: str, arr: Any) -> None:
        self._check_name(name)
        self._write_version_if_necessary()

        if isinstance(arr, CompressedIntArray):
            rows = len(arr.data)
            columns = 0
            if rows > MAX_ARRAY_LEN:
                raise ValueError('Dimension exceeds acceptable value for: ' + name)
            type_descriptor = TypeDescriptor.COMPRESSED_INTEGER
            data_size = 0
        else:
            rows = arr.shape[0]
            columns = arr.shape[1]
            if rows > MAX_ARRAY_LEN or columns > MAX_ARRAY_LEN:
                raise ValueError('Dimension exceeds acceptable value for: ' + name)
            type_descriptor = TypeDescriptor.from_dtype(arr.dtype)
            data_size = Serializer._calc_data_size(arr)

        metadata = Metadata(type_descriptor=type_descriptor,
                            array_name=name,
                            rows=rows,
                            columns=columns,
                            data_size=data_size)
        metadata_position = self.fp.tell()
        self._write_metadata(metadata)

        if type_descriptor == TypeDescriptor.INTEGER:
            arr.astype('{}i4'.format(self.byte_order), copy=False).tofile(self.fp)
        elif type_descriptor == TypeDescriptor.INTEGER16:
            arr.astype('{}i2'.format(self.byte_order), copy=False).tofile(self.fp)
        elif type_descriptor == TypeDescriptor.FLOAT:
            arr.astype('{}f4'.format(self.byte_order), copy=False).tofile(self.fp)
        elif type_descriptor == TypeDescriptor.FLOAT16:
            arr.astype('{}f2'.format(self.byte_order), copy=False).tofile(self.fp)
        elif type_descriptor == TypeDescriptor.STRING:
            self._write_string_array(arr.astype(STRING_TYPE))
        elif type_descriptor == TypeDescriptor.COMPRESSED_INTEGER:
            total_data_size = self._write_compressed_int_array(arr)
            end_position = self.fp.tell()
            metadata = Metadata(type_descriptor=type_descriptor,
                                array_name=name,
                                rows=rows,
                                columns=columns,
                                data_size=total_data_size)
            self.fp.seek(metadata_position)
            self._write_metadata(metadata)
            self.fp.seek(end_position)
        else:
            raise ValueError('invalid type for array: ' + name)
        self.last_used_name = name
