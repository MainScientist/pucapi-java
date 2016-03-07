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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fipezoa on 1/26/2016.
 */
public class BuscaCursos {
    
    public static Ramos<RamoBuscaCursos> buscarCursos(FiltroBuscaCursos filtroBuscaCursos, LoadingCallback callback, boolean loadEverything) throws IOException {
        UrlParameters urlParameters = filtroBuscaCursos.toUrlParameters();
        Response response = Requests.get("http://buscacursos.uc.cl/?" + urlParameters.toString(), urlParameters);
        Document buscaCursos = Jsoup.parse(response.getContent().toString());

        // Academic units
        Elements academicUnits = buscaCursos.getElementsByAttributeValue("style", "text-align:center; " +
                "font-weight:bold; font-size:16px; color:#FFFFFF; background:#1730A6; padding:2px; margin:2px");
        Map<Integer, String> academicUnitsMap = new HashMap<>();
        ArrayList<Integer> keys = new ArrayList<>();
        for (Element academicUnit : academicUnits){
            int index = buscaCursos.toString().indexOf(academicUnit.toString());
            keys.add(index);
            academicUnitsMap.put(index, academicUnit.text());
        }

        Ramos<RamoBuscaCursos> results = new Ramos<>();

        Elements trs = buscaCursos.getElementsByTag("tr");
        boolean unaSeccion = true;
        int times = 0;
        for (Element tr : trs){
            if (tr.className().equals("resultadosRowImpar") || tr.className().equals("resultadosRowPar")) {
                if (times > 0){
                    unaSeccion = false;
                    break;
                }
                times++;
            }
        }
        for (Element tr : trs){
            if (tr.className().equals("resultadosRowImpar") || tr.className().equals("resultadosRowPar")){
                Elements tds = tr.getElementsByTag("td");
                int index = buscaCursos.toString().indexOf(tds.get(0).toString());

                // Find academic unit
                String unidadAcademica = "";
                for (int i = 0; i < keys.size() - 1; i++){
                    int key = keys.get(i);
                    int nextKey = keys.get(i+1);
                    if (key < index && index < nextKey){
                        unidadAcademica = academicUnitsMap.get(key);
                    }
                }
                if (unidadAcademica.equals("")){
                    unidadAcademica = academicUnitsMap.get(keys.get(keys.size()-1));
                }

                int nrc = Integer.valueOf(tds.get(0).text());
                int creditos = Integer.valueOf(tds.get(9).text());
                int vacantesTotales = Integer.valueOf(tds.get(10).text());
                int vacantesDisponibles = Integer.valueOf(tds.get(11).text());
                int seccion = Integer.valueOf(tds.get(4).text());
                String sigla = tds.get(1).text().replace(" ", "");
                String nombre = tds.get(6).text();
                String campus = tds.get(8).text();
                String[] profesores = tds.get(7).text().split(",");
                boolean permiteRetiro = tds.get(2).text().equalsIgnoreCase("si");
                boolean dictadoEnIngles = tds.get(3).text().equalsIgnoreCase("si");
                boolean requiereAprobEspecial = tds.get(5).text().equalsIgnoreCase("si");


                RamoBuscaCursos ramo = new RamoBuscaCursos(filtroBuscaCursos.getSemestre(), sigla, nrc, creditos,
                        seccion, nombre, campus, unidadAcademica, profesores, dictadoEnIngles, permiteRetiro,
                        requiereAprobEspecial, vacantesTotales, vacantesDisponibles, unaSeccion);

                if (loadEverything){
                    ramo.cargarPrograma(callback);
                    ramo.cargarRequisito(callback);
                    ramo.cargarVacantesReservadas();
                }

                if (tds.size() > 15){
                    int i = 14;
                    while( i <= tds.size() - 3){
                        String dias = tds.get(i).text().split(":")[0];
                        String modulos = tds.get(i).text().split(":")[1];
                        String tipo = tds.get(i + 1).text();
                        String sala = tds.get(i + 2).text();
                        for (String dia : dias.split("-")){
                            for (String modulo : modulos.split(",")){
                                ramo.getModulos().add(new Modulo(dia, Integer.valueOf(modulo), sala, tipo, ramo));
                            }
                        }
                        HorarioString horarioString = new HorarioString(tds.get(i).text().trim(), tipo, sala);
                        ramo.getHorarioStrings().add(horarioString);
                        i += 3;
                    }
                }
                results.add(ramo);
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
}
