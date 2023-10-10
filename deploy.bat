cls
set server_directory="F:\apache-tomcat-10.0.22\"

xcopy "bin\" "build\WEB-INF\classes" /Y /E /I
jar -cvf session.war -C build .
copy session.war %server_directory%\webapps