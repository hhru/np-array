import tempfile

import unittest
import numpy as np

from nparray import STRING_TYPE, CompressedIntArray
from nparray.deserializer import Deserializer
from nparray.serializer import Serializer
from pyfastpfor import getCodec, prefixSum1


class TestNPArrays(unittest.TestCase):
    def test_serialization(self):

        floats = np.zeros((1, 2), dtype='float32')
        floats[0][0] = 10.344
        floats[0][1] = -10.344

        ints = np.zeros((2, 1), dtype='int32')
        ints[0][0] = 45435444
        ints[1][0] = 1111

        ints2 = np.zeros((2, 3), dtype='int32')
        ints2[0][0] = 45435444
        ints2[0][1] = 1111
        ints2[0][2] = 44
        ints2[1][0] = 4
        ints2[1][1] = 5
        ints2[1][2] = 6

        shorts = np.zeros((2, 1), dtype='int16')
        shorts[0][0] = 4543
        shorts[1][0] = 111

        halfs = np.zeros((2, 1), dtype='float16')
        halfs[0][0] = 1.0
        halfs[1][0] = 32.2323

        strings1 = np.array([
            ['as\ndcs', '!!!U3gARt7BC9VwlAnxFHQ--'],
            ['апапа', '!-0123456789=ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz'],
            ['整数', '&&()']
        ], dtype=STRING_TYPE)

        strings2 = np.array([
            ['asdcs'],
            ['апапа'],
        ], dtype=STRING_TYPE)

        strings3 = np.array([
            ['asdcs', '!!!U3gARt7BC9VwlAnxFHQ--']
        ], dtype=STRING_TYPE)

        data = {
            'super_matrix': ints,
            'data': ints2,
            'апапа': floats,
            'byte_string1': strings1,
            'byte_string2': strings2,
            'byte_string3': strings3,
            'short': shorts,
            'half': halfs,
        }

        fp, filename = tempfile.mkstemp()

        with Serializer(filename) as serializer:
            serializer.serialize(**data)

        with Deserializer(filename) as deserializer:
            result = deserializer.deserialize()

        with Serializer(filename) as serializer:
            serializer.serialize(**result)

        with Deserializer(filename) as deserializer:
            result = deserializer.deserialize()

        self.assertTrue(np.array_equal(ints, result['super_matrix']))
        self.assertTrue(np.array_equal(ints2, result['data']))
        self.assertTrue(np.array_equal(floats, result['апапа']))
        self.assertTrue(np.array_equal(strings1, result['byte_string1']))
        self.assertTrue(np.array_equal(strings2, result['byte_string2']))
        self.assertTrue(np.array_equal(strings3, result['byte_string3']))
        self.assertTrue(np.array_equal(shorts, result['short']))
        self.assertTrue(np.array_equal(halfs, result['half']))

    def test_ordered_read(self):
        floats = np.zeros((1, 2), dtype='float32')
        floats[0][0] = 10.344
        floats[0][1] = -10.344

        ints = np.zeros((2, 1), dtype='int32')
        ints[0][0] = 45435444
        ints[1][0] = 1111

        data = {
            '2test': floats,
            '1test': ints
        }
        _, filename = tempfile.mkstemp()
        with Serializer(filename) as serializer:
            serializer.serialize(**data)

        with Deserializer(filename) as deserializer:
            self.assertTrue(np.array_equal(ints, deserializer.get_array('1test')))
            self.assertTrue(np.array_equal(floats, deserializer.get_array('2test')))

    def test_ordered_write(self):
        floats = np.zeros((1, 2), dtype='float32')
        floats[0][0] = 10.344
        floats[0][1] = -10.344

        ints = np.zeros((2, 1), dtype='int32')
        ints[0][0] = 45435444
        ints[1][0] = 1111

        _, filename = tempfile.mkstemp()
        with Serializer(filename) as serializer:
            serializer.write_array('1test', ints)
            serializer.write_array('2test', floats)

        with Deserializer(filename) as deserializer:
            self.assertTrue(np.array_equal(ints, deserializer.get_array('1test')))
            self.assertTrue(np.array_equal(floats, deserializer.get_array('2test')))

    def test_partially_read(self):
        floats = np.zeros((1, 2), dtype='float32')
        floats[0][0] = 10.344
        floats[0][1] = -10.344

        ints = np.zeros((2, 1), dtype='int32')
        ints[0][0] = 45435444
        ints[1][0] = 1111

        data = {
            '2test': floats,
            '1test': ints
        }
        _, filename = tempfile.mkstemp()
        with Serializer(filename) as serializer:
            serializer.serialize(**data)

        with Deserializer(filename) as deserializer:
            self.assertTrue(np.array_equal(floats, deserializer.get_array('2test')))

    def test_incorrect_read_order(self):
        with self.assertRaises(Exception) as raise_context:
            floats = np.zeros((1, 2), dtype='float32')
            floats[0][0] = 10.344
            floats[0][1] = -10.344

            ints = np.zeros((2, 1), dtype='int32')
            ints[0][0] = 45435444
            ints[1][0] = 1111

            data = {
                '2test': floats,
                '1test': ints
            }
            _, filename = tempfile.mkstemp()
            with Serializer(filename) as serializer:
                serializer.serialize(**data)

            with Deserializer(filename) as deserializer:
                self.assertTrue(np.array_equal(floats, deserializer.get_array('2test')))
                self.assertTrue(np.array_equal(floats, deserializer.get_array('1test')))

        self.assertTrue('Incorrect name order' in raise_context.exception.args)

    def test_incorrect_write_order(self):
        with self.assertRaises(Exception) as raise_context:
            floats = np.zeros((1, 2), dtype='float32')
            floats[0][0] = 10.344
            floats[0][1] = -10.344

            ints = np.zeros((2, 1), dtype='int32')
            ints[0][0] = 45435444
            ints[1][0] = 1111

            _, filename = tempfile.mkstemp()
            with Serializer(filename) as serializer:
                serializer.write_array('2test', floats)
                serializer.write_array('1test', ints)

        self.assertTrue('Incorrect name order' in raise_context.exception.args)

    def test_read_nonexistent_array(self):
        with self.assertRaises(Exception) as raise_context:
            floats = np.zeros((1, 2), dtype='float32')
            floats[0][0] = 10.344
            floats[0][1] = -10.344

            ints = np.zeros((2, 1), dtype='int32')
            ints[0][0] = 45435444
            ints[1][0] = 1111

            data = {
                '2test': floats,
                '1test': ints
            }
            _, filename = tempfile.mkstemp()
            with Serializer(filename) as serializer:
                serializer.serialize(**data)

            name = 'abc'
            with Deserializer(filename) as deserializer:
                self.assertTrue(np.array_equal(floats, deserializer.get_array(name)))

        self.assertTrue(('Failed to find array: ' + name) in raise_context.exception.args)

    def test_serialize_too_big_array(self):
        with self.assertRaises(Exception) as raise_context:
            name_big_array = 'too_big'
            name_normal_array = 'normal'
            data = {
                name_big_array: np.zeros((2 ** 31 - 7, 1), dtype='float32'),
                name_normal_array: np.zeros((10, 3), dtype='float32')
            }
            _, filename = tempfile.mkstemp()
            with Serializer(filename) as serializer:
                serializer.serialize(**data)
        self.assertTrue(('Dimension exceeds acceptable value for: ' + name_big_array) in raise_context.exception.args)

    def test_compressed_integer_array(self):
        arr_size = 300
        candidates = np.ones((2, arr_size), dtype=np.int32) * -1
        candidates[0][arr_size - 1] = 100
        candidates[0][arr_size - 1] = 999

        arr = CompressedIntArray(candidates)
        data = {
            'compressed_integer_array': arr
        }
        _, filename = tempfile.mkstemp()
        with Serializer(filename) as serializer:
            serializer.serialize(**data)

        with Deserializer(filename) as deserializer:
            result = deserializer.deserialize()

            self.assertTrue(len(result['compressed_integer_array']) == 2)

            def uncompress_array(index: int, size: int):
                codec = getCodec('fastpfor128')
                arr_uncompressed = np.zeros(size, dtype=np.uint32, order='C')
                arr_compressed = np.array(result['compressed_integer_array'][index],
                                          copy=True, dtype=np.uint32, order='C')
                codec.decodeArray(arr_compressed, len(arr_compressed), arr_uncompressed, len(arr_uncompressed))
                prefixSum1(arr_uncompressed, len(arr_uncompressed))
                arr_uncompressed = arr_uncompressed.astype(np.int32, copy=False)
                return arr_uncompressed

            first = uncompress_array(0, arr_size)
            self.assertTrue(np.array_equal(first, candidates[0]))
            second = uncompress_array(1, arr_size)
            self.assertTrue(np.array_equal(second, candidates[1]))
