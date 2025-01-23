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

Мы используем свой форк библиотеки pyfastpfor https://forgejo.pyn.ru/hhru/PyFastPFor, чтобы ограничить CPU инструкции, которые 
с++ компилятор должен использовать при компиляции кода библиотеки.
Сборка с инструкцией -march=native использует все CPU инструкции доступные на машинке, где происходит сборка. Мы обычно собираем образ 
базовый кардинала на стенде, а на стендах отличаются CPU инструкции от прода, посмотреть можно такой командой: grep flags /proc/cpuinfo -m1
Поэтому мы ограничили их такими флагами: '-mavx2', '-mavx', '-msse4.1'
Тут больше информации о флагах при сборке c++ кода: https://gcc.gnu.org/onlinedocs/gcc/x86-Options.html

2 способа использования нашего форка:

1. Способ через путь до git проекта (сейчас актуальный)

Указываем путь в setup.py путь до нашего репозитория: 

    install_requires=[
        'pyfastpfor @ git+https://forgejo.pyn.ru/hhru/PyFastPFor.git@master#subdirectory=python_bindings'
    ]

Т.к. инструкции в форке уже ограничены всё по умолчанию подхватится

2. Способ через .whl файл

Чтобы собрать .whl файл, нужно скачать репозиторий pyfastpfor https://forgejo.pyn.ru/hhru/PyFastPFor/

Перейти в папку: cd ./python_bindings

Если надо, поменять версию в setup.py файле

Выполнить команду сборки .whl файла: sudo /usr/local/bin/python3.9 setup.py bdist_wheel

Копировать новый файл (он находится по пути python_bindings/dist) .whl в папку libs и в setup.py использовать в таком виде зависимость:

    install_requires=[
        f"pyfastpfor @ file://localhost/{os.getcwd()}/libs/pyfastpfor-1.4.1-cp39-cp39-linux_x86_64.whl"
    ],

