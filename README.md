# BlackBoxMC

<div align="center">
  <img src="https://i.imgur.com/o2LhJRF.png" alt="Logo di BlackBoxMC" width="200">
  <br>
  <h3>Proteggi il tuo network e i tuoi server con BlackBoxMC!</h3>
</div>

BlackBoxMC è un plugin di protezione per server Minecraft, sviluppato da **StrayVibes**. Il suo scopo principale è quello di filtrare gli utenti che utilizzano cheat o sono stati precedentemente coinvolti in attività malevole su altri server. Utilizzando una blacklist globale, BlackBoxMC aiuta a garantire un ambiente di gioco sicuro e leale per tutti i giocatori e per il tuo server.

### Caratteristiche principali

* **Protezione Avanzata**: Utilizza una blacklist globale per prevenire l'accesso di giocatori malintenzionati.
* **Facile da usare**: L'installazione e la configurazione sono semplici e veloci.
* **Completamente Open Source**: Il codice è disponibile per la comunità.
* **Piattaforme multiple**: Supporto completo per le piattaforme Spigot, BungeeCord e Velocity.

### Piattaforme Supportate

* **Spigot**: `v1.0b1rc` e versioni successive.
* **BungeeCord**: `v1.0b1rc` e versioni successive.
* **Velocity**: `v1.0b1rc` e versioni successive.

### Installazione

L'installazione è un processo rapido e indolore. Segui questi semplici passaggi:

1.  Scarica l'ultima versione del plugin per la tua piattaforma da [questo link](https://blackboxmc.it/plugin.html).
2.  Trascina il file `.jar` scaricato nella cartella `plugins` del tuo server.
3.  Riavvia il server per generare il file di configurazione.
4.  Apri il file `config.yml` che si trova nella cartella `plugins/BlackBoxMC` e inserisci i dati richiesti.

### Configurazione

Dopo il primo avvio del server, verrà creato un file `config.yml`. È **obbligatorio** modificare questo file per far funzionare correttamente il plugin.

```yaml
#
#  ░████████   ░██                       ░██       ░████████                         ░███     ░███   ░██████
#  ░██    ░██  ░██                       ░██       ░██    ░██                        ░████   ░████  ░██   ░██
#  ░██    ░██  ░██  ░██████    ░███████  ░██    ░██░██    ░██   ░███████  ░██    ░██ ░██░██ ░██░██ ░██
#  ░████████   ░██       ░██  ░██    ░██ ░██   ░██ ░████████   ░██    ░██  ░██  ░██  ░██ ░████ ░██ ░██
#  ░██     ░██ ░██  ░███████  ░██        ░███████  ░██     ░██ ░██    ░██   ░█████   ░██  ░██  ░██ ░██
#  ░██     ░██ ░██ ░██   ░██  ░██    ░██ ░██   ░██ ░██     ░██ ░██    ░██  ░██  ░██  ░██       ░██  ░██   ░██
#  ░█████████  ░██  ░█████░██  ░███████  ░██    ░██░█████████   ░███████  ░██    ░██ ░██       ░██   ░██████
#
#  Configurazione BlackBoxMC versione v1.0b1rc per Spigot,Bungeecord,Velocity
#  Sviluppato da StrayVibes per BlackBoxMC https://blackboxmc.it
---
api-key: "INSERISCI_LA_TUA_CHIAVE_API_QUI"
server-name: "Nome Del Tuo Server"
founder-name: "Nome Del Fondatore"
server-icon-url: "https://i.imgur.com/o2LhJRF.png"
```
