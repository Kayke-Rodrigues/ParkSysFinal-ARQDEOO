# ParkSys — Sistema de Gestão de Estacionamento

## Sobre o Projeto

O ParkSys é um sistema para gerenciamento de estacionamento com interface gráfica Swing. O sistema é capaz de:

- Registrar entrada e saída de veículos;
- Gerenciar 30 vagas distribuídas em duas fileiras;
- Alocar automaticamente vagas consecutivas para SUV 2 vagas, Caminhão 3 vagas;
- Manter cadastro de mensalistas com vagas reservadas;
- Calcular tarifas automaticamente por tipo de veículo;
- Emitir relatórios com ordenação cronológica e por receita;
- Persistir dados entre sessões via serialização Java;
- Processar entradas simultâneas com segurança usando multithreading;

**Grupo:** Eduardo Rafael, Kayke Rodrigues e Leonardo Martins
**Instituto:** IFSP — Campus Araraquara

---

## Tecnologias

- **Java 17** compatível com 11+
- **IDE:** VSCode
- **Interface gráfica:** Java Swing
- **Persistência:** Java Serialization (ObjectOutputStream / ObjectInputStream)
- **Controle de versão:** Git com Conventional Commits

---

## Estrutura de Pacotes

```
src/
└── parksys/
    ├── main/
    │   └── Principal.java              # Ponto de entrada da aplicação
    ├── entities/
    │   ├── Vaga.java                   # Entidade de vaga (Serializable)
    │   ├── Veiculo.java                # Entidade de veículo (Serializable)
    │   ├── Registro.java               # Registro de entrada/saída (Serializable, Comparable)
    │   └── Mensalista.java             # Mensalista com vaga reservada (Serializable)
    ├── enums/
    │   ├── TipoVeiculo.java            # MOTO, CARRO, SUV, CAMINHAO com tarifa e vagas
    │   └── StatusVaga.java             # LIVRE, OCUPADA, RESERVADA com disponibilidade
    ├── exceptions/
    │   ├── VagaOcupadaException.java
    │   ├── VeiculoNaoEncontradoException.java
    │   └── PlacaInvalidaException.java
    ├── services/
    │   ├── GerenciadorEstacionamento.java  # Singleton, lógica de negócio, Collections
    │   ├── GerenciadorArquivo.java         # Serialização, desserialização e exportação .txt
    │   ├── DadosParkSys.java               # Container para serialização dos três mapas/listas
    │   ├── EntradaRunnable.java            # Runnable para processamento assíncrono de entradas
    │   └── MonitorRunnable.java            # Thread daemon que monitora vagas a cada 1 segundo
    ├── observer/
    │   ├── EstacionamentoObserver.java     # Interface do padrão Observer
    │   └── PainelMonitor.java              # Implementação do Observer para monitoramento
    └── ui/
        ├── TelaInicial.java                # Tela principal com menu
        ├── TelaRegistroEntrada.java        # Formulário de entrada de veículos
        ├── TelaSaida.java                  # Formulário de saída de veículos
        ├── TelaCadastroMensalista.java     # Cadastro de mensalistas
        └── TelaRelatorio.java              # Relatórios e exportação .txt
```

---

## Como Executar

**Pré-requisitos:** JDK 11 ou superior instalado.

```bash
# 1. Clone o repositório
git clone https://github.com/Kayke-Rodrigues/ParkSysFinal-ARQDEOO.git
cd ParkSys-ARQDEOO

# 2. Compile (a partir da raiz do projeto)
cd src
javac -encoding UTF-8 -d ../bin $(find . -name "*.java")

# 3. Execute
cd ../bin
java parksys.main.Principal
```

---

## Conceitos Aplicados

### Enums com dados de negócio
`TipoVeiculo` armazena nome legível, tarifa por hora e número de vagas ocupadas diretamente no enum. Nenhum valor fixo existe no código:
```java
double tarifa = TipoVeiculo.SUV.getTarifaHora();     // 18.0
int vagas = TipoVeiculo.CAMINHAO.getVagasOcupadas(); // 3
```

### Collections
Cada estrutura foi escolhida com justificativa técnica:
- `HashMap<String, Vaga>` — acesso O(1) por ID de vaga
- `ArrayList<Registro>` — mantém ordem de chegada e permite acesso por índice
- `LinkedList<Mensalista>` — inserção/remoção nas pontas em O(1)
- `TreeSet<Registro>` — ordena cronologicamente usando `Comparable<Registro>`

### Serialização
Todas as entidades implementam `Serializable` com `serialVersionUID = 1L`. O campo `threadOrigem` em `Registro` é `transient` — não é serializado e fica `null` após restaurar do disco. `DadosParkSys` agrupa os três mapas/listas em um único arquivo `.ser`. Todos os blocos de I/O usam `try-catch-finally`.

### Multithreading
`EntradaRunnable` implementa `Runnable` e simula processamento com `Thread.sleep(200)`. Todos os métodos que acessam `HashMap` e `ArrayList` são `synchronized` para evitar race conditions. `MonitorRunnable` é uma thread daemon que imprime o status das vagas a cada 1 segundo. O `Principal` cria 4 threads de entrada e usa `join()` para aguardar todas.

### Padrões de Projeto
- **Singleton** — `GerenciadorEstacionamento` garante uma única instância via `getInstance()`
- **Observer** — `EstacionamentoObserver` desacopla o gerenciador das telas. `PainelMonitor` recebe notificações via `onVagaAlterada(idVaga, novoStatus)`
- **MVC** — pacote `ui` não contém lógica de negócio; toda operação passa pelo `GerenciadorEstacionamento`

---

## Branches

| Branch | O que foi implementado |
|---|---|
| `main` | Setup inicial, `.gitignore`, estrutura de pacotes |
| `feature/enums` | `TipoVeiculo` e `StatusVaga` com dados de negócio (T01–T05) |
| `feature/entities` | `Vaga`, `Veiculo`, `Registro`, `Mensalista` com `Serializable` (S01) + Exceptions |
| `feature/services` | `GerenciadorEstacionamento` com Collections e lógica de negócio (C01–C06) |
| `feature/serializacao` | `GerenciadorArquivo`, `DadosParkSys`, try-catch-finally (S02–S06) |
| `feature/threads` | `EntradaRunnable`, `MonitorRunnable`, `synchronized` (M01–M07) |
| `feature/patterns` | Singleton, Observer, MVC, `PainelMonitor` (P01–P06) |
| `feature/ui` | Todas as telas Swing e integração final |

---

## Autor(es)

| Nome | Turma |
|---|---|
| Eduardo Rafael | ARQDEOO — IFSP Araraquara |
| Kayke Rodrigues | ARQDEOO — IFSP Araraquara |
| Leonardo Martins | ARQDEOO — IFSP Araraquara |
