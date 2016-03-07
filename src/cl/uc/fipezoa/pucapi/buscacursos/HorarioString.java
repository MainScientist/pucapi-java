package cl.uc.fipezoa.pucapi.buscacursos;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by fipezoa on 2/5/2016.
 */
public class HorarioString implements Serializable{

    public String dias;
    public String tipo;
    public ArrayList<String> salas = new ArrayList<>();
    public String sala;

    protected HorarioString(String dias, String tipo, String sala){
        this.dias = dias;
        this.tipo = tipo;
        if (!sala.equalsIgnoreCase("(por asignar)")){
            salas.add(sala);
        }
        this.sala = sala;
    }

    public void addSala(String sala){
        if (!salas.contains(sala)) {
            salas.add(sala);
            if (salas.size() == 1){
                this.sala = sala;
            }else {
                this.sala += ", " + sala;
            }
        }
    }
}
