# np-array

File format:

```
VERSION               8byte
ARRAY_METADATA_1      variable
ARRAY_DATA_1          variable
.
.
.
ARRAY_METADATA_N
ARRAY_DATA_N
```

Array metadata format:

```
ARRAY_METADATA           variable
  
  type descriptor        int32    4byte
  array name length      int32    4byte
  array name             string   array_name_length bytes
  rows                   int32    4byte
  columns                int32    4byte
  data size              int64    8byte
```

Type descriptor
- 1 - array of int32 elements
- 2 - array of float32 elements
- 3 - array of string elements
- 4 - array of int16 elements
- 5 - array of float16 elements
- 6 - array of compressed integers elements encoded with delta encoding and fastpfor128 compression


Byte order: big endian


# building pyfastpfor lib

Мы используем свой форк библиотеки pyfastpfor https://github.com/hhru/PyFastPFor, чтобы ограничить CPU инструкции, которые 
с++ компилятор должен использовать при компиляции кода библиотеки. (можно посмотреть коммиты)

Чтобы собрать .whl файл, нужно скачать репозиторий pyfastpfor

Перейти в папку: cd ./python_bindings

Если надо, поменять версию в setup.py файле

Выполнить команду сборки .whl файла: sudo /usr/local/bin/python3.9 setup.py bdist_wheel

Копировать новый файл (он находится по пути python_bindings/dist) .whl в папку libs и в setup.py изменить имя файла в install_requires секции
