# Module 2 Final Assignment

###### Nedap University 4.0 2018–2020

###### Authors

 - @silvanverschuur
 - @erikhuizinga

## Introduction

Time flies, and we already arrived at the end of the second module of the Nedap University.
In the last few weeks you learned all the ins and outs about the fascinating subject of networking.
This assignment gives you the chance to combine your programming skills from the previous module with all this cool networking knowledge!

## Summary

During the module you learned about networks, layers and different protocols.
Where TCP guarantees reliable transfer of information, UDP does not.
For this assignment you will create a wireless storage medium, think of it like a simple NAS, with which you should achieve reliable file transfer using the UDP protocol.

The assignment should be performed individually.
Discussion about the assignment is allowed, but you should always be able to defend your programming and design choices.
You've got 11 work days to finish the assignment and demonstrate your final results.

To be able to demonstrate your skills, you will be provided with a Raspberry Pi 3 Model B+ starter kit.
The Raspberry Pi will be pre-installed with the Raspbian Stretch Lite image (a minimal Linux distro without GUI), Java 8, the Java Cryptography Extension (JCE) and some other configuration.
When the device boots, an ad-hoc Wi-Fi network is set up.
The Raspberry Pi setup guide to configure your laptop and communicate with the device can be found further down this document.
The ad-hoc network should be used to communicate between your laptop and the Raspberry Pis.

## Requirements

The application you will be making consists of two parts:

 0. a storage application (server) on the Raspberry Pi
 0. a desktop / laptop client which connects to the Pi

##### Minimal requirements

 - You should be able to upload and download files from the client to the Raspberry Pi Server.
 - Your applications support files of any size and type.
 - To keep it interesting, use UDP combined with an ARQ protocol.
 You are not allowed to use TCP/IP.
 - The client should be able to ask for and list all available files on the Raspberry Pi.
 - You should be able to pause and resume downloads at any time.
 - Any errors or failures should be handled gracefully.
 - The server should be able to transfer several files at the same time.
 Under what circumstances can this improve or deteriorate the total transfer speed / quality?
 - You should be able to prove that the file you download from the server is exactly the same as the one on the server, and the other way around (data integrity).
 - Your client should be able to discover the server on a local network without knowing its IP address.
 - Your client should be able to show statistics about download speeds, packet loss, retransmissions, etc.
 - *Have fun!*

##### Stretch goals (bonus)

 - Compressed file transfer.
 Hint: transmission speeds can improve when using compression.
 However, strong compression algorithms might not be quick for some files, resulting in lower total throughput.
 - Mesh network support.
 Download a file from a Raspberry Pi out of range of the Wi-Fi from your laptop.
 Any (unknown) number of intermediate nodes should provide and maintain the connection.
 Hint: it is possible to simulate a Raspberry Pi out of range by blacklisting a Pi from your computer.
 - Encrypted file transfer.
 Prove this by transferring a text file and creating a Wireshark dump in which none of the text is visible, unless decrypted.
 Also show that decryption of this dump actually results in the original file.

##### Deliverables

 - During development you should work with version control in Git.
 - Keep your code in a repository via GitHub.
 - The repo should be accessible to the coaches (or just make it public).
 - Pro tip: try working with branches and pull requests!
 - For submission, link to a version tagged 'final' on GitHub.
 Email the link to the tag to nu4coaches@nedap.com.
 - The project should have the following content and structure:
   - There is a README file in the project root with instructions how to install and start the applications (clients and servers).
   - Think about which directories and files are necessary, and any pre-conditions that should be met for successful installation.
   - After reading this file, someone unfamiliar with the project should be able to install and execute the application and transfer files without any problem.
   Hint: to verify your README contains all the required steps, checkout your project into a new directory on a different client and server than your own and follow the instructions yourself.
 - Any necessary but non-standard predefined classes and libraries should be included in JARs.
 - Typical causes that make the installation and compilation procedure fail are names and paths or hardcoded URLs, so make sure to not use any.
 Test this before submitting your project.

##### Report

This assignment is a lot of work, therefore we do not want you to focus to much on the meta part of the process.
You do have to write a report.
The purpose of your report should be to explain the protocol you designed and how you got to the values of the several parameters.
E-mail your report to nu4coaches@nedap.com as a PDF.
Pro tip: [markdowntopdf.com](https://www.markdowntopdf.com)

## Tournament

On Thursday the 19th of April 2019 we will host a tournament!
You and your applications will compete against the other competitors' applications.
The competitor that can transmit a large file without errors in unstable network conditions, which will be provided by the coaches, in the least amount of time wins!

## Evaluation

The minimal requirements as outlined in this report are required to achieve a positive evaluation that will let you pass the project.
If your implementation exceeds the minimum requirements the evaluation will be even better!
The coaches will, together, review your applications, report and tournament results on the final tournament day.

## Criteria

The list of criteria below will be used to evaluate your project.
Use it to keep track of and judge your own progress.

##### Code

 - The application has all the required components and functionality.
 - There is a README file with installation and execution instructions.
 - The application compiles and executes without errors.
 - The application has been sufficiently documented with Javadoc.
 - The implementation of large and/or complex methods has been documented internally.
 - The application layout/design is understandable and accessible.
 - Bytecode of predefined, external, non-standard Java classes is submitted with the code.

##### Programming Style

- Names of classes, variables and methods are well-chosen, understandable and possibly self-explanatory.
- All code is efficiently and neatly implemented.
- The program is easily maintainable.
 Signs of high maintainability include, but are not limited to:
   - Abstraction
   - Correct use of constants
   - Logical variable names
   - No magic numbers
   - No code smells
 - Exception mechanisms are used appropriately.
 - Concurrency constructs are used properly.

##### Testing

 - Appropriate unit tests are provided.
 - Appropriate system tests are provided.
 - Sufficient test coverage is reached.
 - Tests are well documented, or better: the tests are self-documenting (a testable code base with good test can be self-documenting).
 - All classes in the system have been tested by unit testing.
 - All tests pass.

## Organisational overview Calendar

| Calendar week | Day            | Time  | Activity                  |
|---------------|----------------|-------|---------------------------|
| 14            | Wed April 3rd  | 09:00 | Final assignment kickoff  |
| 16            | Wed April 17th | 23:59 | Deadline for deliverables |
| 16            | Thu April 18th | 15:00 | Tournament and review     |

---

# Raspberry Pi Setup Guide

## Image

You will be provided with a Raspberry Pi 3B+ starter kit.
The Pi is pre-installed with the latest Raspbian image.
This is a minimal Linux distribution without a graphical interface.

A Java SDK is also installed including the Java Cryptography Extension (JCE).

To connect to the Pi you can use any SSH client.
The default credentials are:
 - Username: `pi`
 - Password: `raspberry`

## Networking

Besides the physical ethernet port, the Raspberry Pi is configured to start an ad-hoc Wi-Fi network on startup.
This network can be used to connect the Pi to your laptop without the need of switches/cables.

### Physical ethernet

The physical adapter is configured to use DHCP to obtain an IP address.

### Wi-Fi

The name of the Wi-Fi network is unique for each student.

 - SSID: `nu-pi-$name`
 - Security: none (don't try this at home, kids!)
 - Static IP address: `172.16.1.1`

#### Setup a Wi-Fi connection with the Pi

For Mac users it's very easy to connect to the Wi-Fi network of the Pi.
For Windows users it's a little bit more difficult because Microsoft decided to reduce support for ad-hoc Wi-Fi networks since Windows 8.1.

It's recommended to use a static IP address for the Wi-Fi adapter of your laptop:

- IP: `172.16.1.2`
- Subnet `255.255.0.0`

**Important:** you may need to disable the Nedap security policy of you computer's VPN client.

##### Mac

 0. Open System Preferences -> Network.
 0. (Optional) create a new location, so you don't mess with your current settings.
 0. Select your Wi-Fi network in the adapter list.
 0. Click on advanced and open the TCP/IP to change the static IP address / subnet.
 0. Press apply and your Mac should be connected to the Wi-Fi network of the Pi.

| Network                      | Advanced Wi-Fi preferences    |
|------------------------------|------------------------------|
| ![](doc/macOS-network-1.png) | ![](doc/macOS-network-2.png) |

##### Windows

 0. Open the Network and Sharing Center.
 0. Click Set up a new connection or network.
 0. Double click Manually connect to a wireless network.
 0. Enter the SSID of the ad-hoc network into the Network name field.
 0. Uncheck Start this connection automatically.
 0. Click Next and then Close.
 
Open a command prompt and enter:

`netsh wlan set profileparameter $ssid connectiontype=ibss connectionmode=manual`

After setting up, run the following command whenever you want to connect:

`netsh wlan connect $ssid`

To remove the network profile from Windows, run the following command:

`netsh wlan delete profile $ssid`

## Project setup

The `nedap/nu-module-2` repo on GitHub contains a sample setup of a Gradle project.
It contains a task to automatically send a JAR to the Raspberry Pi over SSH.

## Linux service

To start and stop our service when the Pi starts, we need a service wrapper.
Create a new wrapper using `sudo vi /lib/systemd/system/num2.service` and paste the following contents into it:

```
[Unit]
Description=Nedap U Service
After=multi-user.agent

[Service]
Type=simple
ExecStart=/usr/bin/java -jar /home/pi/NUM2.jar
Restart=on-abort
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
```

After doing a reload with `sudo systemctl daemon-reload`, the service can be started by invoking `sudo systemctl start num2.service` and stopped with `sudo systemctl stop num2.service`.

See also [this resource](http://www.diegoacuna.me/how-to-run-a-script-as-a-service-in-raspberry-pi-raspbian-jessie/) about running scripts as services.
