"""This script expects an instance of the FileServer to be running locally on port 58090.
   To start the server on that port, run 'docker-compose up' in the directory above this one.

   N.B. These examples are intended to illustrate use of the server via one particular
   (Python-based) client and shouldn't be treated as unit tests.
"""

import filecmp
import os.path
import requests
from requests import status_codes


# Port must match the one specified in docker-compose.yml
server_URL = 'http://localhost:49086/FileServer/'
upload_URL = server_URL
download_URL = server_URL
delete_URL = server_URL

# Default credentials for authentication. If you supply a different password via a secret, modify 'fs_pass' on the next line accordingly
auth=('fs_user', 'fs_pass')

# Paths to some dummy data files
base_dir   = os.path.join(os.path.dirname(__file__),"data")
owl_fpath  = os.path.join(base_dir,'dummy.owl')
log_fpath  = os.path.join(base_dir,'dummy.log')
text_fpath = os.path.join(base_dir,'dummy.txt')

# Remote sub-directory used for some uploads
remote_subdir = 'my_namespace/'

# Path used to download a file and compare it to the original that was uploaded
downloaded_txt_fpath  = os.path.join(base_dir,'downloaded_dummy.txt')

# Test multi-file upload
with open(owl_fpath,'rb') as file_obj1, open(log_fpath,'rb') as file_obj2, open(text_fpath,'rb') as file_obj3:
    # Set the subdirectory in which to store files
    print("Uploading multiple files to subdir [%s]" % remote_subdir)

    # Aggregrate file objects in a dict
    # Key names can be anything - but whatever is set will be re-used in the response to return actual filenames
    files = {'file1': file_obj1,'file2': file_obj2,'file3': file_obj3}

    # Post request
    response = requests.post(upload_URL+remote_subdir, auth=auth, files=files)
    # Extract actual filenames from the response
    if (response.status_code == status_codes.codes.OK):
        for key in files.keys():
            print(" %s uploaded with filename [%s]" % (key,response.headers[key]))
    else:
        print("  ERROR: File upload failed with code %d " % response.status_code)


# Test single file upload
text_remote_upload_path=None
with open(text_fpath,'rb') as file_obj:
    print("\nUploading single file without a subdir")
    files={'file':file_obj}
    response = requests.post(upload_URL, auth=auth, files=files)
    if (response.status_code == status_codes.codes.OK):
        text_remote_upload_path = response.headers['file']
        print(" Uploaded with filename [%s]" % text_remote_upload_path)
    else:
        print("  ERROR: File upload failed with code %d " % response.status_code)

# Test single file upload with nested sub-dir
with open(text_fpath,'rb') as file_obj:
    nested_subdir="%sgroup1/" % remote_subdir
    print("\nUploading single file to nested subdir (%s)" % nested_subdir)
    response = requests.post(upload_URL+nested_subdir, auth=auth, files={'file':file_obj})
    if (response.status_code == status_codes.codes.OK):
        for key in files.keys():
            print(" %s uploaded with filename [%s]" % (key,response.headers[key]))
    else:
        print("  ERROR: File upload failed with code %d " % response.status_code)

# Test single file upload with rename
with open(text_fpath,'rb') as file_obj:
    nested_subdir="%ssmarty.txt" % remote_subdir
    print("\nUploading single file with explicit name (%s)" % nested_subdir)
    response = requests.post(upload_URL+nested_subdir, auth=auth, files={'file':file_obj})
    if (response.status_code == status_codes.codes.OK):
        for key in files.keys():
            print(" %s uploaded with filename [%s]" % (key,response.headers[key]))
    else:
        print("  ERROR: File upload failed with code %d " % response.status_code)

# Test single file download
if text_remote_upload_path is not None:
    print("\nRe-downloading file")
    response = requests.get(text_remote_upload_path, auth=auth)
    if (response.status_code == status_codes.codes.OK):
        with open(downloaded_txt_fpath, 'wb') as file_obj:
            for chunk in response.iter_content(chunk_size=128):
                file_obj.write(chunk)
        if filecmp.cmp(text_fpath,downloaded_txt_fpath):
            print("  Downloaded file matches original")
        else:
            print("  ERROR: Downloaded file differs from original")
    else:
        print("  ERROR: File download failed with code %d " % response.status_code)

# Test delete file
if text_remote_upload_path is not None:
    print("\nTesting file deletion")
    response = requests.delete(text_remote_upload_path, auth=auth)
    if (response.status_code == status_codes.codes.OK):
        print(" Deleted "+text_remote_upload_path)
    else:
        print("  ERROR: File deletion failed with code %d " % response.status_code)

    # Try GET deleted file (should fail)
    print("\nAttempting to GET the deleted file")
    response = requests.get(text_remote_upload_path, auth=auth)
    if (response.status_code != status_codes.codes.not_found):
        print("  ERROR: GET deleted file: expected status code %d, but got %d" % (status_codes.codes.not_found,response.status_code) )
    else:
        print("  Fails, as expected with message:\n%s" % response.text)

# Test delete folder
if text_remote_upload_path is not None:
    print("\nTesting file deletion")
    response = requests.delete(delete_URL+remote_subdir, auth=auth)
    if (response.status_code == status_codes.codes.OK):
        print(" Deleted "+remote_subdir)
    else:
        print("  ERROR: Folder deletion failed with code %d " % response.status_code)
    
    # Try GET deleted file (should fail)
    print("\nAttempting to GET a deleted file")
    response = requests.get(download_URL+owl_fpath, auth=auth)
    if (response.status_code != status_codes.codes.not_found):
        print("  ERROR: GET deleted file: expected status code %d, but got %d" % (status_codes.codes.not_found,response.status_code) )
    else:
        print("  Fails, as expected with message:\n%s" % response.text)

# Try GET non-existant file (should fail)
print("\nAttempting GET non-existant file")
response = requests.get(upload_URL, auth=auth)
if (response.status_code != status_codes.codes.not_found):
    print("  ERROR: GET non-existant file: expected status code %d, but got %d" % (status_codes.codes.not_found ,response.status_code) )
else:
    print("  Rejected, as expected with message:\n%s" % response.text)


# Try POST without a file attached (should fail)
print("\nAttempting POST without a file attached")
fname="my_namespace/dummy.owl"
response = requests.post(download_URL+fname, auth=auth)
if (response.status_code != status_codes.codes.bad_request):
    print("  ERROR: POST without a file attached: expected status code %d, but got %d" % (status_codes.codes.bad_request,response.status_code) )
else:
    print("  Rejected, as expected with message:\n%s" % response.text)

