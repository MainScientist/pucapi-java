package cl.uc.fipezoa.pucapi.buscacursos;

import java.io.Serializable;

/**
 * Created by fipezoa on 2/15/2016.
 */
public class Vacante implements Serializable {


    public String escuela;
    public String nivel;
    public String programa;
    public String campoDeEstudio;
    public String cohorte;
    public String prdoAdmision;
    public Integer ofrecidas;
    public Integer ocupadas;
    public Integer disponibles;


    public Vacante(String escuela, String nivel, String programa, String campoDeEstudio, String cohorte, String prdoAdmision, Integer ofrecidas, Integer ocupadas, Integer disponibles) {
        this.escuela = escuela;
        this.nivel = nivel;
        this.programa = programa;
        this.campoDeEstudio = campoDeEstudio;
        this.cohorte = cohorte;
        this.prdoAdmision = prdoAdmision;
        this.ofrecidas = ofrecidas;
        this.ocupadas = ocupadas;
        this.disponibles = disponibles;
    }
}
