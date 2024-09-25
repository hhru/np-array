# coding=utf-8

from setuptools import setup
from version import version


setup(
    name='nparray',
    version=version,
    description='NumPy arrays serializer/deserializer',
    url='https://github.com/hhru/np-array/',
    packages=[
        'nparray'
    ],
    install_requires=[
        'pyfastpfor == 1.4.0'
    ],
    test_suite='tests',
    tests_require=[
        'numpy == 1.26.4',
        'pycodestyle == 2.5.0'
    ],
    zip_safe=False
)
