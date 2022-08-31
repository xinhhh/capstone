###############################################
# Author: Markus Hofmeister (mh807@cam.ac.uk) #
# Date: 18 Jan 2022                           #
###############################################

""" 
This module investigates the capabilities of the Energy Performance Certificates API
https://epc.opendatacommunities.org/docs/api/non-domestic
"""

import json

import pandas as pd
import requests


# Personal authentification token
auth = ''

# Example postcode
postcode = 'PE301BB'

# Specify API endpoints
url_epc_base = 'https://epc.opendatacommunities.org/api/v1/non-domestic/search?postcode='
url_rec_base = 'https://epc.opendatacommunities.org/api/v1/non-domestic/recommendations/'
headers = {'Authorization': 'Basic {}'.format(auth),
           'Accept': 'application/json'}

#####################################################################################

# Search certificates for postcode  
url_epc = url_epc_base + str(postcode)
r = requests.get(url=url_epc, headers=headers)
if r.status_code == 200:
    epcs = r.json()
else:
    raise RuntimeError('No buildings could be retrieved for specified post code')

# 1) Request all energy performance certificates and create DataFrame
epc_data = pd.DataFrame(index=sorted(epcs['column-names']))
cols = ['Bld_{}'.format(i+1) for i in range(len(epcs['rows']))]
for i in range(len(epcs['rows'])):
    epc_data[cols[i]] = epc_data.index.map(epcs['rows'][i])
epc_data.to_csv('./data/nondomestic_epc_summary.csv')

# 2) Request all recommendations
recommendations = {}
# Loop through all returned buildings
for i in range(len(epcs['rows'])):
    uprn = epcs['rows'][i]['uprn']
    lmk_key = epcs['rows'][i]['lmk-key']
    # Request recommendations for current building (if available)
    url_rec = url_rec_base + str(lmk_key)
    r = requests.get(url=url_rec, headers=headers)
    if r.status_code == 200:
        recommendations[uprn] = r.json()['rows']
    else:
        print('No recommendations could be retrieved for current building')
# Write all recommentaions to JSON file
with open('./data/nondomestic_epc_recommendations.json', "w") as write_file:
    json.dump(recommendations, write_file, indent=4)