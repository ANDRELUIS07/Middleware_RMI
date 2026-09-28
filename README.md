# Atividade Prática — Middleware Java RMI

## 1. Identificação e justificativa da plataforma

- **Plataforma:** Java RMI (Remote Method Invocation)
- **Categoria:** Objetos distribuídos / RPC
- **Modelo de comunicação:** invocação remota síncrona (o cliente chama um método como se o objeto fosse local)
- **Principais abstrações oferecidas:**
  - *Stub/Skeleton*: proxies gerados automaticamente que escondem toda a comunicação de rede e serialização
  - *RMI Registry*: serviço de nomes para localizar (`lookup`) o objeto remoto publicado (`bind`)
  - *Serialização automática* dos parâmetros e retornos (objetos Java trafegam pela rede sem código manual)
  - *Garbage collection distribuído* de referências remotas
- **Motivo da escolha:** não exige broker ou serviço externo (roda só com o JDK), integra nativamente com Java, e é o exemplo clássico de middleware de objetos distribuídos / RPC estudado na disciplina.
- **Cenários reais de uso:** sistemas corporativos legados em Java puro, aplicações internas homogêneas (mesma linguagem/plataforma) que precisam de chamadas remotas simples sem o overhead de uma fila de mensagens.

## 2. Arquitetura da solução

```
Cliente A --\
             \
              >--- RMI Registry (porta 1099) ---> Servidor (RemoteMessageServiceImpl)
             /
Cliente B --/
```

- `RemoteMessageService.java` — interface remota (contrato) que estende `Remote`
- `RemoteMessageServiceImpl.java` — implementação do objeto remoto (estende `UnicastRemoteObject`)
- `Server.java` — cria o RMI Registry e publica (`rebind`) o objeto remoto
- `Client.java` — localiza o objeto (`lookup`) e invoca `sendMessage`/`ping`

## 3. Como compilar

```bash
cd src
javac *.java
```

## 4. Como executar (2 processos independentes)

**Terminal 1 — Servidor:**
```bash
cd src
java Server
```
Deixe rodando. Você verá os logs de inicialização e, depois, de cada requisição recebida.

**Terminal 2 — Cliente:**
```bash
cd src
java Client cliente-A localhost 5
```
Parâmetros: `<clientId> <host> <numeroDeMensagens>`

Você verá, no terminal do cliente, cada mensagem enviada e a resposta recebida; no terminal do servidor, o log de cada requisição processada (com id sequencial, thread e timestamp) — essa é a evidência da comunicação exigida na Etapa 2/3.

## 5. Etapa 5 — Concorrência / múltiplas instâncias

Abra **mais terminais** e rode vários clientes ao mesmo tempo, todos contra o mesmo servidor:

```bash
# Terminal 2
java Client cliente-A localhost 8

# Terminal 3
java Client cliente-B localhost 8

# Terminal 4
java Client cliente-C localhost 8
```

Observe no log do servidor:
- cada requisição é atendida por uma **thread RMI diferente** (RMI cria uma thread por chamada) — é possível ver isso no nome da thread logado (`RMI TCP Connection(...)`).
- como o método `sendMessage` está `synchronized`, as chamadas de clientes diferentes são **serializadas** no acesso ao contador/log (bom ponto para discutir na questão 7 do documento — como a plataforma lida com concorrência).
- diferente de uma fila (RabbitMQ/Kafka), aqui **não há balanceamento entre múltiplas instâncias de servidor** — RMI é ponto a ponto: cada cliente fala diretamente com o objeto remoto publicado. Se vocês quiserem demonstrar "múltiplas instâncias do lado servidor", teriam que registrar dois objetos remotos sob nomes diferentes (não há um mecanismo nativo de load balancing no RMI puro).

## 6. Etapa 6 — Experimento de falha

1. Com o servidor rodando e um cliente enviando várias mensagens (ex: `java Client cliente-A localhost 20`, que manda uma mensagem por segundo),
2. **No meio da execução**, vá ao terminal do servidor e mate o processo (`Ctrl+C`).
3. Observe o terminal do cliente: a próxima chamada vai lançar uma exceção de rede.

O código já trata isso (`ConnectException` / `RemoteException`) e imprime uma mensagem de log clara, por exemplo:
```
[cliente-A] FALHA: servidor indisponível (ConnectException) - Connection refused
```

Pontos para a análise (Etapa 7 / questões 8, 10, 12):
- RMI **não tem fila nem persistência** — a mensagem que estava "em trânsito" durante a queda é **perdida**, não é reenviada nem armazenada (diferente de um MOM como RabbitMQ/Kafka).
- Não há reconexão automática nem retry — é responsabilidade da aplicação cliente tratar a exceção e decidir o que fazer (tentar de novo, desistir, etc.). O middleware só informa que a chamada falhou.
- Se vocês reiniciarem o servidor (`java Server` de novo), ele volta a aceitar chamadas normalmente, mas o cliente só volta a funcionar se fizer um novo `lookup` (o stub antigo pode ficar "morto" dependendo do cenário) — vale testar e registrar o que acontece no ambiente de vocês.
- Isso contrasta com o padrão pub/sub ou fila, onde geralmente há reentrega, ACK e persistência — ótimo gancho para comparar com outra categoria de middleware nas questões 11 e 14.

## 7. Roteiro de evidências para a entrega

- [ ] Print/log do servidor iniciando (Etapa 2)
- [ ] Print/log do cliente conectando e trocando mensagens com IDs de requisição (Etapa 3/4)
- [ ] Print/log de múltiplos clientes concorrentes (Etapa 5)
- [ ] Print/log do experimento de falha: servidor derrubado + erro no cliente + servidor reiniciado (Etapa 6)
- [ ] Respostas às 14 questões do documento (usar os pontos discutidos acima como base)
- [ ] Linha da tabela de síntese comparativa:

| Plataforma | Categoria | Modelo | Broker? | Persistência | Escalabilidade | Tolerância a falhas |
|---|---|---|---|---|---|---|
| Java RMI | Objetos distribuídos / RPC | Síncrono (invocação remota) | Não (registry ponto a ponto) | Não | Baixa (sem balanceamento nativo) | Baixa (sem retry/persistência automática) |
