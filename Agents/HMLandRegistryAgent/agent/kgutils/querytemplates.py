################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 12 Oct 2022                            #
################################################

# The purpose of this module is to provide templates for (frequently)
# required SPARQL queries

import uuid

from py4jps import agentlogging

from agent.datamodel.iris import *

# Initialise logger
logger = agentlogging.get_logger("prod")


#
# EXTERNAL SPARQL QUERIES
#
# HM Land Registry Price Paid Data model detailed here:
# https://landregistry.data.gov.uk/app/root/doc/ppd
def get_transaction_data_for_postcodes(postcodes: list) -> str:
    # Retrieve price paid transaction data for postcode(s)
    
    # Create list of postcodes of interest
    values = '", "'.join(postcodes)
    values = values.replace(',', f'^^<{XSD_STRING}>')
    values = f'"{values}"^^<{XSD_STRING}>'

    query = f"""
        SELECT ?tx_iri ?price ?date ?property_type ?tx_category
               ?address_iri ?paon ?saon ?street ?town ?postcode ?district ?county
        WHERE
        {{
        VALUES ?postcode {{ {values} }}
        ?address_iri <{LRCOMMON_POSTCODE}> ?postcode . 
        ?tx_iri <{LRPPI_PROPERTY_ADDRESS}> ?address_iri ; 
                <{LRPPI_PRICE}> ?price ; 
                <{LRPPI_DATE}> ?date ; 
                <{LRPPI_PROPERTY_TYPE}>/<{RDFS_LABEL}> ?property_type ; 
                <{LRPPI_TX_CATEGORY}>/<{SKOS_LABEL}> ?tx_category . 
        OPTIONAL {{ ?address_iri <{LRCOMMON_PAON}> ?paon }} 
        OPTIONAL {{ ?address_iri <{LRCOMMON_SAON}> ?saon }} 
        OPTIONAL {{ ?address_iri <{LRCOMMON_STREET}> ?street }} 
        OPTIONAL {{ ?address_iri <{LRCOMMON_TOWN}> ?town }} 
        OPTIONAL {{ ?address_iri <{LRCOMMON_DISTRICT}> ?district }} 
        OPTIONAL {{ ?address_iri <{LRCOMMON_DISTRICT}> ?county }} 
        }}
    """
    
    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query


# HM Land Registry UK House Price Index (UKHPI) detailed here:
# https://landregistry.data.gov.uk/app/ukhpi/doc
# Returned date format is xsd:gYearMonth (YYYY-MM)
def get_ukhpi_monthly_data_for_district(ons_local_authority_iri, 
                                        months:int=None) -> str:
    """
    Retrieve UKHPI data for a given local authority ONS IRI 

    Arguments:
        ons_local_authority_iri {str} - IRI of the local authority as used by
                                        Office for National statistics
        months {int} - Number of months for which to retrieve date
    """

    query = f"""
        SELECT ?month ?ukhpi_value 
        WHERE {{
        ?hm_region <{RDFS_SEEALSO}> <{ons_local_authority_iri}> . 
        ?ukhpi <{UKHPI_REFREGION}> ?hm_region ; 
               <{UKHPI_INDEX}> ?ukhpi_value ; 
               <{UKHPI_REF_MONTH}> ?month
        }}
    """
    if months:
        query += f"""
            ORDER BY DESC(?month)
            LIMIT {months}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query


#
# SPARQL QUERIES
#
def get_instantiated_properties_with_location_info(property_iris: list = None) -> str:
    # Retrieve instantiated properties with location information from KG

    values_statement = ""
    if property_iris:
        # Create list of IRIs of interest
        iris = '> <'.join(property_iris)
        iris = '<' + iris + '>'
        values_statement = f"VALUES ?property_iri {{ {iris} }} "

    query = f"""
        SELECT ?property_iri ?address_iri ?postcode_iri ?district_iri
               ?property_type ?postcode ?street ?number ?bldg_name ?unit_name 
               ?tx_iri
        WHERE {{
        {values_statement}
        ?property_iri <{RDF_TYPE}> ?property_type . 
        ?property_type <{RDFS_SUBCLASS}> <{OBE_PROPERTY}> . 
        ?property_iri <{OBE_LOCATEDIN}> ?district_iri ; 
                      <{OBE_HAS_ADDRESS}> ?address_iri . 
        ?address_iri <{OBE_HAS_POSTALCODE}> ?postcode_iri . 
        ?postcode_iri <{RDFS_LABEL}> ?postcode . 
        OPTIONAL {{ ?address_iri <{ICONTACT_HAS_STREET}> ?street }}
        OPTIONAL {{ ?address_iri <{ICONTACT_HAS_STREET_NUMBER}> ?number }}
        OPTIONAL {{ ?address_iri <{ICONTACT_HAS_BUILDING}> ?bldg_name }}
        OPTIONAL {{ ?address_iri <{OBE_HAS_UNIT_NAME}> ?unit_name }} 
        OPTIONAL {{ ?property_iri <{OBE_HAS_LATEST_TRANSACTION}> ?tx_iri }} 
        }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query


def get_all_properties_with_postcode() -> str:
    # Retrieve instantiated properties with location information from KG

    query = f"""
        SELECT ?property_iri ?postcode
        WHERE {{
        ?property_iri <{RDF_TYPE}> ?property_type ; 
                      <{OBE_HAS_ADDRESS}> ?address_iri . 
        ?address_iri <{OBE_HAS_POSTALCODE}>/<{RDFS_LABEL}> ?postcode . 
        }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query


def update_transaction_record(property_iri:None, address_iri:None, tx_iri:None, 
                              new_tx_iri:None, price:None, date:None, 
                              ppd_address_iri:None) -> str:
    # Returns DELETE / INSERT query to instantiate/update transaction record
    
    if property_iri and address_iri and price and date:
        # Create tx_iri if not provided (i.e. not instantiated yet)
        if not tx_iri:
            tx_iri = KB + 'Transaction_' + str(uuid.uuid4())
        
        # Start query building blocks
        delete = f"""
            DELETE {{
                <{tx_iri}> <{RDF_TYPE}> <{LRPPI_TRANSACTION_RECORD}> ; 
                           <{LRPPI_PRICE}> ?price ; 
                           <{LRPPI_DATE}> ?date ; 
                           <{OWL_SAME_AS}> ?ppd_tx . 
                <{address_iri}> <{OBE_IS_PRESUMED_MATCH_OF}> ?ppd_address . 
        }}
        """

        insert = f"""
            INSERT {{
                <{property_iri}> <{OBE_HAS_LATEST_TRANSACTION}> <{tx_iri}> . 
                <{tx_iri}> <{RDF_TYPE}> <{LRPPI_TRANSACTION_RECORD}> ;
                           <{LRPPI_PRICE}> \"{price}\"^^<{XSD_INTEGER}> ; 
                           <{LRPPI_DATE}> \"{date}\"^^<{XSD_DATE}> . 
        """
        if new_tx_iri: insert += f"<{tx_iri}> <{OWL_SAME_AS}> <{new_tx_iri}> . "
        if ppd_address_iri: insert += f"<{address_iri}> <{OBE_IS_PRESUMED_MATCH_OF}> <{ppd_address_iri}> . "
        insert += f"}} "

        where =f"""WHERE {{
            <{property_iri}> <{OBE_HAS_ADDRESS}> <{address_iri}> . 
            OPTIONAL {{ <{property_iri}> <{OBE_HAS_LATEST_TRANSACTION}> <{tx_iri}> . 
                        <{tx_iri}> <{LRPPI_PRICE}> ?price ;
                                <{LRPPI_DATE}> ?date . }}
            OPTIONAL {{ <{tx_iri}> <{OWL_SAME_AS}> ?ppd_tx }}
            OPTIONAL {{ <{address_iri}> <{OBE_IS_PRESUMED_MATCH_OF}> ?ppd_address }} 
            }}
        """

        query = delete + insert + where
        # Remove unnecessary whitespaces
        query = ' '.join(query.split())
    
    else:
        query = None

    return query


def get_all_admin_districts_with_price_indices() -> str:
    # Retrieve instantiated administrative districts (i.e. local authorities) 
    # including their ONS equivalent instance and (potentially) instantiated
    # property price indices

    query = f"""
        SELECT ?local_authority ?ons_district ?ukhpi
        WHERE {{
        ?local_authority <{RDF_TYPE}> <{OBE_ADMIN_DISTRICT}> ;
                         <{OWL_SAME_AS}> ?ons_district . 
        OPTIONAL {{ ?local_authority ^<{OBE_REPRESENTATIVE_FOR}> ?ukhpi . 
                    ?ukhpi <{RDF_TYPE}> <{OBE_PROPERTY_PRICE_INDEX}> . }} 
        }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query


def instantiate_property_price_index(district_iri, ppi_iri):
    # Instantiate property price index for a given administrative district
    query = f"""
        INSERT DATA {{
            <{ppi_iri}> <{RDF_TYPE}> <{OBE_PROPERTY_PRICE_INDEX}> . 
            <{ppi_iri}> <{OBE_REPRESENTATIVE_FOR}> <{district_iri}> . 
        }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query


def get_ocgml_crs():
    # Retrieve coordinate reference system (from OCGML endpoint)
    query = f"""SELECT ?crs
        WHERE {{ ?s <{OCGML_SRSNAME}> ?crs . }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())
    return query


def get_matched_buildings() -> str:
    # Retrieve all OntoBuiltEnv building with a OntoCityGml representations
    query = f"""
        SELECT DISTINCT ?obe_bldg
        WHERE {{
            ?obe_bldg <{OBE_HAS_OCGML_REPRESENTATION}> ?ocgml_bldg .
        }}
    """
    # Remove unnecessary whitespaces
    query = ' '.join(query.split())
    return 


def get_matched_ocgml_information(obe_endpoint, ocgml_endpoint, bldg_iris=[]) -> str:
    # Retrieved relevant OCGML information for matched buildings (i.e. OBE building IRIs)
    
    # Create list of IRIs of interest
    values = '> <'.join(bldg_iris)
    values = '<' + values + '>'

    query = f"""
        SELECT DISTINCT ?obe_bldg ?surf (DATATYPE(?geom) as ?datatype) ?geom ?height
        WHERE {{
            SERVICE <{obe_endpoint}> {{
                VALUES ?obe_bldg {{ {values} }}
                 ?obe_bldg <{OBE_HAS_OCGML_REPRESENTATION}> ?ocgml_bldg .
                }}
            SERVICE <{ocgml_endpoint}> {{
                ?ocgml_bldg <{OCGML_BLDG_HEIGHT}> ?height ;
                            <{OCGML_FOOTPRINT}> ?footprint .
                ?surf <{OCGML_ROOT_ID}> ?footprint ;
       		          <{OCGML_GEOM_TYPE}> ?geom .
       		    FILTER (!isBlank(?geom))
            }}
        }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())
    return query


def delete_old_building_elevation(obe_bldg_iris):
    # Delete (potentially) outdated OntoBuiltEnv building elevation triples

    # Create list of IRIs of interest
    values = '> <'.join(obe_bldg_iris)
    values = '<' + values + '>'

    query = f"""        
        DELETE {{
            ?bldg_iri <{OBE_HAS_GROUND_ELEVATION}> ?old_elev .
            ?old_elev <{RDF_TYPE}> ?old_quant_type ;
                        <{OM_HAS_VALUE}> ?old_measure .
            ?old_measure <{RDF_TYPE}> ?old_measure_type ; 
                            <{OM_NUM_VALUE}> ?old_value ;
                            <{OM_HAS_UNIT}> ?old_unit .
            ?old_unit <{RDF_TYPE}> ?old_unit_type ;
                        <{OM_SYMBOL}> ?old_unit_symbol 
        }}
        WHERE {{
                VALUES ?bldg_iri {{ {values} }}
                ?bldg_iri <{OBE_HAS_OCGML_REPRESENTATION}> ?ocgml_bldg .
                OPTIONAL {{ ?bldg_iri <{OBE_HAS_GROUND_ELEVATION}> ?old_elev 
                OPTIONAL {{ ?old_elev <{RDF_TYPE}> ?old_quant_type ;
                                      <{OM_HAS_VALUE}> ?old_measure .
                            ?old_measure <{RDF_TYPE}> ?old_measure_type ;
                                         <{OM_NUM_VALUE}> ?old_value }}
                OPTIONAL {{ ?old_measure <{OM_HAS_UNIT}> ?old_unit .
                            ?old_unit <{RDF_TYPE}> ?old_unit_type ;
                                      <{OM_SYMBOL}> ?old_unit_symbol }}
                }}
        }}
    """

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query

def instantiate_building_elevation(elevation_data):
    # Instantiate building elevation (as retrieved from OntoCityGml instances) 
    # according to OntoBuiltEnv
    # elevation_data: [{'unit': '...', 'obe_bldg': '...', 'elevation': '...'}, ...]

    # Initialise data insert query
    query = f"INSERT DATA {{"

    for d in elevation_data:
        # Create IRIs
        elevation = KB + 'GroundElevation_' + str(uuid.uuid4())
        measure = KB + 'Measure_' + str(uuid.uuid4())
        if d['unit'] == 'm':
            unit = OM_M
        else:
            unit = None
            logger.warn('Building elevation specified in unknown unit, i.e. not metres.')

        # Add triples to instantiate
        query += f"""
            <{d['obe_bldg']}> <{OBE_HAS_GROUND_ELEVATION}> <{elevation}> . 
            <{elevation}> <{RDF_TYPE}> <{OM_HEIGHT}> . 
            <{elevation}> <{OM_HAS_VALUE}> <{measure}> . 
            <{measure}> <{RDF_TYPE}> <{OM_MEASURE}> . 
            <{measure}> <{OM_NUM_VALUE}> "{d['elevation']}"^^<{XSD_FLOAT}>  . 
        """
        if unit:
            query += f"""
                <{measure}> <{OM_HAS_UNIT}> <{unit}> . 
                <{unit}> <{RDF_TYPE}> <{OM_UNIT}> . 
                <{unit}> <{OM_SYMBOL}> "{d['unit']}"^^<{XSD_STRING}> . 
            """

    # Close query
    query += f"}}"

    # Remove unnecessary whitespaces
    query = ' '.join(query.split())

    return query