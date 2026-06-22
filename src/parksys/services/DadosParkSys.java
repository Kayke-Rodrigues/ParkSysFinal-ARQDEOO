package parksys.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;

// S03 - Container que agrupa os três mapas para serialização em um único arquivo
public class DadosParkSys implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Vaga> vagas;
    private ArrayList<Registro> registros;
    private LinkedList<Mensalista> mensalistas;

    public DadosParkSys(HashMap<String, Vaga> vagas,
                        ArrayList<Registro> registros,
                        LinkedList<Mensalista> mensalistas) {
        this.vagas = vagas;
        this.registros = registros;
        this.mensalistas = mensalistas;
    }

    public HashMap<String, Vaga> getVagas() {
        return vagas;
    }

    public ArrayList<Registro> getRegistros() {
        return registros;
    }

    public LinkedList<Mensalista> getMensalistas() {
        return mensalistas;
    }
}
