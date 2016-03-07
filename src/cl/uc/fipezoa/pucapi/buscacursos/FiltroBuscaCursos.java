package cl.uc.fipezoa.pucapi.buscacursos;

import cl.uc.fipezoa.pucapi.PUC;
import cl.uc.fipezoa.requests.Requests;
import cl.uc.fipezoa.requests.Response;
import cl.uc.fipezoa.requests.UrlParameters;
import com.sun.istack.internal.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by fipezoa on 1/26/2016.
 */
public class FiltroBuscaCursos {

    private String sigla;
    private String campus;
    private String nombreCurso;
    private String profesor;
    private String[] horario;
    private String unidadAcademica;
    private String semestre;
    private String tipoHorario;
    public static ArrayList<String> periodosDisponibles = new ArrayList<>();
    public static ArrayList<String> campusDisponibles = new ArrayList<>();
    public static ArrayList<String> unidadesAcademicasDisponibles = new ArrayList<>();
    public static ArrayList<String> tiposHorarioDisponibles = new ArrayList<>();
    public static boolean dataLoaded = false;

    public static String TODOS = "TODOS";
    public static String TALLER = "TALL";
    public static String CATEDRA = "CAT";
    public static String LABORATORIO = "LAB";
    public static String TESIS = "TES";
    public static String PRACTICA = "PRAC";
    public static String TERRENO = "TERR";

    public static String CAMPUS_EXTERNO = "Campus Externo";
    public static String CASA_CENTRAL = "Casa Central";
    public static String SAN_JOAQUIN = "San Joaquin";
    public static String LO_CONTADOR = "Lo Contador";
    public static String ORIENTE = "Oriente";
    public static String VILLARICA = "Villarica";


    public static String ACTUACION = "Teatro";
    public static String AGRONOMIA_E_ING_FORESTAL = "Agronomia E Ing. Forestal";
    public static String ARQUITECTURA = "Arquitectura";
    public static String ARTE = "Arte";


//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";
//    public static String  = "AYUD";


    static public boolean loadFormData(){
        try {
            Response response = Requests.get("http://buscacursos.uc.cl");
            Document document = Jsoup.parse(response.getContent().toString());
            Element form = document.getElementsByTag("form").first();
            if (form != null) {
                tiposHorarioDisponibles = getOptionsFromForm(form, "cxml_horario_tipo_busqueda_actividad");
                unidadesAcademicasDisponibles = getOptionsFromForm(form, "cxml_unidad_academica");
                campusDisponibles = getOptionsFromForm(form, "cxml_campus");
                periodosDisponibles = getOptionsFromForm("cxml_semestre");
                dataLoaded = true;
            }else{
                System.out.println(document);
                dataLoaded = false;
            }
            return dataLoaded;
        }catch (IOException e){
            return false;
        }
    }

    static private ArrayList<String> getOptionsFromForm(String option){
        ArrayList<String> optionsArray = new ArrayList<>();
        try {
            Response response = Requests.get("http://buscacursos.uc.cl");
            Document document = Jsoup.parse(response.getContent().toString());
            Element form = document.getElementsByTag("form").first();
            Element select = form.getElementsByAttributeValue("name", option).first();
            Elements options = select.getElementsByTag("option");
            for (Element element : options){
                optionsArray.add(element.attr("value"));
            }
            return optionsArray;
        } catch (IOException e) {
            e.printStackTrace();
            optionsArray.add(PUC.getSemestreActual());
            return optionsArray;
        }
    }

    static private ArrayList<String> getOptionsFromForm(Element form, String option){
        ArrayList<String> optionsArray = new ArrayList<>();
        Element select = form.getElementsByAttributeValue("name", option).first();
        Elements options = select.getElementsByTag("option");
        for (Element element : options){
            optionsArray.add(element.attr("value"));
        }
        return optionsArray;
    }

    public FiltroBuscaCursos(@NotNull String semestre){
        this.semestre = semestre;
    }

    public FiltroBuscaCursos(){
        this(PUC.getSemestreActual());
    }


    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }


    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String[] getHorario() {
        return horario;
    }

    public void setHorario(String[] horario) {
        this.horario = horario;
    }

    public String getUnidadAcademica() {
        return unidadAcademica;
    }

    public void setUnidadAcademica(String unidadAcademica) {
        this.unidadAcademica = unidadAcademica;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    protected UrlParameters toUrlParameters(){
        UrlParameters urlParameters = new UrlParameters();
        urlParameters.addParameter("cxml_semestre", semestre);
        urlParameters.addParameter("cxml_horario_tipo_busqueda", "si_tenga");
        urlParameters.addParameter("cxml_horario_tipo_busqueda_actividad", "TODOS");
        if (tipoHorario != null){
            urlParameters.addParameter("cxml_horario_tipo_busqueda_actividad", tipoHorario);
        }
        if (sigla != null){
            urlParameters.addParameter("cxml_sigla", sigla);
        }
        if (campus != null){
            urlParameters.addParameter("cxml_campus", campus);
        }
        if (nombreCurso != null){
            urlParameters.addParameter("cxml_nombre", nombreCurso);
        }
        if (profesor != null){
            urlParameters.addParameter("cxml_sigla", sigla);
        }
        if (horario != null){
            for (String modulo : horario){
                urlParameters.addParameter("cxml_modulo_" + modulo.toUpperCase(), modulo.toUpperCase());
            }
        }
        if (unidadAcademica != null){
            urlParameters.addParameter("cxml_unidad_academica", unidadAcademica);
        }
        return urlParameters;
    }

    public String getTipoHorario() {
        return tipoHorario;
    }

    public void setTipoHorario(String tipoHorario) {
        this.tipoHorario = tipoHorario;
    }
}
