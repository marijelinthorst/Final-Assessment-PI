# Module 2 Final Assignment

###### Nedap University 4.0 2018–2020

###### Author: @MarijeLinthorst

## Installation
Due to my inexperience with gradle, the installation requires that changes be made to the build.gradle and gradle.settings file.
All the necessary changes are already present as comments in the file and thus only need to be interchanged with each other.

To install the client program navigate to the location of the src folder.

The build.gradle file must be modified such that the `mainClassName = 'com.nedap.university.Client'`
and not `'com.nedap.university.Server'`
Installation can now be done by running `gradlew.build` A jar file with the name `FileServer-client-PI` will then appear in the build.libs folder

To install the server program again make sure to navigate to the location of the src folder.
The build.gradle file must be modified such that the `mainClassName = 'com.nedap.university.Server'`
and not `'com.nedap.university.Client'`

Before continuing make sure a WiFi connection to the pi is present, as this will be required for the deployment.
Once this is done. First execute `gradlew build`

Once the build is complete execute `gradlew deploy`, this will move the server program to the raspberry pi.

It is possible that a reboot of the pi is required before it is working properly.
To prevent this a ssh connection to the pi can also be made and the command `sudo systemctl stop num2.service` can be executed to ensure nothing is running during deployment.



## Startup
The Server will run upon boot of the pi and connection of the client to the server is done automatically once the client program is started and a WiFi connection with the pi is available.
The client can be started by navigating to the build.libs directory where its jar is located and then running `java -jar` and then its name, which should be `FileServer-client-PI-0.1`

Both the client and server program require a Desktop folder to be present at the location of the program, and in that either a Server of Client folder. This is because this will be the folder in which files will be uploaded and downloaded.
If these are not available it is likely going to fail.

## Usage
Sadly not all required functions were implemented.

You can get a list with files from the pi, but it will continue retransmitting
You can download a file. The file checksum will incorrect, but the file will be there.
You can start uploading a file, all packets will arrive, but it gets stuck in a loop and will not write the bytes to a file. 

It is also possible for the reliable transfer protocol to fail, the cause behind this is unknown and there was not enough time to find a solution to this.
If an errors occurs, or it gets stuck in a loop, a reboot of the client and server should allow for you to continue.
