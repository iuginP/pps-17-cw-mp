# pps-17-cw-mp
Elaborato di progetto per l'esame di PPS (Mirko Viroli) A.A.2017-18

## Esecuzione

### Requisiti
Per eseguire il progetto è necessario che le macchine che eseguono i servizi di authentication e rooms abbiano installato un server mysql con un **utente** `pps-17-cwmp:pps-17-cwmp` che abbia i permessi per lo **schema** `pps-17-cwmp`.

È inoltre necessario che la macchina in cui si mette in esecuzione il servizio di autenticazione abbia la libreria [bouncycastle](https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on) tra le librerie di sistema (nella cartella `JAVA_HOME/jre/lib/ext`).

### Avvio
Per eseguire il progetto è sufficiente avviare i servizi ed i client (in questo caso si considera che tutti i servizi sono avviati sulla stessa macchina):
* Discovery service
  * Gradle: `./gradlew :services:discovery:run --args '7777'`
  * JAR: è sufficiente eseguire il jar, in alternativa: `java -jar cwmp-VERSIONE-discovery.jar 7777` (porta sulla quale eseguire il servizio)
* Authentication service
  * Gradle: `./gradlew :services:authentication:run --args 'localhost 7777 localhost 7788'`
  * JAR: è sufficiente eseguire il jar, in alternativa: `java -jar cwmp-VERSIONE-authentication.jar localhost 7777 localhost 7788` (host e porta del discovery service, host e porta per raggiungere il servizio appena avviato)
* Rooms service
  * Gradle: `./gradlew :services:rooms:run --args 'localhost 7777 localhost 7799'`
  * JAR: è sufficiente eseguire il jar, in alternativa: `java -jar cwmp-VERSIONE-rooms.jar localhost 7777 localhost 7789`  (host e porta del discovery service, host e porta per raggiungere il servizio appena avviato)
* Client
  * Gradle: `./gradlew :client:run --args 'localhost 7777 localhost'`
  * JAR: è sufficiente eseguire il jar, in alternativa: `java -jar cwmp-VERSIONE-client.jar localhost 7777 localhost`  (host e porta del discovery service, host per raggiungere il servizio appena avviato)

## Downloads e rilasci
È possibile scaricare i sorgenti e gli eseguibili di ogni rilascio alla pagina: https://github.com/iuginP/pps-17-cw-mp/releases

## Documentazione e coverage reports
Documentazione e coverage reports sono disponibili alla pagina: https://iuginp.github.io/pps-17-cw-mp/

## Team members
Eugenio Pierfederici(eugenio.pierfederici@studio.unibo.it) *Team leader*  
Enrico Siboni (enrico.siboni3@studio.unibo.it)  
Davide Borficchia (davide.borficchia@studio.unibo.it)  
Elia Di Pasquale (elia.dipasquale@studio.unibo.it)  
