import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.time.LocalDateTime;

/**
 * Processo cliente: localiza o objeto remoto via RMI Registry ("lookup")
 * e invoca métodos nele como se fosse um objeto local (transparência de
 * localização). Toda a comunicação de rede/serialização é feita pelo stub
 * gerado dinamicamente pelo RMI — o cliente não lida com sockets.
 *
 * Uso:
 *   java Client <clientId> <host> <numMensagens>
 * Exemplo:
 *   java Client cliente-A localhost 5
 */
public class Client {

    public static void main(String[] args) {
        String clientId = args.length > 0 ? args[0] : "cliente-anonimo";
        String host = args.length > 1 ? args[1] : "localhost";
        int numMessages = args.length > 2 ? Integer.parseInt(args[2]) : 5;

        try {
            Registry registry = LocateRegistry.getRegistry(host, Server.REGISTRY_PORT);
            RemoteMessageService service = (RemoteMessageService) registry.lookup(Server.SERVICE_NAME);

            System.out.println("[" + clientId + "] Conectado ao serviço remoto. Enviando " + numMessages + " mensagens...");

            for (int i = 1; i <= numMessages; i++) {
                try {
                    String msg = "Mensagem " + i + " de " + clientId;
                    System.out.println("[" + clientId + "][" + LocalDateTime.now() + "] Enviando: " + msg);

                    String response = service.sendMessage(clientId, msg);

                    System.out.println("[" + clientId + "][" + LocalDateTime.now() + "] Recebido: " + response);

                } catch (ConnectException ce) {
                    // Evidência do experimento de falha: servidor indisponível
                    System.err.println("[" + clientId + "] FALHA: servidor indisponível (ConnectException) - " + ce.getMessage());
                } catch (RemoteException re) {
                    System.err.println("[" + clientId + "] FALHA na chamada remota: " + re.getMessage());
                }

                Thread.sleep(1000);
            }

        } catch (NotBoundException e) {
            System.err.println("Serviço '" + Server.SERVICE_NAME + "' não encontrado no registry: " + e.getMessage());
        } catch (RemoteException e) {
            System.err.println("Não foi possível conectar ao RMI Registry em " + host + ":" + Server.REGISTRY_PORT
                    + " - o servidor está rodando? Detalhe: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
