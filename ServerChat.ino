#include <SPI.h>
#include <Ethernet.h>

#define DEBUG

const byte MAX_SIZE = 10;

struct nick{
  String nome;
  byte ip;
} nicks[MAX_SIZE];

// network configuration.  gateway and subnet are optional.

 // the media access control (ethernet hardware) address for the shield:
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };  
//the IP address for the shield:
byte ip[] = { 192, 168, 0, 104 };    
// the router's gateway address:
byte gateway[] = { 192, 168, 0, 1 };
// the subnet:
byte subnet[] = { 255, 255, 255, 0 };

char packetBuffer[256];
char replyBuffer[256];

EthernetServer sLogin = EthernetServer(6666);
EthernetUDP sMensagem;
EthernetUDP sOutMsg;

void setup()
{
  #ifdef DEBUG
    Serial.begin(9600);
  #endif
  
  // initialize the ethernet device
  Ethernet.begin(mac, ip, gateway, subnet);
  // start listening for clients
  sLogin.begin();
  sMensagem.begin(6668);
  sOutMsg.begin(6667);
  
  for (byte i = 0; i < MAX_SIZE; i++) {
    nicks[i].nome = NULL;
    nicks[i].ip = 0;
  }
}


void loop () 
{
  //Login:
  EthernetClient client = sLogin.available();
  if (client == true) {
    #ifdef DEBUG
      Serial.println("Iniciando processamento Login");
    #endif
    String nick = "";
    byte b;
    boolean split = false;
    boolean saindo = false;
    do {
      b = client.read();
      #ifdef DEBUG
        Serial.print((char) b);
      #endif
      if (split && b != '\n') {
        if (b == '\0') {
          saindo = true;
          break;
        }
        nick += (char) b;
      }
      if (b == ':')
        split = true;
    } while (b != -1 && b != '\0' && b != '\n');
    if (saindo) {
      byte ip__[4];
      for (byte i = 0; i < MAX_SIZE; i++) {
        if (nicks[i].ip == client.getRemoteIP(ip__)[3]) {
          #ifdef DEBUG
            Serial.println("Saindo cliente: " + nicks[i].nome);
          #endif
          nicks[i].nome = NULL;
          nicks[i].ip = 0;
        }
      }
    } else {
      #ifdef DEBUG
        Serial.println("Iniciando processamento nick: " + nick);
      #endif
      byte posIns = -1;
      for (byte i = 0; i < MAX_SIZE; i++) {
        if (nicks[i].nome == nick) {
          posIns = -1;
          break;
        }
        if (nicks[i].ip == 0) {
          posIns = i;
        }
      }
      if (posIns != -1) {
        nicks[posIns].nome = nick;
        byte ip__[4];
        nicks[posIns].ip = client.getRemoteIP(ip__)[3];
        byte msg[5] = {'#', '#', 'o', 'k', '\n'};
        #ifdef DEBUG
          Serial.print("Nick: " + nick + ", status: OK, IP: ");
          Serial.println(ip__[3]);
        #endif
        client.write(msg, 5);
      } else {
        byte msg[5] = {'#', '#', 'e', 'r', '\n'};
        #ifdef DEBUG
          Serial.println("Nick: " + nick + ", status: ERR");
        #endif
        client.write(msg, 5);
      }
    }
  }
  //Mensagens:
  int packetSize = sMensagem.parsePacket();
  if (packetSize != 0) {// No baldochi mode on
    #ifdef DEBUG
      Serial.println("Processando pacote UDP.");
    #endif
    IPAddress remote = sMensagem.remoteIP();
    sMensagem.read(packetBuffer, 256);
    String reply = "";
    for (byte i = 0; i < MAX_SIZE; i++) {
      if (nicks[i].ip == remote[3]) {
        reply += nicks[i].nome + ": ";
        break;
      }
    }
    #ifdef DEBUG
      Serial.print("Nick: " + reply);
    #endif
    byte posArray = 0;
    for (byte i = 0; i < reply.length(); i++) {
      replyBuffer[posArray++] = reply.charAt(i);
    }
    for (byte i = 0; posArray < 256 && i < packetSize; i++) {
      replyBuffer[posArray++] = packetBuffer[i];
      #ifdef DEBUG
        Serial.print((char) packetBuffer[i]);
      #endif
    }
    if (posArray == 256) {
      replyBuffer[255] = '\0';
    } else {
      replyBuffer[posArray++] = '\0';
    }
    #ifdef DEBUG
      Serial.println("\nMensagem sendo enviada");
    #endif
    // PORCOCAST:
    for (uint8_t i = 0; i < MAX_SIZE; i++) {
      if (nicks[i].ip != 0) {
        #ifdef DEBUG
          Serial.print("\nMensagem enviada para ");
          Serial.println(nicks[i].ip);
        #endif
        IPAddress ip(192, 168, 0, nicks[i].ip);
        sOutMsg.beginPacket(ip, 6667);
        sOutMsg.write(replyBuffer);
        sOutMsg.endPacket();
      }
    }
    #ifdef DEBUG
      Serial.println("Mensagem enviada para todos");
    #endif
  }
}
