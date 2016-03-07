package cl.uc.fipezoa.pucapi;

import cl.uc.fipezoa.pucapi.callbacks.LoadingCallback;
import cl.uc.fipezoa.pucapi.exceptions.LoginException;
import cl.uc.fipezoa.requests.Requests;
import cl.uc.fipezoa.requests.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by fipezoa on 1/26/2016.
 */
public class PUC {


    private static String semestreActual;

    public static String getSemestreActual(){
        try {
            if (semestreActual == null) {
                Response response = Requests.get("http://buscacursos.uc.cl");
                Document document = Jsoup.parse(response.getContent().toString());
                Element semestre = document.getElementsByTag("option").first();
                String year = semestre.text().substring(0, semestre.text().indexOf(' '));
                String numero = semestre.text().contains("Primer") ? "1" : "2";
                semestreActual = year + "-" + numero;
                return semestreActual;
            }else return semestreActual;
        } catch (IOException e){
            String semestre = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            semestre += "-" + (Calendar.getInstance().get(Calendar.MONTH) >= 8 ? "2" : "1");
            return semestre;
        }
    }

    public static AlumnoUC login(String username, String password) throws IOException, LoginException {
        return new AlumnoUC(username, password);
    }

    public static AlumnoUC login(String username, String password, LoadingCallback callback) throws IOException, LoginException {
        return new AlumnoUC(username, password, callback);
    }
}
