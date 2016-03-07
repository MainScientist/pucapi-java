package cl.uc.fipezoa.pucapi.buscacursos;

import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.ArrayList;

/**
 * Created by fipezoa on 2/22/2016.
 */
public class Modulos extends ArrayList<Modulo> {

    public int count(String tipo){
        int count = 0;
        for (Modulo modulo : this){
            if (modulo.getTipo().equals(tipo)){
                count ++;
            }
        }
        return count;
    }

    public boolean topa(Modulo otroModulo){
        for (Modulo modulo : this){
            if (modulo.topa(otroModulo)){
                return true;
            }
        }
        return false;
    }
}
