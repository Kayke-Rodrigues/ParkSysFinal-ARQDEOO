package parksys.enums;

public enum TipoVeiculo {
    MOTO("Motocicleta", 5.0, 1),
    CARRO("Automóvel", 10.0, 1),
    SUV("Caminhonete/SUV", 18.0, 2),
    CAMINHAO("Caminhão", 30.0, 3);

    private final String nome;
    private final double tarifaHora;
    private final int vagasOcupadas;

    TipoVeiculo(String nome, double tarifaHora, int vagasOcupadas) {
        this.nome = nome;
        this.tarifaHora = tarifaHora;
        this.vagasOcupadas = vagasOcupadas;
    }

    public String getNome() {
        return nome;
    }

    public double getTarifaHora() {
        return tarifaHora;
    }

    public int getVagasOcupadas() {
        return vagasOcupadas;
    }
}
