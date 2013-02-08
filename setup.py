#!/usr/bin/python2
# -*- coding: utf-8 -*-

from setuptools import setup, find_packages
import gotcha

setup(name='gotcha',
      version=gotcha.__version__,
      packages=find_packages(),
      author="Yann 'Meow' Bordenave",
      author_email='meow@meowstars.org',
      description='A gotcha keywords parser',
      long_description=open('README.md').read(),
      install_requires=[
          'clint==0.3.1',
          'clize==2.0',
          'termcolor==1.1.0'
      ],
      include_package_data=True,
      url='http://github.com/meowstars/gotcha',
      classifiers=[
          'Programming Language :: Python',
          'License :: OSI Approved :: GNU Lesser General Public License v3 (LGPLv3)',
          'Natural Language :: English',
          'Topic :: Software Development',
      ],
      entry_points = {
          'console_scripts': [
              'gotcha = gotcha.core:gotcha',
          ],
      },
      license='LGPLv3',
)
