from struct import pack
from typing import Any

import numpy as np

from nparray import (BYTE_ORDER_SELECT_VERSION, BIG_ENDIAN, LITTLE_ENDIAN, Metadata, TypeDescriptor,
                     STRING_TYPE, NUMBER_SIZE, SHORT_SIZE)

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
        rows = arr.shape[0]
        columns = arr.shape[1]

        if rows > MAX_ARRAY_LEN or columns > MAX_ARRAY_LEN:
            raise ValueError('Dimension exceeds acceptable value for: ' + name)

        type_descriptor = TypeDescriptor.from_dtype(arr.dtype)
        metadata = Metadata(type_descriptor=type_descriptor,
                            array_name=name,
                            rows=rows,
                            columns=columns,
                            data_size=Serializer._calc_data_size(arr))
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
        else:
            raise ValueError('invalid type for array: ' + name)
        self.last_used_name = name
