all: compile-client compile-tcp-client

../Server/RMIInterface.jar: ../Server/Server/Interface/IResourceManager.java
	make -C ../Server/ RMIInterface.jar

compile-client: ../Server/RMIInterface.jar
	javac -cp ../Server/RMIInterface.jar Client/RMIClient.java Client/Client.java Client/Command.java

compile-tcp-client: ../Server/RMIInterface.jar
	make -C ../Server/ compile-server-rmi
	javac -cp "../Server/RMIInterface.jar:../Server" Client/TCPClient.java Client/TCPResourceManagerProxy.java Client/Client.java Client/Command.java

clean:
	rm -f Client/*.class
	rm -f ../Server/RMIInterface.jar
