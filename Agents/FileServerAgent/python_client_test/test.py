"""This script expects an instance of the FileServerAgent to be running locally on port 58090.
   To start the agent on that port, run 'docker-compose up' in the directory above this one.
"""

import os.path
import requests
from requests import status_codes


# Port must match the one specified in docker-compose.yml
url = 'http://localhost:58090/FileServerAgent/upload'

base_dir   = os.path.join(os.path.dirname(__file__),"data")
owl_fpath  = os.path.join(base_dir,'dummy.owl')
log_fpath  = os.path.join(base_dir,'dummy.log')
text_fpath = os.path.join(base_dir,'dummy.txt')

# Test multi-file upload
with open(owl_fpath,'rb') as file_obj1, open(log_fpath,'rb') as file_obj2, open(text_fpath,'rb') as file_obj3:
    # Set the subdirectory in which to store files (optional header)
    headers= {'subDir': 'my_namespace'}
    print("Uploading multiple files to subdir [%s]" % headers['subDir'])

    # Aggregrate file objects in a dict
    # Key names can be anything - but whatever is set will be re-used in the response to return actual filenames
    files = {'file1': file_obj1,'file2': file_obj2,'file3': file_obj3}

    # Post request
    response = requests.post(url, headers=headers, files=files)
    # Extract actual filenames from the response
    if (response.status_code == status_codes.codes.OK):
        for key in files.keys():
            print(" %s uploaded with filename [%s]" % (key,response.headers[key]))
    else:
        print("File upload failed with code %d " % response.status_code)


# Test single file upload
with open(text_fpath,'rb') as file_obj:
    print("\nUploading single file without a subdir")
    files={'file':file_obj}
    response = requests.post(url, files=files)
    if (response.status_code == status_codes.codes.OK):
        for key in files.keys():
            print(" %s uploaded with filename [%s]" % (key,response.headers[key]))
    else:
        print("File upload failed with code %d " % response.status_code)