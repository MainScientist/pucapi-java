package cl.uc.fipezoa.pucapi.fichaacademica;

import cl.uc.fipezoa.pucapi.Ramo;

import java.io.Serializable;

/**
 * Created by fipezoa on 1/28/2016.
 */
public class RamoCursado extends Ramo{

    private String nota;
    private Semestre semestre;
    private String nombre;
    private int creditos;

    protected RamoCursado(String sigla, String nombre, String nota, Semestre semestre, int creditos) {
        super(sigla);
        this.nombre = nombre;
        this.nota = nota;
        this.semestre = semestre;
        this.creditos = creditos;
    }

    public String getNota() {
        return nota;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCreditos() {
        return creditos;
    }
}
