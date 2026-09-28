import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota do middleware RMI.
 * Todo método deve declarar throws RemoteException, pois a chamada
 * pode falhar por questões de rede, indisponibilidade do servidor, etc.
 * Essa é a "abstração" que o cliente enxerga — ele não sabe (nem precisa
 * saber) que a implementação está rodando em outra JVM/máquina.
 */
public interface RemoteMessageService extends Remote {

    /**
     * Envia uma mensagem ao servidor remoto e recebe uma resposta processada.
     * @param clientId identificador do cliente que está enviando (para logs)
     * @param message conteúdo da mensagem
     * @return resposta processada pelo servidor
     */
    String sendMessage(String clientId, String message) throws RemoteException;

    /**
     * Método simples para "ping" — usado para checar se o serviço está vivo.
     */
    String ping() throws RemoteException;
}
