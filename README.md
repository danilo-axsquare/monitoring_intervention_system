# monitoring_intervention_system
Monitoring and intervention system

##Synopsis
This project contains the code to implement a monitoring system of servers of a corporate intranet. Using an Android application you can view the data collected and perform actions on the monitored hosts.

##Motivation
I realized this project at the end of my university career. In addition, this project was carried out as there are no monitoring systems used specifically for smartphones. But most importantly, no monitoring system allows you to perform actions on the monitored hosts.

##Project structure
The project can be split in 3 entity:
* Hosts
* Server
* Smartphone/application

#Host:
In this project, the **hosts** are computers to be monitored. To retrieve informations are used bash scripts that are run periodically. The scripts were tested on the following operating systems: CentOS and RedHat 4/5/6.
The bash scripts collect two types of information/data: <br>
1. **Description host specs**, this informations describes the monitored host, these data rarely changes:
  * RAM memory in MB (e.g. 2048)
  * IP address
  * Operating system (e.g. CentOS 6)
  * Hostname (e. g. webserver.danilo.com)
  * CPU type (e. g. Intel Xeon )
  * Disk information (e. g. xvda: 16.1GB)
  * Machine type (e. g. Virtual)
  
2. **Health information**, this data describes the state of health of a host , these data frequently changes:
  * RAM used (e. g. 54% )
  * CPU used (e. g. 80% )
  * Use partitions and mount point (e. g. 54% / - 13% /boot )
  * Number of users connected (e. g. 3)
