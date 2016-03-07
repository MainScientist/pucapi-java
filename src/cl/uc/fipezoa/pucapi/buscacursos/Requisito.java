package cl.uc.fipezoa.pucapi.buscacursos;

import cl.uc.fipezoa.pucapi.Ramo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by fipezoa on 1/26/2016.
 */
public class Requisito implements Serializable {

    private String sigla;
    private String relacion;
    private boolean isCorrequisito;
    private ArrayList<Requisito> requisitos;
    private boolean isNull = false;

    protected Requisito(String s){
        if (s == null || s.equals("")){
            isNull = true;
        }else {
            String sStripped = s.charAt(0) == '(' ? s.substring(1, s.length() - 1) : s;
            relacion = encontrarRelacion(sStripped);
            if (relacion.equals("")) {
                isCorrequisito = sStripped.contains("(c)");
                sigla = isCorrequisito ? sStripped.substring(0, sStripped.indexOf("(c)")) : sStripped;
            } else {
                requisitos = new ArrayList<>();
                for (String requisito : sStripped.split(relacion)) {
                    requisitos.add(new Requisito(requisito));
                }
            }
        }
    }

    private static String encontrarRelacion(String s){
        int state = 0;
        int depth = 0;
        for (char c : s.toCharArray()){
            switch (state){
                case 0:
                    if (c == '('){
                        state = 1;
                        depth += 1;
                    }else{
                        state = 2;
                    }
                    break;
                case 1:
                    if (c == ')' && depth == 1){
                        state = 2;
                    }else if (c == '('){
                        depth += 1;
                    }else if (c == ')'){
                        depth -= 1;
                    }
                    break;
                case 2:
                    if (c == ' '){
                        state = 3;
                    }
                    break;
                case 3:
                    if (c == 'y' || c == 'o'){
                        return " " + c + " ";
                    }
                    break;
            }
        }
        return "";
    }

    public boolean cumpleRequisito(ArrayList<? extends Ramo> ramosCursados){
        return cumpleRequisito(ramosCursados, null);
    }

    public boolean cumpleRequisito(ArrayList<? extends Ramo> ramosCursados, ArrayList<? extends Ramo> ramosEnCurso){
        if (isNull) return true;

        if (requisitos == null){
            for (Ramo ramo : ramosCursados) {
                if (ramo.getSigla().equals(sigla)) {
                    return true;
                }
            }
            if (isCorrequisito){
                for (Ramo ramo : ramosEnCurso) {
                    if (ramo.getSigla().equals(sigla)) {
                        return true;
                    }
                }
            }
            return false;
        }else{
            if (relacion.equals(" o ")){
                for (Requisito requisito : requisitos){
                    if (requisito.cumpleRequisito(ramosCursados)){
                        return true;
                    }
                }
                return false;
            }else{
                for (Requisito requisito : requisitos){
                    if (!requisito.cumpleRequisito(ramosCursados)){
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public boolean isNull() {
        return isNull;
    }

    public String toString(){
        if (isNull){
            return "No tiene";
        }
        String r = "";
        if (sigla != null){
            r = isCorrequisito ? sigla + "(c)" : sigla;
        }else{
            r += "(";
            for (Requisito requisito : requisitos){
                r += requisito.toString() + relacion;
            }
            r = requisitos.size() > 1 ? r.substring(0, r.length()-3) : r;
            r += ")";
        }
        return r;
    }
}
