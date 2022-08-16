# BodyAreaNetwork

Bachelor Degree in IT - University of Verona - A.Y. 19/20

Bachelor thesis project

## Description

An Android application responsible for collecting data from Nordic:52 devices and Wagoo Smart Glasses, to connect to an another smartphone, 
with an Andorid Wearable and to exchange messages with all these devices

thingyserverwear: module for smartwatch app, responsible for starting vibration once received messages from phone app
thingylib: support library for the management of the nordicBleDevice, offers interfaces and manager to communicate with these devices.
wgcom: support library for the management of the wagoo glasses, offers interfaces to communicating with these devices.
app: module for phone app
    important files:
    
      db:
        DataMapper: java class responsible for saving data received from sensor into local db and to push them to remote db
        
      models:
        GeneralDevice: interface which is used as superclass for the Nordic52 and waggo glasses devices.
        _PeriodSample: Object used as temporary storage for sensor data
        
      services:
        BluetoothConnectionService: service responsible for the bluetooth connection with the doctor app
        DataCollectionService: service for the collection of sensor data, will work even in background, implements callback for wagoo glasses and nordic, phone sensor directly managed in PhonePeriodSample
        ThingyService: service necessary for the management of the Nordic52
        closingService: service for operations before the app is closed
