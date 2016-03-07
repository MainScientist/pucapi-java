package cl.uc.fipezoa.pucapi.buscacursos;

import cl.uc.fipezoa.pucapi.AlumnoUC;
import cl.uc.fipezoa.pucapi.Ramo;
import cl.uc.fipezoa.pucapi.callbacks.LoadingCallback;
import cl.uc.fipezoa.pucapi.callbacks.Progress;
import cl.uc.fipezoa.requests.Requests;
import cl.uc.fipezoa.requests.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fipezoa on 1/26/2016.
 */
public class RamoBuscaCursos extends Ramo implements Serializable {

    private static Map<String, String> cacheProgramas = new HashMap<>();
    private static Map<String, Requisito> cacheRequisitos = new HashMap<>();

    protected Modulos modulos = new Modulos();
    protected ArrayList<HorarioString> horarioStrings = new ArrayList<>();

    protected boolean permiteRetiro;
    protected boolean dictadoEnIngles;
    protected boolean requiereAprobEspecial;
    protected boolean requisitoCargado = false;
    protected boolean programaCargado = false;
    protected boolean vacantesCargadas = false;
    protected boolean tieneUnaSeccion;

    protected int nrc;
    protected int creditos;
    protected int seccion;
    protected int vacantesDisponibles;
    protected int vacantesTotales;

    // TODO: horario

    protected Requisito requisito;

    protected String nombre;
    protected String programa;
    protected String periodo;
    protected String campus;
    protected String unidadAcademica;

    protected String[] profesores;
    protected ArrayList<Vacante> vacantesReservadas;


    protected RamoBuscaCursos(String periodo, String sigla, int nrc, int creditos, int seccion, String nombre, String campus,
                              String unidadAcademica, String[] profesores, boolean dictadoEnIngles, boolean permiteRetiro, boolean requiereAprobEspecial,
                              int vacantesTotales, int vacantesDisponibles, boolean unaSeccion) {
        super(sigla);
        this.periodo = periodo;
        this.nrc = nrc;
        this.creditos = creditos;
        this.seccion = seccion;
        this.nombre = nombre;
        this.campus = campus;
        this.unidadAcademica = unidadAcademica;
        this.profesores = profesores;
        this.permiteRetiro = permiteRetiro;
        this.dictadoEnIngles = dictadoEnIngles;
        this.requiereAprobEspecial = requiereAprobEspecial;
        this.vacantesTotales = vacantesTotales;
        this.vacantesDisponibles = vacantesDisponibles;
        this.tieneUnaSeccion = unaSeccion;
    }

    protected RamoBuscaCursos(String sigla, int seccion){
        this.sigla = sigla;
        this.seccion = seccion;
    }

    public boolean topa(RamoBuscaCursos otro){
        for (Modulo modulo : modulos){
            if (otro.getModulos().topa(modulo)){
                return true;
            }
        }
        return false;
    }

    public boolean alumnoTieneVacantes(AlumnoUC alumnoUC){
        if (tieneVacantesReservadas()){
            for (Vacante vacante : alumnoUC.vacantesCorrespondientes(vacantesReservadas)){
                if (vacante.disponibles > 0){
                    return true;
                }
            }
            return false;
        }else{
            return vacantesDisponibles > 0;
        }
    }

//    public void foo(){
//
//    }

    public int getVacantesReservadasDisponibles(AlumnoUC alumnoUC){
        int count = 0;
        for (Vacante vacante : alumnoUC.vacantesCorrespondientes(vacantesReservadas)){
            count += vacante.disponibles;
        }
        return count;
    }

    public boolean cumpleRequisito(AlumnoUC alumno) {
        return !tieneRequisitos() || requisito.cumpleRequisito(alumno.getFichaAcademica().ramosAprobados(), alumno.getRamosEnCurso());
    }

    public boolean dictadoEnIngles(){
        return dictadoEnIngles;
    }

    public boolean permiteRetiro(){
        return permiteRetiro;
    }

    public boolean requiereAprobEspecial(){
        return requiereAprobEspecial;
    }

    public int getNrc() {
        return nrc;
    }

    public int getCreditos() {
        return creditos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrograma() {
        return programa;
    }

    public String getCampus() {
        return campus;
    }

    public String getUnidadAcademica() {
        return unidadAcademica;
    }

    public String[] getProfesores() {
        return profesores;
    }

    public int getSeccion() {
        return seccion;
    }

    public Requisito getRequisito() {
        return requisito;
    }

    public boolean tieneRequisitos(){
        return requisito != null;
    }

    public Modulos getModulos() {
        return modulos;
    }

    public boolean tieneUnaSeccion(){
        return tieneUnaSeccion;
    }

    public boolean requisitoCargado() {
        return requisitoCargado;
    }

    public boolean programaCargado() {
        return programaCargado;
    }

    public boolean vacantesCargadas() {
        return vacantesCargadas;
    }

    public String getPeriodo() {
        return periodo;
    }

    public boolean completamenteCargado(){
        return (programaCargado && vacantesCargadas && requisitoCargado);
    }

    public ArrayList<HorarioString> getHorarioStrings(){
        return horarioStrings;
    }

    public ArrayList<Vacante> getVacantesReservadas() {
        return vacantesReservadas;
    }

    public boolean tieneVacantesReservadas(){ return vacantesReservadas.size() > 0;}

    public int getVacantesDisponibles() {
        return vacantesDisponibles;
    }

    public int getVacantesTotales() {
        return vacantesTotales;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RamoBuscaCursos){
            RamoBuscaCursos ramo = (RamoBuscaCursos) obj;
            return ramo.sigla.equals(this.sigla) && ramo.seccion == this.seccion;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (sigla + String.valueOf(this.seccion)).hashCode();
    }

    public void cargarPrograma(LoadingCallback callback) throws IOException {
        if (cacheProgramas.containsKey(sigla)){
            programaCargado = true;
            this.programa = cacheProgramas.get(sigla);
        }else{
            String url = "http://catalogo.uc.cl/index.php?tmpl=component&option=com_catalogo&view=programa&sigla=" +
                    sigla;
            Response response = Requests.get(url);
            Document programDocument = Jsoup.parse(response.getContent().toString());
            Elements bloque =  programDocument.getElementsByClass("bloque");
            if (bloque.size() >0) {
                String programa = bloque.text();
                cacheProgramas.put(sigla, programa);
                this.programa = programa;
                programaCargado = true;
            }
        }
    }

    public void cargarPrograma() throws IOException {
        cargarPrograma(new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing
            }
        });
    }

    public void cargarRequisito(LoadingCallback callback) throws IOException {
        if (cacheRequisitos.containsKey(sigla)){
            this.requisito = cacheRequisitos.get(sigla);
            requisitoCargado = true;
        }else {
            String url = "http://catalogo.uc.cl/index.php?tmpl=component&option=com_catalogo&view=requisitos&sigla=" +
                    sigla;
            Response response = Requests.get(url);
            Document requisitosDocument = Jsoup.parse(response.getContent().toString());
            Elements elements = requisitosDocument.getElementsByTag("tr");
            if (elements.size() > 0) {
                Element element = elements.get(0).getElementsByTag("span").get(0);
                Requisito requisito = element.text().contains("No tiene") ? new Requisito(null) :
                        new Requisito(element.text());
                cacheRequisitos.put(sigla, requisito);
                this.requisito = requisito;
                requisitoCargado = true;
            }else{
                this.requisito = null;
            }

        }
    }

    public void cargarRequisito() throws IOException {
        cargarRequisito(new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing
            }
        });
    }

    public void cargarVacantesReservadas(){
        String url = "http://buscacursos.uc.cl/informacionVacReserva.ajax.php?nrc=" + String.valueOf(nrc) + "&termcode=" + periodo;
        ArrayList<Vacante> vacantes = new ArrayList<>();
        try {
            Response response = Requests.get(url);
            String div = response.getContent().toString();
            Document page = Jsoup.parse(div);
            Elements trs = page.getElementsByTag("tr");
            for (Element tr : trs){
                if ((tr.attr("class").equals("resultadosRowImpar") || tr.attr("class").equals("resultadosRowPar"))) {
                    Elements tds = tr.getElementsByTag("td");
                    if (tds.size() == 9) {
                        vacantes.add(new Vacante(tds.get(0).text(), tds.get(1).text(), tds.get(2).text(), tds.get(3).text(),
                                tds.get(4).text(), tds.get(5).text(), Integer.valueOf(tds.get(6).text()),
                                Integer.valueOf(tds.get(7).text()), Integer.valueOf(tds.get(8).text())));
                    }
                }
            }
            this.vacantesReservadas = vacantes;
            vacantesCargadas = true;
        } catch (IOException e) {
            this.vacantesReservadas = null;
        }
    }

    public boolean mismoHorario(RamoBuscaCursos otro){
        for (Modulo modulo : modulos){
            boolean found = false;
            for (Modulo otroModulo : otro.modulos){
                if (modulo.getDia().equals(otroModulo.getDia()) && modulo.getNumero() == otroModulo.getNumero() && modulo.getTipo().equals(otroModulo.getTipo())){
                    found = true;
                }
            }
            if (!found){
                return false;
            }
        }
        return true;
    }
}
