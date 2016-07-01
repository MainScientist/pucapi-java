package cl.uc.fipezoa.pucapi.buscacursos;

import cl.uc.fipezoa.pucapi.callbacks.LoadingCallback;
import cl.uc.fipezoa.pucapi.callbacks.Progress;
import cl.uc.fipezoa.requests.Requests;
import cl.uc.fipezoa.requests.Response;
import cl.uc.fipezoa.requests.UrlParameters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fipezoa on 1/26/2016.
 */
public class BuscaCursos {

    static int TABLE_INDEX = 5;
    
    public static Ramos<RamoBuscaCursos> buscarCursos(FiltroBuscaCursos filtroBuscaCursos, LoadingCallback callback, boolean loadEverything) throws IOException {
        Ramos<RamoBuscaCursos> results = new Ramos<>();

        UrlParameters urlParameters = filtroBuscaCursos.toUrlParameters();
        Response response = Requests.get("http://buscacursos.uc.cl/?" + urlParameters.toString(), urlParameters);
        Document buscaCursos = Jsoup.parse(response.getContent().toString());

        Elements tables = buscaCursos.select("table");

        if (tables.size() <= 5) return results;
        Element table = buscaCursos.select("table").get(TABLE_INDEX);
        Elements table_rows = table.select("tbody > tr");

        boolean unaSeccion = true;
        int times = 0;
        for (Element tr : table_rows){
            if (tr.className().equals("resultadosRowImpar") || tr.className().equals("resultadosRowPar")) {
                if (times > 0){
                    unaSeccion = false;
                    break;
                }
                times++;
            }
        }

        HashMap<String, Integer> headers = getHeaders(table_rows.get(1));

        String unidadAcademica = "";
        for (Element row : table_rows){
            if (row.attributes().size() == 0){
                unidadAcademica = row.text().trim();
            }else{
                if (row.className().equals("resultadosRowPar") || row.className().equals("resultadosRowImpar")){
                    Elements data = row.select(":root > td");

                    String nombre = data.get(headers.get("Nombre")).text().trim();
                    String sigla = data.get(headers.get("Sigla")).text().trim();
                    String campus = data.get(headers.get("Campus")).text().trim();

                    int nrc = Integer.valueOf(data.get(headers.get("NRC")).text().trim());
                    int creditos = Integer.valueOf(data.get(headers.get("Cred.")).text().trim());
                    int seccion = Integer.valueOf(data.get(headers.get("Sec.")).text().trim());
                    int vacantesDisponibles = Integer.valueOf(data.get(headers.get("Horario") - 1).text().trim());
                    int vacantesTotales = Integer.valueOf(data.get(headers.get("Horario")).text().trim());

                    boolean permiteRetiro = data.get(headers.get("Permite Retiro")).text().trim().equals("SI");
                    boolean dictadoEnIngles = data.get(headers.get("Se dicta en ingles")).text().trim().equals("SI");
                    boolean requiereAprobEspecial = data.get(headers.get("Requiere Aprob. Especial")).text().trim()
                            .equals("SI");

                    String[] profesores = data.get(headers.get("Profesor")).text().trim().split(",");

                    RamoBuscaCursos ramo = new RamoBuscaCursos(filtroBuscaCursos.getSemestre(), sigla, nrc, creditos,
                        seccion, nombre, campus, unidadAcademica, profesores, dictadoEnIngles, permiteRetiro,
                        requiereAprobEspecial, vacantesTotales, vacantesDisponibles, unaSeccion);

                    if (loadEverything){
                        ramo.cargarPrograma(callback);
                        ramo.cargarRequisito(callback);
                        ramo.cargarVacantesReservadas();
                    }

                    Elements module_rows = data.get(headers.get("Horario")+2).select("table > tbody > tr");
                    for (Element module_row : module_rows){
                        Elements row_data = module_row.select(":root > td");
                        String[] splitted = row_data.get(0).text().trim().split(":");
                        String days = splitted[0];
                        String modules = splitted[1];
                        String type = row_data.get(1).text().trim();
                        String classroom = row_data.get(2).text().trim();
                        for (String day : days.split("-")){
                            for (String module : modules.split(",")){
                                ramo.getModulos().add(new Modulo(day, Integer.valueOf(module), classroom, type, ramo));
                            }
                        }
                        HorarioString horarioString = new HorarioString(row_data.get(0).text().trim(), type, classroom);
                        ramo.getHorarioStrings().add(horarioString);
                    }

                    results.add(ramo);
                }
            }
        }
        return results;
    }

    public static Ramos<RamoBuscaCursos> buscarCursos(FiltroBuscaCursos filtroBuscaCursos, boolean loadEverything) throws IOException {
        return buscarCursos(filtroBuscaCursos, new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing...
            }
        }, loadEverything);
    }

    public static RamoBuscaCursos buscarCurso(FiltroBuscaCursos filtroBuscaCursos, int seccion, LoadingCallback callback, boolean loadEverything) throws IOException {
        ArrayList<RamoBuscaCursos> ramos = buscarCursos(filtroBuscaCursos, callback, loadEverything);
        for (RamoBuscaCursos ramoBuscaCursos : ramos){
            if (ramoBuscaCursos.getSeccion() == seccion){
                return ramoBuscaCursos;
            }
        }
        if (ramos.size() > 0) {
            return ramos.get(0);
        }else{
            return null;
        }
    }

    public static RamoBuscaCursos buscarCurso(FiltroBuscaCursos filtroBuscaCursos, int seccion, boolean loadEverything) throws IOException {
        return buscarCurso(filtroBuscaCursos, seccion, new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing...
            }
        }, loadEverything);
    }

    public static RamoBuscaCursos buscarCurso(FiltroBuscaCursos filtroBuscaCursos, LoadingCallback callback, boolean loadEverything) throws IOException {
        return buscarCurso(filtroBuscaCursos, 1, callback, loadEverything);
    }

    public static RamoBuscaCursos buscarCurso(FiltroBuscaCursos filtroBuscaCursos, boolean loadEverythin) throws IOException {
        return buscarCurso(filtroBuscaCursos, new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing...
            }
        }, loadEverythin);
    }

    private static HashMap<String, Integer> getHeaders(Element row){
        HashMap<String, Integer> headers = new HashMap<>();
        Elements elements = row.select(":root > td");
        for (int i = 0; i < elements.size(); i++){
            String s = elements.get(i).text().trim();
            s = Normalizer.normalize( s, Normalizer.Form.NFD );
            s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            s = s.replaceAll("[¿?]", "");
            headers.put(s, i);
        }
        return headers;
    }
}
