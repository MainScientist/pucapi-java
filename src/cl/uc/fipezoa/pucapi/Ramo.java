package cl.uc.fipezoa.pucapi;

import java.io.Serializable;

/**
 * Created by fipezoa on 1/26/2016.
 */
public abstract class Ramo implements Serializable {

    protected String sigla;

    protected Ramo(String sigla){
        this.sigla = sigla;
    }

    public Ramo(){
        // for serialization purposes
    }

    public String getSigla() {
        return sigla;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ramo){
            Ramo ramo = (Ramo) obj;
            return ramo.sigla.equals(this.sigla);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return sigla.hashCode();
    }
}
