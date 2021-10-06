##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 09 June 2021         #
##########################################

"""This module declare the properties of generating UK Energy Consumption A-boxes"""

from UK_Digital_Twin_Package import EndPointConfigAndBlazegraphRepoLabel

class UKEnergyConsumption:
    
    """Default path of storing owl file """
    StoreGeneratedOWLs = "C:\\Users\\wx243\\Desktop\\KGB\\1 My project\\1 Ongoing\\4 UK Digital Twin\\A_Box\\UK_Energy_Consumption\\UK_Energy_Consumption_KG_2019\\"
    
    """Default path of SleepycatStoragePath"""
    SleepycatStoragePath = "C:\\Users\\wx243\\Desktop\\KGB\\1 My project\\1 Ongoing\\4 UK Digital Twin\\A_Box\\UK_Energy_Consumption\\Sleepycat_UKec_UKtopo"
    
    """Default remote endpoint"""
    endpoint = EndPointConfigAndBlazegraphRepoLabel.UKEnergyConsumptionKG
    
    """Node keys"""
    TotalConsumptionKey = "Total_Electricity_Consumption_of_"
    DomesticConsumptionKey = "Domestic_Electricity_Consumption_of_"
    IndustrialAndCommercialConsumptionKey = "Industrial_and_Commercial_Electricity_Consumption_of_"
    TimePeriodKey = "Time_Period_of_"
    StartTimeKey = "Start_Time_of_"
    
    valueKey = "value_"