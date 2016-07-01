package cl.uc.fipezoa.pucapi.fichaacademica;

import cl.uc.fipezoa.pucapi.AlumnoUC;
import cl.uc.fipezoa.pucapi.callbacks.LoadingCallback;
import cl.uc.fipezoa.pucapi.callbacks.Progress;
import cl.uc.fipezoa.pucapi.exceptions.LoginException;
import cl.uc.fipezoa.pucapi.exceptions.ServerUnavailable;
import cl.uc.fipezoa.requests.Response;
import cl.uc.fipezoa.requests.UrlParameters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by fipezoa on 1/28/2016.
 */
public class FichaAcademica implements Serializable {

    private AlumnoUC owner;
    private ArrayList<Semestre> semestres = new ArrayList<>();
    private boolean successfulyLoaded = false;

    public FichaAcademica(AlumnoUC alumnoUC, LoadingCallback callback){

        this.owner = alumnoUC;
        try {
            callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando ficha academica..."));
            cargarDatos(callback);
        } catch (IOException e) {
            callback.onProgressChange(new Progress(0, Progress.ERROR, "Error cargando ficha academica"));
            e.printStackTrace();
        }

    }

    public ArrayList<RamoCursado> ramosAprobados(){
        ArrayList<RamoCursado> ramosAprobados = new ArrayList<>();
        for (Semestre semestre : semestres){
            for (RamoCursado ramo : semestre.getRamosCursados()){
                if (ramo.getNota().equals("A") || (ramo.getCreditos() != 0 && Float.valueOf(ramo.getNota()) >= 4f)){
                    ramosAprobados.add(ramo);
                }
            }
        }
        return ramosAprobados;
    }

    public ArrayList<Semestre> getSemestres(){
        return semestres;
    }

    public AlumnoUC getOwner(){
        return owner;
    }

    public boolean cargarDatos(LoadingCallback callback) throws IOException {
        for (int intent = 0; intent < 6; intent++) {
            try {
                owner.ssbLogin();
                UrlParameters data = new UrlParameters();
                data.addParameter("levl", "");
                data.addParameter("tprt", "FAA");

                Response response = owner.getSession().post("https://ssb.uc.cl/ERPUC/bwskotrn.P_ViewTran", data);
                Document fichaAcademicaDoc = Jsoup.parse(response.getContent().toString());
                if (!fichaAcademicaDoc.title().contains("Ficha Acad")){
                    throw new LoginException();
                }
                Elements trs = fichaAcademicaDoc.getElementsByTag("tr");
                Semestre semestre = null;
                for (Element tr : trs) {
                    Elements ths = tr.getElementsByTag("th");
                    if (ths.size() > 0) {
                        Element th = ths.first();
                        // TODO: ddtitle colspan 12 CREDITOS TRANSFERIDOS
                        if (th.hasAttr("class") && th.hasAttr("colspan") && th.attr("class").contains("ddlabel")) {
                            if (th.attr("colspan").equals("12") || th.attr("colspan").equals("11")) {
                                if (semestre != null && !semestres.contains(semestre)) {
                                    semestres.add(semestre);
                                }
                                int firstSpace = th.text().indexOf(" ");
                                int secondSpace = th.text().indexOf(" ", firstSpace + 1);
                                int thirdSpace = th.text().indexOf(" ", secondSpace + 1);
                                String year = th.text().substring(firstSpace + 1, secondSpace);
                                String periodo;
                                if (th.text().contains("TAV")) {
                                    periodo = year + "-3";
                                }else {
                                    String sem = th.text().substring(secondSpace + 1, thirdSpace);
                                    periodo = sem.equals("Primer") ? year + "-1" : year + "-2";
                                }
                                semestre = buscarSemestre(periodo);
                                if (semestre == null) {
                                    semestre = new Semestre(periodo, this);
                                }
                            }
                        }
                    }
                    Elements tds = tr.select("td.dddefault");
                    if (tds.size() > 2 && semestre != null) {
                        String sigla = tds.get(0).text() + tds.get(1).text();
                        String nombre = tds.get(3).text();
                        if (tds.size() == 8) {
                            String nota = tds.get(4).text().replace(" ", "");
                            String notaString = "";
                            for (char c : nota.toCharArray()){
                                if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6'
                                        || c == '7' || c == '8' || c == '9' || c == '.' || c == ','){
                                    notaString += c;
                                }else{
                                    break;
                                }
                            }
                            int creditos = Float.valueOf(tds.get(5).text()).intValue();
                            String notDef = notaString.length() > 0 ? notaString : nota;
                            RamoCursado ramo = new RamoCursado(sigla, nombre, notDef, semestre, creditos);
                            semestre.getRamosCursados().add(ramo);
                        } else if (tds.size() == 5) {
                            int creditos = Float.valueOf(tds.get(4).text()).intValue();
                            RamoEnCurso ramoEnCurso = new RamoEnCurso(sigla, nombre, semestre, creditos);
                            semestre.getRamosEnCurso().add(ramoEnCurso);
                        }
                    }
                }
                if (semestre != null) {
                    semestres.add(semestre);
                }
                successfulyLoaded = true;
                return true;
            } catch (ServerUnavailable | LoginException serverUnavailable) {
                serverUnavailable.printStackTrace();
                callback.onProgressChange(new Progress(0, Progress.ERROR, "Recargando ficha academica..."));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        callback.onProgressChange(new Progress(0, Progress.ERROR, "Error cargando ficha academica..."));
        return false;
    }

    public Semestre buscarSemestre(String periodo){
        for (Semestre s : semestres){
            if (s.getPeriodo().equals(periodo)){
                return s;
            }
        }
        return null;
    }

    public boolean isSuccessfulyLoaded() {
        return successfulyLoaded;
    }
}
