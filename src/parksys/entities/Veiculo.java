package parksys.entities;

import java.io.Serializable;

import parksys.enums.TipoVeiculo;

// S01 - Serializable com serialVersionUID garante compatibilidade entre sessões
public class Veiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String placa;
    private TipoVeiculo tipo;

    public Veiculo(String placa, TipoVeiculo tipo) {
        this.placa = placa;
        this.tipo = tipo;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setTipo(TipoVeiculo tipo) {
        this.tipo = tipo;
    }
}
