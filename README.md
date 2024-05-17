# DataBaton

## how to use

### requirement
jdk21 or later

### remote server(proxy server)
By using vm options or modifying the "application.yaml" file, make "databaton.mode=remote". 
After compiling and packaging, run the jar file on your remote server(proxy server)


### local server
By using vm options or modifying the "application.yaml" file, make "databaton.mode=local".
and set the value of 'databaton.remote-server.host' to the IP address of your remote server(proxy server).
After compiling and packaging, run the jar file on your computer



