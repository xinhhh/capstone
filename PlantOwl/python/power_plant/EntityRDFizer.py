##########################################
# Author: Feroz Farazi (msff2@cam.ac.uk) #
# Date: 04 Dec 2020                      #
##########################################

"""This module is designed to convert entities of any domain and their data and metadata into RDF.
It requires the entities and their data to be provided as inputs in an ABox excel template, that is
filled in with example data and that is provided in the following path:
python/power_plnat/test/resources/ABoxOntoLandUse.csv."""

from rdflib import Graph, FOAF, URIRef, BNode, Literal
from rdflib.extras.infixowl import OWL_NS
from rdflib.namespace import RDF, RDFS, Namespace, XSD
from tkinter import Tk  # from tkinter import Tk for Python 3.x
from tkinter.filedialog import askdirectory
import csv
import PropertyReader as propread
import ABoxGeneration as aboxgen
import os.path as path
import glob,os

"""Declared column headers as constants"""
COLUMN_1 = 'Source'
COLUMN_2 = 'Type'
COLUMN_3 = 'Target'
COLUMN_4 = 'Relation'
COLUMN_5 = 'Value'
TOTAL_NO_OF_COLUMNS = 5

"""Predefined types source entries"""
TYPE_INSTANCE = 'Instance'
TYPE_DATA     = 'Data Property'

"""Utility constants"""
HASH = '#'
SLASH = '/'
UNDERSCORE = '_'
HTTP='http://'
HTTPS='https://'
DATA_TYPE_STRING = 'string'
DATA_TYPE_INTEGER = 'integer'
DATA_TYPE_FLOAT = 'float'
DATA_TYPE_DOUBLE = 'double'
DATA_TYPE_DATE_TIME = 'datetime'

"""Declared an array to maintain the list of already created instances"""
instances = dict()
g = Graph()

"""This shows a file dialog box that enables the user to select a file to convert into RDF"""
def select_folder():
    """Suppresses the root window of GUI"""
    Tk().withdraw()
    """Opens a file dialog box to select a file"""
    return askdirectory()

"""This function goes through the folder and writes the files path of all the .csv files into an array """
def select_files():
    folder_path=select_folder()
    os.chdir(folder_path)
    filepaths=[]
    abox_name=[]
    for file in glob.glob("*.csv"):
        csv_filepath=folder_path+'/'+file
        abox_name.append(file.strip('.csv'))
        filepaths.append(csv_filepath)
    return(filepaths,abox_name)

"""This function checks the validity of header in the ABox excel template"""
def is_header_valid(row):
    if len(row) >= TOTAL_NO_OF_COLUMNS:
        if row[0].strip().lower()==COLUMN_1.lower() \
                and row[1].strip().lower()==COLUMN_2.lower() \
                and row[2].strip().lower()==COLUMN_3.lower() \
                and row[3].strip().lower()==COLUMN_4.lower() \
                and row[4].strip().lower()==COLUMN_5.lower():
            return True
        else:
            return False

"""This function converts a row into an entity or a link between two entities or a data or annotation property value"""
def process_data(row):
    if len(row) >= TOTAL_NO_OF_COLUMNS:
        if row[0].strip() is None or row[0].strip()  == '' \
                or row[1].strip() is None or row[1].strip()  == '' \
                or row[2].strip() is None or row[2].strip()  == '':
           return

        if row[1].strip().lower() == TYPE_INSTANCE.lower():
            if (row[3].strip() is None or row[3].strip() == '') \
                    and (row[4].strip() is None or row[4].strip() == ''):
                print('Creating an instance:')
                instance = propread.getABoxIRI()+SLASH+format_iri(row[0])
                type = propread.getTBoxIRI()+HASH+format_iri(row[2])
                if row[0].strip().startswith(HTTP) or row[0].strip().startswith(HTTPS):
                    instance = row[0]
                if row[2].strip().startswith(HTTP) or row[2].strip().startswith(HTTPS):
                    type = row[2]
                aboxgen.create_instance_without_name(g, URIRef(type), URIRef(instance))
                instances[row[0].strip()] = row[2].strip()

            elif row[2].strip() in instances or row[2].strip().startswith(HTTP) or row[2].startswith(HTTPS):
                if not row[0].strip() in instances or row[3].strip()  == '':
                    return
                else:
                    print('link instance 1', instances.get(row[0]))
                    print('link instance 2', instances.get(row[2]))
                    if row[2].strip().startswith(HTTP) or row[2].startswith(HTTPS):
                        aboxgen.link_instance(g, URIRef(row[3]),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[0].strip())),
                                              URIRef(row[2].strip()))
                    else:
                        aboxgen.link_instance(g, URIRef(row[3]),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[0].strip())),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[2].strip())))

        elif row[1].strip().lower() == TYPE_DATA.lower():
            if (row[2].startswith(HTTP) or row[2].startswith(HTTPS)) and not row[4].strip() == '':
                if not row[5].strip() == '':
                    aboxgen.link_data_with_type(g, URIRef(row[0].strip()),
                                      URIRef(format_iri(row[2].strip())),
                                      row[4].strip(), get_data_type(row[5].strip()))
                else:
                    aboxgen.link_data(g, URIRef(row[0].strip()),
                                  URIRef(format_iri(row[2].strip())),
                                  row[4].strip())
            elif row[2].strip() in instances and not row[4].strip() == '':
                if not row[5].strip() == '':
                    aboxgen.link_data_with_type(g, URIRef(row[0].strip()),
                                      URIRef(propread.getABoxIRI() + SLASH + format_iri(row[2].strip())),
                                      row[4].strip(), get_data_type(row[5].strip()))
                else:
                    instance = propread.getABoxIRI() + SLASH + format_iri(row[2].strip())
                    if row[2].strip().startswith(HTTP) or row[2].strip().startswith(HTTPS):
                        instance = row[2].strip()
                    if not row[5].strip() == '':
                        aboxgen.link_data_with_type(g, URIRef(row[0].strip()),
                                                    URIRef(instance),
                                                    row[4].strip(), get_data_type(row[5]))
                    else:
                        aboxgen.link_data(g, URIRef(row[0].strip()),
                                          URIRef(propread.getABoxIRI() + SLASH + format_iri(row[2].strip())),
                                          row[4].strip())

"""Returns the corresponding data type syntax for a given data type"""
def get_data_type(data_type):
    if data_type.strip().lower() == DATA_TYPE_STRING:
        return XSD.string
    elif data_type.strip().lower() == DATA_TYPE_INTEGER:
        return XSD.integer
    elif data_type.strip().lower() == DATA_TYPE_FLOAT:
        return XSD.float
    elif data_type.strip().lower() == DATA_TYPE_DOUBLE:
        return XSD.double
    elif data_type.strip().lower() == DATA_TYPE_DATE_TIME:
        return XSD.dateTime
    else:
        return data_type

"""Formats an IRI string to discard characters that are not allowed in an IRI"""
def format_iri(iri):
    iri = iri.replace(":"," ")
    iri = iri.replace(",", " ")
    iri = iri.replace(" ","")
    return iri

"""Converts an IRI into a namespace"""
def create_namespace(IRI):
    print(IRI)
    return Namespace(IRI)

"""This function checks the validity of the excel template header and iterates over each data row until the whole
content of the template is converted into RDF"""
def convert_into_rdf(file_path):
    print('Provided file path:', file_path)
    if not path.isfile(file_path):
        print('The provided file path is not valid.')
        return
    with open(file_path, 'rt') as csvfile:
        rows = csv.reader(csvfile, skipinitialspace=True)
        line_count = 0
        for row in rows:
           if line_count == 0:
               if not is_header_valid(row):
                   print('Found invalid header, so it will terminate now.')
                   break
               else:
                   print('Found valid header, so it is creating a graph model for adding instances to it.')
                   global g
                   g = Graph()

           if line_count > 0:
               process_data(row)
           line_count +=1
           print('[', line_count, ']', row)
    g.set((g.identifier, OWL_NS['imports'], URIRef(propread.getTBoxIRI())))
    g.serialize(destination=propread.getABoxFileName()+propread.getABoxFileExtension(),
                format="application/rdf+xml")

"""This block of codes calls the function that converts the content of ABox excel template into RDF"""
if __name__ == '__main__':
    """Calls the RDF conversion function"""
    files=[]
    abox_filename=[]
    files,abox_filename=select_files()
    for (i,j) in zip(files,abox_filename):
        propread.setABoxFileName(j)
        propread.setABoxFileExtension('.owl')
        convert_into_rdf(i)
