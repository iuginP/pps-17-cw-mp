# pps-17-cw-mp
Elaborato di progetto per l'esame di PPS (Mirko Viroli) A.A.2017-18

## Esecuzione
Per eseguire il progetto è sufficiente avviare i servizi ed i client:
* Discovery service: `./gradlew :services:discovery:run --args '7777'`
* Authentication service: `./gradlew :services:authentication:run --args 'localhost 7777 localhost 7788'`
* Rooms service: `./gradlew :services:rooms:run --args 'localhost 7777 localhost 7799'`
* Client: `./gradlew :client:run --args 'localhost 7777'`

## Downloads e rilasci
È possibile scaricare i sorgenti e gli eseguibili di ogni rilascio alla pagina: https://github.com/iuginP/pps-17-cw-mp/releases

## Documentazione e coverage reports
Documentazione e coverage reports sono disponibili alla pagina: https://iuginp.github.io/pps-17-cw-mp/

## Team members
Eugenio Pierfederici(eugenio.pierfederici@studio.unibo.it) *Team leader*  
Enrico Siboni (enrico.siboni3@studio.unibo.it)  
Davide Borficchia (davide.borficchia@studio.unibo.it)  
Elia Di Pasquale (elia.dipasquale@studio.unibo.it)  
