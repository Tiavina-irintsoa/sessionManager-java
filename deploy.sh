#!/bin/bash
clear
server_directory="/home/tiavina/tita/apache-tomcat-10.0.22"
source_directory="bin/"
destination_directory="build/WEB-INF/classes"
war_file="session.war"

# Copie du répertoire source vers le répertoire de destination
rsync -av "$source_directory" "$destination_directory"

# Création du fichier WAR en utilisant le répertoire de build
jar -cvf "$war_file" -C build .

# Copie du fichier WAR vers le répertoire du serveur Tomcat
cp "$war_file" "$server_directory/webapps/"
