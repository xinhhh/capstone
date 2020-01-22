from PIL import Image
from pytesseract import *
import urllib.request
from datetime import datetime
import json
import time
import random
import requests
#read and download image
def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False
def ocr():
    result = {}
#     URL = "https://api.data.gov.sg/v1/environment/air-temperature"
#     r = requests.get(url = URL)
#     data = r.json()
#     temp = str(data["items"][0]["readings"][1]["value"])
#     URL2 = "https://api.data.gov.sg/v1/environment/wind-speed"
#     r = requests.get(url = URL2)
#     data = r.json()
#     speed = str(data["items"][0]["readings"][1]["value"])
    url = 'https://www.solar-repository.sg/ftp_up/weather/500_Weather.png'
    response = urllib.request.urlretrieve(url, '500_image.png')
    #scan image provided. 
    im = Image.open('500_image.png')
    text = image_to_string(im)
    im.close()
    r = text.split('\n')
    for i in r:
        #print(i)
        if i.startswith('Ambient'):
            temp = i.split(' ')[2]
            if (temp == '|'):
                temp = i.split(' ')[3]
            if (len(temp.split('.')))>2:
                temp = temp[:-1]
            if (not is_number(temp)):
                temp = str(random.uniform (26, 32))
            
            result["temperature"] = temp
        if i.startswith('Global'):
            irrad = i.split(' ')[2]
            if (irrad == '|'):
                irrad = i.split(' ')[3]
            if (len(irrad.split('.')))>2:
                irrad = irrad[:-1]
            if (not is_number(irrad)):
                #check the time. if night, then print zero. 
                hou = datetime.now().hour
                if (( hou <= 7 )or (hou > 18)):
                    irrad = 0 
                else:
                    irrad=str(random.uniform(0,100)) 
            result['irradiance'] =irrad
        if i.startswith('Wind Speed'):
            speed = i.split(' ')[2]
            if (speed == '|'):
                speed = i.split(' ')[3]
            if (not is_number(temp)):
                speed = str(random.uniform (0, 5))
            
            result["windspeed"] = speed
    if "windspeed" not in result:
        result["windspeed"] = "0"
    if "irradiance" not in result:
        result["irradiance"] =str(random.uniform(0,100)) 
    if "temperature" not in result:
        result["temperature"] =  str(random.uniform (26, 32))
    now = datetime.now() # current date and time
    
    result['year']= now.strftime("%Y")
    result['month'] = now.strftime("%m")
    result['date']= now.strftime("%d")
    result['time'] =now.strftime("%H:%M:%S")
    with open ('data.json', 'w') as outfile:
        json.dump(result, outfile)
try:
    ocr()
except Exception as e:
    with open ('error log.txt', 'w') as outfile:
        outfile.write('error occurred\n')
        outfile.write(str(e))
        outfile.write('I said end of message')
