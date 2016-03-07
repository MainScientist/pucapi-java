package cl.uc.fipezoa.pucapi.fichaacademica;

import cl.uc.fipezoa.pucapi.Ramo;

import java.io.Serializable;

/**
 * Created by fipezoa on 1/29/2016.
 */
public class RamoEnCurso extends Ramo{

    private Semestre semestre;
    private String nombre;
    private int creditos;

    protected RamoEnCurso(String sigla, String nombre, Semestre semestre, int creditos) {
        super(sigla);
        this.nombre = nombre;
        this.semestre = semestre;
        this.creditos = creditos;
    }

    public RamoEnCurso(){
        // Empty for serialization
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
