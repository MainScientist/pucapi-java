package cl.uc.fipezoa.pucapi.buscacursos;

import java.io.Serializable;

/**
 * Created by fipezoa on 1/29/2016.
 */
public class Modulo implements Serializable {

    private String dia;
    private int numero;
    private String sala;
    private String tipo;
    private RamoBuscaCursos ramo;

    public Modulo(String dia, int numero, String sala, String tipo, RamoBuscaCursos ramo){
        this.dia = dia;
        this.numero = numero;
        this.sala = sala;
        this.tipo = tipo;
        this.ramo = ramo;
    }

    public boolean topa(Modulo otro){
        if (otro.getDia().equals(dia)){
            if (otro.getNumero() == numero){
                if (otro.getTipo().equals("LAB") || otro.getTipo().equals("CLAS")){
                    if (tipo.equals("LAB") || tipo.equals("CLAS")){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void setRamo(RamoBuscaCursos ramo){
        this.ramo = ramo;
    }

    public String getDia() {
        return dia;
    }

    public int getNumero() {
        return numero;
    }

    public String getSala() {
        return sala;
    }

    public String getTipo() {
        return tipo;
    }

    public RamoBuscaCursos getRamo() {
        return ramo;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Modulo){
            Modulo modulo = (Modulo)obj;
            return (modulo.getRamo().equals(ramo) && modulo.getDia().equals(dia) && modulo.getNumero() == numero);
        }else{
            return false;
        }
    }
}
