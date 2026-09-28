import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementação concreta do serviço remoto.
 * Estender UnicastRemoteObject faz com que cada instância seja exportada
 * automaticamente para aceitar chamadas remotas (RMI cuida do socket,
 * da serialização dos parâmetros e do encaminhamento da chamada).
 */
public class RemoteMessageServiceImpl extends UnicastRemoteObject implements RemoteMessageService {

    // Contador para gerar um identificador único por requisição (evidência nos logs)
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    protected RemoteMessageServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized String sendMessage(String clientId, String message) throws RemoteException {
        int requestId = requestCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();

        // Log de recebimento — evidência da comunicação via middleware
        System.out.printf("[%s] REQ#%d recebida de '%s' (thread=%s): \"%s\"%n",
                LocalDateTime.now(), requestId, clientId, threadName, message);

        // "Processamento" simples
        String response = String.format("[Servidor RMI] Echo(#%d): %s", requestId, message.toUpperCase());

        // Simula algum processamento (ajuda a observar concorrência entre chamadas)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf("[%s] REQ#%d respondida para '%s'%n", LocalDateTime.now(), requestId, clientId);
        return response;
    }

    @Override
    public String ping() throws RemoteException {
        return "pong (" + LocalDateTime.now() + ")";
    }
}
