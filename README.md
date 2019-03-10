# Bluetooth TrackPad App
This Android application simulates the function of a trackpad. It has mouse movement and buttons for mouse clicking.
It requires a Bluetooth server running on the target computer in order for the control to work.
The server will interpret commands coming from the application.

Connection to the server occurs on the app's startup currently. If it fails to connect, you will have to restart the app 
in order to attempt connection again. The app assumes the target host's name is "LAPTOP1" and assumes that its already
paired. In the future, I may add ability to attempt reconnect without restarting the app. I may also add the ability to displaying
the list of paired devices so the user can select the right device. I might make a new app entirely to add these features.

Also, I'm using the Samsung Galaxy 2 7.0 with Android 4.0 to test this app. I encountered errors on other, newer phones with trying 
to retrieve the default Bluetooth Adapter (would return null).
