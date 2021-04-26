import netCDF4 as nc
from tqdm import tqdm 
import numpy as np 
import matplotlib.pyplot as plt
from shapely.geometry import Polygon, MultiPolygon, Point, LineString
from shapely.ops import polygonize
import uuid 
from scipy.spatial import Voronoi, voronoi_plot_2d
from pyproj import Proj, CRS, transform

wgs84 = CRS('EPSG:4326')
osgb36 = CRS('EPSG:27700')

def month_num(month_str):
    '''
    Converts a string of month name to that month's number 
    Starting at 0
    '''
    months = {'January':0,'February':1,'March':2,'April':3,'May':4,'June':5,'July':6,'August':7,'September':8,'October':9,'November':10,'December':11}
    month = months[month_str]
    return month

month_str = 'March'
month = month_num(month_str)

def read_nc(var_name,loc=True):
    '''
    Given a variable in the HadUK Grid dataset, reads the file
    and returns the grid of observations.
    '''
    fn = 'HadUK Files/'+var_name+'_hadukgrid_uk_1km_mon_201901-201912.nc'
    ds = nc.Dataset(fn)
    var_grid = ds.variables[var_name][:]
    if loc == True:
        lon = ds.variables['longitude'][:]
        lat = ds.variables['latitude'][:]
        ds.close()
        return lon, lat, var_grid
    else:
        ds.close()
        return var_grid

lon,lat,tasmin = read_nc('tasmin',loc=True)
tasmax = read_nc('tasmax',loc=False)
tas = read_nc('tas',loc=False)


original_shape = np.shape(lon)
lon = lon.flatten()
lat = lat.flatten()

lat,lon = transform(osgb36,wgs84,lat,lon)

lat = np.reshape(lat,original_shape)
lon = np.reshape(lon,original_shape)




fig,ax = plt.subplots(1,3,figsize=(12,4))
for i in range(3):
    ax[i].set_xlabel('Longitude')
    ax[i].set_ylabel('Latitude')

plt.suptitle('HadUK 1km resolution dataset: '+month_str+' 2019')
rain_plot = ax[0].imshow(tasmin[0,:,:],origin='lower')
fig.colorbar(rain_plot,ax=ax[0])
ax[0].title.set_text('Minimum air temperature (C)')
temp_plot = ax[1].imshow(tas[0,:,:],origin='lower')
fig.colorbar(temp_plot,ax=ax[1])
ax[1].title.set_text('Mean air temperature (C)')
sun_plot = ax[2].imshow(tasmax[0,:,:],origin='lower')
fig.colorbar(sun_plot,ax=ax[2])
ax[2].title.set_text('Maximum air temperature (C)')
plt.savefig('OutputImages/TemperaturePlot.png')

def gridded_data_to_array(nc_vars):
    overall_centroids = []
    overall_polygons = []
    var_store = np.array([[0 for i in range(len(nc_vars))]])
    for i in range(len(nc_vars)):
        nc_vars[i] = nc_vars[i].filled(np.nan)
    for i in tqdm(range(len(lat[:,0]))):
        for j in range(len(lon[0,:])):
            nc_var_temp = [0 for i in range(len(nc_vars))]
            for k in range(len(nc_vars)):
                nc_var_temp[k] = nc_vars[k][month,i,j]
            if nc_var_temp[0] > -1e8:
                point = [lat[i,j],lon[i,j]]
                centroid = Point(point).wkt
                if i == len(lat[:,0]):
                    y1 = lat[i,j] - ((lat[i,j]-lat[i-1,j])/2)
                    y2 = lat[i,j] + ((lat[i,j]-lat[i-1,j])/2)
                    x1 = lon[i,j] - ((lon[i,j]-lon[i,j-1])/2)
                    x2 = lon[i,j] + ((lon[i,j+1]-lon[i,j])/2)
                if j == len(lat[0,:]):
                    y1 = lat[i,j] - ((lat[i,j]-lat[i-1,j])/2)
                    y2 = lat[i,j] + ((lat[i+1,j]-lat[i,j])/2)
                    x1 = lon[i,j] - ((lon[i,j]-lon[i,j-1])/2)
                    x2 = lon[i,j] + ((lon[i,j]-lon[i,j-1])/2)
                else:
                    y1 = lat[i,j] - ((lat[i,j]-lat[i-1,j])/2)
                    y2 = lat[i,j] + ((lat[i+1,j]-lat[i,j])/2)
                    x1 = lon[i,j] - ((lon[i,j]-lon[i,j-1])/2)
                    x2 = lon[i,j] + ((lon[i,j+1]-lon[i,j])/2)

                polygon_list = [[x1,y1],[x1,y2],[x2,y2],[x2,y1],[x1,y1]]
                polygon = Polygon(polygon_list).wkt
                overall_centroids.append(centroid)
                overall_polygons.append(polygon)
                var_store = np.append(var_store,[nc_var_temp],axis=0)
    return overall_centroids, overall_polygons, var_store

points,polygons,nc_vars = gridded_data_to_array([tasmin,tas,tasmax])
overall_tasmin,overall_tas,overall_tasmax = nc_vars[:,0],nc_vars[:,1],nc_vars[:,2]


def COP_heating(Th,Tc,n):
    return n*(273.15+Th)/(Th-Tc)

t_c = np.linspace(-10,20,100)
COP_store = COP_heating(35.0,t_c,0.5)

plt.figure(figsize=(10,4))
plt.title('Heat pump coefficient of performance for an assumed $\eta$ of 0.5\
     \n and heating temperature of 35C.')
plt.grid()
plt.xlabel('Outside Air Temperature (C)')
plt.ylabel('COP')
plt.plot(t_c,COP_store,linewidth=2.5,c='k')
plt.savefig('OutputImages/HeatPumpCOP.png')

COP_tas = COP_heating(35.0,np.array(overall_tas),0.5)
COP_tasmin = COP_heating(35.0,np.array(overall_tasmin),0.5)
COP_tasmax = COP_heating(35.0,np.array(overall_tasmax),0.5)

plt.figure(figsize=(10,4))
plt.grid()
plt.title('Histogram of COP frequency across the UK in '+month_str+' 2019')
plt.hist(COP_tas,bins=200,color='k',alpha=0.3,label='Mean Air Temp')
plt.hist(COP_tasmin,bins=200,color='tab:blue',alpha=0.3,label='Min Air Temp')
plt.hist(COP_tasmax,bins=200,color='tab:orange',alpha=0.3,label='Max Air Temp')
plt.legend()
plt.ylabel('frequency')
plt.xlabel('COP')
plt.savefig('OutputImages/COPHistogram.png')

