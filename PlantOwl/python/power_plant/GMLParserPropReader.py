##########################################
# Author: Feroz Farazi (msff2@cam.ac.uk) #
# Date: 06 Jan 2021                      #
##########################################
import configparser

config = configparser.RawConfigParser()
config.read('./conf/GMLParser.properties')

def getNOfMapsInAnAboxFile():
    return config.get('ABOX', 'number.of.maps.in.an.abox.file')

def getClassLinearRing():
    return config.get('TBOX', 'class.linear.ring')

def getPropertyPosList():
    return config.get('TBOX', 'property.pos.list')

def getABoxIRI():
    return config.get('ABOX', 'kb.abox.iri')

def getABoxFileName():
    return config.get('ABOX', 'kb.abox.file.name')

def getABoxFileExtension():
    return config.get('ABOX', 'kb.abox.file.extension')

def getShapeLengthVocabulary():
    return config.get('ABOX', 'shape.length.vocab')

def getShapeAreaVocabulary():
    return config.get('ABOX', 'shape.area.vocab')

def getRefDateVocabulary():
    return config.get('TBOX', 'property.ref.date')

def getLucodeVocabulary():
    return config.get('TBOX', 'property.lucode')

def getCromeIDVocabulary():
    return config.get('TBOX', 'property.crome.id')

def getObjectIDVocabulary():
    return config.get('TBOX', 'property.object.id')

def getDataTypePolygonalPoints():
    return config.get('TBOX', 'data.type.polygon.points')

def getClassEnvelope():
    return config.get('TBOX', 'class.envelope')

def getSrsName():
    return config.get('TBOX', 'property.srs.name')

def getSrsDimension():
    return config.get('TBOX', 'property.srs.dimension')

def getLowerCorner():
    return config.get('TBOX', 'property.lower.corner')

def getUpperCorner():
    return config.get('TBOX', 'property.upper.corner')

def getCentrePoint():
    return config.get('TBOX', 'property.centre.point')

def getBoundedBy():
    return config.get('TBOX', 'property.bounded.by')

if __name__ == '__main__':
    print(getNOfMapsInAnAboxFile())
    print(getClassLinearRing())
    print(getPropertyPosList())
    print(getABoxIRI())
    print(getABoxFileName())
    print(getABoxFileExtension())
    print(getShapeLengthVocabulary())
    print(getShapeLengthVocabulary())
    print(getRefDateVocabulary())
    print(getLucodeVocabulary())
    print(getCromeIDVocabulary())
    print(getObjectIDVocabulary())
    print(getDataTypePolygonalPoints())
    print(getClassEnvelope())
    print(getSrsName())
    print(getSrsDimension())
    print(getLowerCorner())
    print(getUpperCorner())
    print(getCentrePoint())
    print(getBoundedBy())