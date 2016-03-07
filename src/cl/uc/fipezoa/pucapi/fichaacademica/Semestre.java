package cl.uc.fipezoa.pucapi.fichaacademica;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by fipezoa on 1/28/2016.
 */
public class Semestre implements Serializable{

    private String periodo;
    private ArrayList<RamoEnCurso> ramosEnCurso;
    private ArrayList<RamoCursado> ramosCursados;
    private FichaAcademica fichaAcademica;

    public Semestre(String periodo, FichaAcademica fichaAcademica){
        this.periodo = periodo;
        this.fichaAcademica = fichaAcademica;
        ramosCursados = new ArrayList<>();
        ramosEnCurso = new ArrayList<>();
    }

    public ArrayList<RamoEnCurso> getRamosEnCurso() {
        return ramosEnCurso;
    }

    public ArrayList<RamoCursado> getRamosCursados() {
        return ramosCursados;
    }

    public String getPeriodo() {
        return periodo;
    }

    public int creditosCursados(){
        int creditos = 0;
        for (RamoCursado ramoCursado : ramosCursados){
            creditos += ramoCursado.getCreditos();
        }
        return creditos;
    }

    public float getPromedio(){
        float nota = 0;
        int creditosCursados = creditosCursados();

        for (RamoCursado ramoCursado : ramosCursados){
            if (ramoCursado.getCreditos() > 0){
                nota += ((float)ramoCursado.getCreditos()/creditosCursados) * Float.valueOf(ramoCursado.getNota());
            }
        }
        return nota;
    }

    public int creditosCursadosAcumulados(){
        int creditos = 0;
        for (Semestre semestre : fichaAcademica.getSemestres()){
            for (RamoCursado ramoCursado : semestre.getRamosCursados()){
                creditos += ramoCursado.getCreditos();
            }
            if (semestre == this){
                break;
            }
        }
        return creditos;
    }

    public float getPromedioAcumulado(){
        float nota = 0;
        int creditosCursadosAcumulados = creditosCursadosAcumulados();

        for (Semestre semestre : fichaAcademica.getSemestres()){
            for (RamoCursado ramoCursado : semestre.getRamosCursados()){
                if (ramoCursado.getCreditos() > 0) {
                    nota += ((float) ramoCursado.getCreditos() / creditosCursadosAcumulados) * Float.valueOf(ramoCursado.getNota());
                }
            }
            if (semestre == this){
                break;
            }
        }
        return nota;
    }
}
