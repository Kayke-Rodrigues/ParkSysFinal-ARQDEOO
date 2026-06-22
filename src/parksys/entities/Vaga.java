package parksys.entities;

import java.io.Serializable;

import parksys.enums.StatusVaga;

// S01 - Serializable com serialVersionUID garante compatibilidade entre sessões
public class Vaga implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private StatusVaga status;

    public Vaga(String id) {
        this.id = id;
        this.status = StatusVaga.LIVRE;
    }

    public String getId() {
        return id;
    }

    public StatusVaga getStatus() {
        return status;
    }

    public void setStatus(StatusVaga status) {
        this.status = status;
    }
}
