#!/usr/bin/env python
import sys
import xml.etree.ElementTree as ET
import lxml.etree
import re
import os.path




def convert_source(filename):
    #read input file
    fin = open(filename, "rt")
    #read file contents to string
    data = fin.read()
    # xmlstr is your xml in a string
    root = lxml.etree.fromstring(data)
    sources = root.find('sources')
    packages = root.find('packages')
    for package in packages:
        classes = package.find('classes')
        for clazz in classes:
            file_not_found = True
            for source in sources:
                full_filename = source.text + '/' + clazz.attrib['filename']
                if os.path.isfile(full_filename):
                    clazz.attrib['filename'] = full_filename
                    file_not_found = False
            if file_not_found:
                print("Warning: File {} not found in all sources; removing from sources.".format(clazz.attrib['filename']))
                clazz.getparent().remove(clazz)

    data = lxml.etree.tostring(root, pretty_print=True)
    #close the input file
    fin.close()
    #open the input file in write mode
    fin = open(filename, "wb")
    #overrite the input file with the resulting data
    fin.write(data)
    #close the file
    fin.close()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: source2filename.py FILENAME")
        sys.exit(1)

    filename    = sys.argv[1]

    convert_source(filename)
