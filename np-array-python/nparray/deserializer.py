import numpy as np

from typing import Union, Any

from nparray import (SUPPORTED_VERSIONS, VERSION_SIZE, NUMBER_SIZE, STRING_TYPE, Metadata, TypeDescriptor,
                     BYTE_ORDER_SELECT_VERSION, BIG_ENDIAN, LITTLE_ENDIAN, BYTE_ORDER_SIZE)


def from_bytes(data):
    return data.decode('utf-8')


class Deserializer:
    def __init__(self, filename: str):
        self.filename = filename
        self.version = None
        self.last_used_name = None
        self.byte_order = BIG_ENDIAN

    def __enter__(self):
        self.fp = open(self.filename, 'rb')
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.fp.close()

    def _read_int(self, size: int = NUMBER_SIZE) -> int:
        read_bytes = self.fp.read(size)
        if len(read_bytes) != size:
            raise IOError('Read only {} bytes, expected {}'.format(len(read_bytes), size))
        return int.from_bytes(read_bytes, byteorder='big')

    def _read_metadata(self) -> Union[Metadata, None]:
        type_descriptor_bytes = self.fp.read(NUMBER_SIZE)
        if not type_descriptor_bytes:
            return None
        type_descriptor = TypeDescriptor(int.from_bytes(type_descriptor_bytes, byteorder='big'))
        array_name_len = self._read_int()
        array_name = from_bytes(self.fp.read(array_name_len))
        rows = self._read_int()
        columns = self._read_int()
        data_size = self._read_int(8)
        return Metadata(type_descriptor, array_name, rows, columns, data_size)

    def _read_array(self, rows: int, columns: int, type_str: str) -> Any:
        return (np.fromfile(self.fp, np.dtype('{}{}'.format(self.byte_order, type_str)), count=rows * columns)
                .reshape((rows, columns))
                .astype(type_str))

    def _read_string_array(self, rows: int, columns: int, bytes_to_read: int) -> Any:
        buffer = self.fp.read(bytes_to_read)
        offset = 0
        arr = []
        for _ in range(rows):
            row = []
            for j in range(columns):
                string_length = int.from_bytes(buffer[offset: offset + NUMBER_SIZE], byteorder='big')
                offset += NUMBER_SIZE
                row.append(from_bytes(buffer[offset: offset + string_length]))
                offset += string_length
            arr.append(row)
        return np.asarray(arr, dtype=STRING_TYPE).reshape((rows, columns))

    def _read_compressed_int_array(self, rows: int) -> list[np.ndarray]:
        arr = []
        for i in range(rows):
            array_len = self._read_int()
            compressed_array_len = self._read_int()
            row_arr = np.fromfile(self.fp, np.dtype('{}{}'.format(self.byte_order, 'i4')),
                                  count=compressed_array_len).astype('i4')
            arr.append(row_arr)
        return arr

    def _read_current_array(self, metadata: Metadata) -> Any:
        if metadata.type_descriptor == TypeDescriptor.INTEGER:
            arr = self._read_array(metadata.rows, metadata.columns, 'i4')
        elif metadata.type_descriptor == TypeDescriptor.INTEGER16:
            arr = self._read_array(metadata.rows, metadata.columns, 'i2')
        elif metadata.type_descriptor == TypeDescriptor.FLOAT:
            arr = self._read_array(metadata.rows, metadata.columns, 'f4')
        elif metadata.type_descriptor == TypeDescriptor.FLOAT16:
            arr = self._read_array(metadata.rows, metadata.columns, 'f2')
        elif metadata.type_descriptor == TypeDescriptor.STRING:
            arr = self._read_string_array(metadata.rows, metadata.columns, metadata.data_size)
        elif metadata.type_descriptor == TypeDescriptor.COMPRESSED_INTEGER:
            arr = self._read_compressed_int_array(metadata.rows)
        else:
            raise ValueError('invalid type: ' + metadata.type_descriptor.value)
        return arr

    def deserialize(self) -> dict:
        self._read_header_if_necessary()
        result = {}
        metadata = self._read_metadata()
        while metadata is not None:
            result[metadata.array_name] = self._read_current_array(metadata)
            metadata = self._read_metadata()
        return result

    def _read_header_if_necessary(self) -> None:
        if self.version is not None:
            return
        self.version = from_bytes(self.fp.read(VERSION_SIZE))
        if self.version not in SUPPORTED_VERSIONS:
            raise ValueError('Version {} is not supported', self.version)
        if self.version == BYTE_ORDER_SELECT_VERSION:
            self.byte_order = from_bytes(self.fp.read(BYTE_ORDER_SIZE))
            if self.byte_order != BIG_ENDIAN and self.byte_order != LITTLE_ENDIAN:
                raise ValueError('Invalid byte order {}', self.byte_order)

    def _check_name(self, name: str) -> None:
        if self.last_used_name is not None and name < self.last_used_name:
            raise ValueError('Incorrect name order')

    def _find_array(self, name: str) -> Union[Metadata, None]:
        metadata = self._read_metadata()
        while metadata is not None and metadata.array_name != name:
            readed_bytes = self.fp.read(metadata.data_size)
            if len(readed_bytes) != metadata.data_size:
                return None
            metadata = self._read_metadata()
        return metadata

    def get_array(self, name: str) -> Any:
        self._check_name(name)
        self._read_header_if_necessary()
        metadata = self._find_array(name)
        if metadata is None:
            raise RuntimeError('Failed to find array: ' + name)
        arr = self._read_current_array(metadata)
        self.last_used_name = name
        return arr
