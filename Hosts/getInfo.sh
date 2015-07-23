#!/bin/bash
# @Author: Danilo Raspa
# @Date: 09/01/2015
#
#Questo script raccoglie le informazioni della macchine e li invia al server tramite POST
pathToServer="http://192.168.0.222/internal.php"
#memoria totale in MB
let memTotal=$(cat /proc/meminfo | grep MemTotal | cut -d ':' -f 2 | cut -d'k' -f 1 | tr -d ' ')/1024
#IPaddress
ip=$(ifconfig | grep -v "eth[0-9]:[0-9]" | grep eth -A2 | grep "inet addr:" | cut -d ':' -f 2 | cut -d ' ' -f 1| head -n1)
#sistema operativo
SOversion=$(cat /etc/*-release | cut -d '(' -f 1 | head -n1|tr ' ' '_')
#hostname
hostnameVar=$(cat /proc/sys/kernel/hostname)
#Tipo CPU
CPUtype=$(cat /proc/cpuinfo | grep "model name" |head -n1| cut -d ':' -f 2 | tr ' ' '_')
#Dischi e dimensioni(da vedere se sono presenti due dischi)
diskVar=$(fdisk -l | grep 'Disk /dev/' | grep -v VolGroup |grep -v mapper| awk -F'/dev/' '{print $2}' | cut -d',' -f 1 | tr  '\n' '|' |  tr ' ' '_')
numDisk=$(fdisk -l | grep 'Disk /dev/' | grep -v VolGroup |grep -v mapper| wc -l)
#virtuale o fisica
dmesg | grep Xen > /dev/nul
if [ $? -eq 0 ]; then
	machineType=virtuale
else
	machineType=fisica
fi

curl -i \
-H "Accept: application/json" \
-H "Content-Type:application/json" \
-X POST --data '{"operation":"Information", "data":{ "IP": "'$ip'", "Hostname": "'$hostnameVar'" , "Memoria":"'$memTotal'MB", "OS": "'$SOversion'" , "CPU" : "'$CPUtype'", "Tipo macchina" :  "'$machineType'", "Disco": "'$diskVar'" } }' $pathToServer
