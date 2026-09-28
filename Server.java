import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Processo servidor: cria (ou usa) o RMI Registry e registra o objeto remoto
 * sob um nome público. É o "serviço de nomes" (name service) do middleware —
 * abstrai a descoberta/localização do objeto remoto para o cliente.
 */
public class Server {

    public static final String SERVICE_NAME = "MessageService";
    public static final int REGISTRY_PORT = 1099;

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "10.85.166.99");
            // Cria o registry RMI nesta JVM, na porta padrão 1099
            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);

            RemoteMessageService service = new RemoteMessageServiceImpl();

            // "bind" publica o objeto remoto sob um nome — o cliente fará "lookup" por esse nome
            registry.rebind(SERVICE_NAME, service);

            System.out.println("=========================================");
            System.out.println(" Servidor RMI iniciado com sucesso");
            System.out.println(" Serviço registrado como: " + SERVICE_NAME);
            System.out.println(" Porta do registry: " + REGISTRY_PORT);
            System.out.println(" Aguardando chamadas remotas...");
            System.out.println("=========================================");

        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
