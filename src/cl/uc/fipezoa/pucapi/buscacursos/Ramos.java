package cl.uc.fipezoa.pucapi.buscacursos;

import cl.uc.fipezoa.pucapi.Ramo;

import java.util.ArrayList;

/**
 * Created by fipezoa on 2/19/2016.
 */
public class Ramos<E extends RamoBuscaCursos> extends ArrayList<E>{

    public boolean topa(E otro){
        for (E element : this){
            if (element.topa(otro)){
                return true;
            }
        }
        return false;
    }

    public boolean mismoHorario(E otro){
        for (E element : this){
            if (element.mismoHorario(otro)){
                return true;
            }
        }
        return false;
    }

    public Ramos<E> get(String sigla){
        Ramos<E> ramos = new Ramos<>();
        for (E element : this){
            if (element.getSigla().equals(sigla)){
                ramos.add(element);
            }
        }
        return ramos;
    }

    public E get(String sigla, int seccion){
        for (E element : this){
            if (element.getSigla().equals(sigla) && element.getSeccion() == seccion){
                return element;
            }
        }
        return null;
    }


}
