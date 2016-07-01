package cl.uc.fipezoa.pucapi;

import cl.uc.fipezoa.pucapi.buscacursos.*;
import cl.uc.fipezoa.pucapi.callbacks.LoadingCallback;
import cl.uc.fipezoa.pucapi.callbacks.Progress;
import cl.uc.fipezoa.pucapi.exceptions.LoginException;
import cl.uc.fipezoa.pucapi.exceptions.ServerUnavailable;
import cl.uc.fipezoa.pucapi.exceptions.SessionExpired;
import cl.uc.fipezoa.pucapi.fichaacademica.FichaAcademica;
import cl.uc.fipezoa.pucapi.fichaacademica.RamoCursado;
import cl.uc.fipezoa.pucapi.fichaacademica.Semestre;
import cl.uc.fipezoa.requests.Response;
import cl.uc.fipezoa.requests.Session;
import cl.uc.fipezoa.requests.UrlParameters;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fipezoa on 1/25/2016.
 */
public class AlumnoUC implements Serializable{

    private Ramos<RamoAlumno> ramosEnCurso = new Ramos<>();

    private byte[] fotoPortal;

    transient private Store store;

    private String username;
    private String password;
    private String nombre;
    private String genero;
    private String pais;
    private String rut;
    private String carrera;
    private String numeroDeAlumno;

    private String escuela;
    private String nivel;
    private String programa;
    private String prdoAdmision;
    private String segundaClave;
    private Date fechaDeNacimiento;
    private int generacion;
    private String situacion;
    private FichaAcademica fichaAcademica;

    private transient Session session = new Session();

    protected AlumnoUC(String username, String passord, LoadingCallback callback) throws IOException, LoginException {
        this.username = username;
        this.password = passord;
        callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando datos..."));
        cargarInfoPortal();
        try {
            callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando info personal..."));
            cargarInfoPersonal();
            callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando informacion academica..."));
            cargarInfoAcademica(callback);
        } catch (SessionExpired e){
            // HIGHLY UNLIKELY DUE TO RECENT LOGIN
        }
        fichaAcademica = new FichaAcademica(this, callback);
        callback.onProgressChange(new Progress(0, Progress.NORMAL, "Datos cargados..."));
    }

    protected AlumnoUC(String username, String password) throws IOException, LoginException {
        this(username, password, new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing
            }
        });
    }

    public AlumnoUC(){
        // Empty for serialization
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        session = new Session();
    }

    public boolean portalSessionExpired() throws IOException {
        Document portalUC = cargarPortal();
        return portalUC.getElementsByClass("greeting").size() == 0;
    }

    private void cargarInfoPortal() throws IOException, LoginException {
            Document homePortalUC = portalLogin();
            String nombreUpperCase = homePortalUC.getElementsByClass("greeting").get(0).text();
            nombre = "";
            String[] componentesNombre = nombreUpperCase.split(" ");
            for (String s : componentesNombre) {
                nombre += s.substring(0, 1);
                nombre += s.substring(1).toLowerCase() +
                        (componentesNombre[componentesNombre.length - 1] == s ? "" : " ");
            }
    }

    public Document portalLogin() throws IOException, LoginException {
        Document portalUC = cargarPortal();
        if (portalUC.getElementsByClass("greeting").size() > 0){
            return portalUC;
        }
        Document portalDocument = ssoLogin(portalUC);
        if (portalDocument.getElementsByClass("greeting").size() == 0) {
            throw new LoginException();
        }
        return portalDocument;
    }

    private Document cargarPortal() throws IOException {
        Response response = session.get("https://portal.uc.cl");
        String portalUC = response.getContent().toString();
        return Jsoup.parse(portalUC);

    }

    private void cargarInfoPersonal() throws IOException, SessionExpired {
        Document paginaInfoPersonal = cargarHtmlInfoPersonal();
        if (paginaInfoPersonal.toString().contains("Session Expired")){
            throw new SessionExpired();
        }

        Elements scripts = paginaInfoPersonal.getElementsByTag("script");

        // Datos Personales

        Document portletDatosPersonales = cargarPortletDatosPersonales(scripts);
        Element table = portletDatosPersonales.getElementsByTag("table").get(0);
        for (Element element : table.getElementsByTag("tr")){
            String key = element.getElementsByTag("th").text();
            String value = element.getElementsByTag("td").text();

            switch (key){
                case "Fecha de Nacimiento":
                    DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        fechaDeNacimiento = format.parse(value);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "Sexo": genero = value; break;
                case "RUT": rut = value; break;
                case "País de Origen": pais = value; break;
            }

        }

        // Persona UC

        Document portletPersonaUC = cargarPortletPersonaUC(scripts);
        Elements datos = portletPersonaUC.getElementsByClass("IP-alumno-td");
        numeroDeAlumno = datos.get(0).text();
        carrera = datos.get(1).text().split(" - ")[1];
        generacion = Integer.valueOf(datos.get(2).text());
        situacion = datos.get(4).text();

        // foto

        Response fotoResponse = session.get("https://portal.uc.cl/LPT022_DatosPersonales/Foto");
        fotoPortal = fotoResponse.getContent().toByteArray();

    }

    private Document cargarPortletDatosPersonales(Elements scripts) throws IOException {
        if (scripts.size() >= 34){
            Element dataElement = scripts.get(34);
            int urlBegin = dataElement.toString().indexOf("url:");
            int firstQuote = dataElement.toString().indexOf('"', urlBegin);
            int secondQuote = dataElement.toString().indexOf('"', firstQuote + 1);
            String url = dataElement.toString().substring(firstQuote + 1, secondQuote);

            UrlParameters urlParameters = new UrlParameters();
            urlParameters.addParameter("currentUrl", "/web/home-community/datos-personales?gpi=10225");
            urlParameters.addParameter("p_l_id", "10230");
            urlParameters.addParameter("p_p_id", "DatosPersonales_WAR_LPT022_DatosPersonales");
            urlParameters.addParameter("p_p_lifecycle", "0");
            urlParameters.addParameter("p_p_state", "normal");
            urlParameters.addParameter("p_p_mode", "view");
            urlParameters.addParameter("p_p_col_id", "column-1");
            urlParameters.addParameter("p_p_col_pos", "0");
            urlParameters.addParameter("p_p_col_count", "1");
            Response response = session.post("https://portal.uc.cl" + url, urlParameters);
            return Jsoup.parse(response.getContent().toString());
        }else {
            throw new IOException();
        }
    }

    private Document cargarPortletPersonaUC(Elements scripts) throws IOException{
        if (scripts.size() >= 34){
            Element dataElement = scripts.get(35);
            int urlBegin = dataElement.toString().indexOf("url:");
            int firstQuote = dataElement.toString().indexOf('"', urlBegin);
            int secondQuote = dataElement.toString().indexOf('"', firstQuote + 1);
            String url = dataElement.toString().substring(firstQuote + 1, secondQuote);

            UrlParameters urlParameters = new UrlParameters();
            urlParameters.addParameter("currentUrl", "/web/home-community/datos-personales?gpi=10225");
            urlParameters.addParameter("p_l_id", "10230");
            urlParameters.addParameter("p_p_id", "DatosPersonales_WAR_LPT022_DatosPersonales");
            urlParameters.addParameter("p_p_lifecycle", "0");
            urlParameters.addParameter("p_p_state", "normal");
            urlParameters.addParameter("p_p_mode", "view");
            urlParameters.addParameter("p_p_col_id", "column-2");
            urlParameters.addParameter("p_p_col_pos", "0");
            urlParameters.addParameter("p_p_col_count", "2");
            Response response = session.post("https://portal.uc.cl" + url, urlParameters);
            return Jsoup.parse(response.getContent().toString());
        }else {
            throw new IOException();
        }
    }

    private Document cargarHtmlInfoPersonal() throws IOException {
        Response response = session.get("https://portal.uc.cl/web/home-community/datos-personales?gpi=10225");
        return Jsoup.parse(response.getContent().toString());

    }

    private void cargarInfoAcademica(LoadingCallback callback) throws IOException, SessionExpired {
        UrlParameters parameters = new UrlParameters();
        parameters.addParameter("MODE", "DEFAULT");
        parameters.addParameter("VIEW", "DEFAULT");
        parameters.addParameter("RESET_CACHE", "DEFAULT");
        parameters.addParameter("selected_term", String.valueOf(Calendar.getInstance().get(Calendar.YEAR))+"20");
        parameters.addParameter("term", String.valueOf(Calendar.getInstance().get(Calendar.YEAR))+"20");
        parameters.addParameter("GO_BUTTON", "Ir");

        Response response = session.post("https://portal.uc.cl/web/home-community/informacion-academica?p_p_id=" +
                "AcademicProfile_WAR_luminisbanner&p_p_lifecycle=1&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1" +
                "&p_p_col_count=8", parameters);

        Document paginaInfoAcademica = Jsoup.parse(response.getContent().toString());



        if (paginaInfoAcademica.toString().contains("Session Expired")){
            throw new SessionExpired();
        }

        Element portletPerfilAcademico = paginaInfoAcademica.getElementById("app_portletParentContainer");
        Element tbody = portletPerfilAcademico.getElementsByTag("tbody").first();

        Elements trs = tbody.getElementsByTag("tr");
        for (Element tr : trs){
            Elements tds = tr.getElementsByTag("td");
            if (tds.size() == 2){
                switch (tds.get(0).text()){
//                    case "Carr:":
//                        System.out.println("Carrera: " + tds.get(1).text());
//                        break;
                    case "Escuela:":
                        escuela = tds.get(1).text();
                        break;
                    case "Prdo Admis:":
                        prdoAdmision = tds.get(1).text();
                        break;
                    case "Programa:":
                        programa = tds.get(1).text();
                        break;
                    case "Nivel:":
                        nivel = tds.get(1).text();
                        break;
//                    default:
//                        System.out.println(tds.get(0).text() + tds.get(1).text());
//                        break;
                }
            }
        }

        Element portletHorario = cargarProtletHorario(paginaInfoAcademica);

        Elements cursos = portletHorario.getElementsByTag("td");
        for (Element e : cursos){
            if (e.className().equals("hc-uportal-td2 hc-td")) {
                String sigla = e.text().split("-")[0];
                String seccion = e.text().split("-")[1];

                FiltroBuscaCursos filtro = new FiltroBuscaCursos();
                filtro.setSemestre("2016-1");
                filtro.setSigla(sigla);

                callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando ramo " + sigla + "-" + seccion));
                RamoAlumno ramo = new RamoAlumno(BuscaCursos.buscarCurso(filtro, Integer.valueOf(seccion), false));

                ramosEnCurso.add(ramo);
            }
        }

        cargarSalasPortal(portletHorario);

    }

    public void cargarSalasPortal(Element portletHorario){
        Element table = portletHorario.getElementsByClass("hc-table_data").first();
        Element tbody = table.getElementsByTag("tbody").first();
        int i = 1;
        String[] dias = new String[]{"L", "M", "W", "J", "V", "S"};
        for (Element element : tbody.children()){
            int j = 0;
            for (Element ts : element.children()){
                if (ts.tagName().equals("td")) {
                    if (ts.children().size() == 1) {
                        setSala(ts, dias[j], i);
                    }
                    j++;
                }
            }
            i++;
        }
    }

    public void setSala(Element ts, String day, int number){
        String sala = null;
        String sigla = null;

        for (Element tr : ts.getElementsByTag("tr")){
            Elements tds = tr.getElementsByTag("td");
            String key = tds.get(0).text();
            String value = tds.get(1).text();
            if (key.contains("Sala")){
                sala = value;
            }
            if (key.contains("Curso")){
                sigla = value.split("-")[0];
            }
        }
        if (sala != null && sigla != null){
            for (RamoAlumno ramo : ramosEnCurso){
                if (ramo.sigla.equals(sigla)){
                    ramo.setSala(day, number, sala);
                    for (HorarioString horarioString : ramo.getHorarioStrings()){
                        if (horarioString.dias.contains(day) && horarioString.dias.contains(String.valueOf(number))){
                            horarioString.addSala(sala);
                        }
                    }
                }
            }
        }
    }

    private Element cargarProtletHorario(Document paginaInfoAcademica) throws IOException {
        Elements scripts = paginaInfoAcademica.getElementsByTag("script");
        if (scripts.size() > 37) {
            Element dataElement = scripts.get(37);
            int urlBegin = dataElement.toString().indexOf("url:");
            int firstQuote = dataElement.toString().indexOf('"', urlBegin);
            int secondQuote = dataElement.toString().indexOf('"', firstQuote + 1);
            String url = dataElement.toString().substring(firstQuote + 1, secondQuote);

            UrlParameters urlParameters = new UrlParameters();
            urlParameters.addParameter("currentUrl", "/web/home-community/informacion-academica?gpi=10225");
            urlParameters.addParameter("p_l_id", "10706");
            urlParameters.addParameter("p_p_id", "horarioClases_WAR_LPT002_HorarioClases_INSTANCE_uXS5");
            urlParameters.addParameter("p_p_lifecycle", "0");
            urlParameters.addParameter("p_p_state", "normal");
            urlParameters.addParameter("p_p_mode", "view");
            urlParameters.addParameter("p_p_col_id", "column-1");
            urlParameters.addParameter("p_p_col_pos", "1");
            urlParameters.addParameter("p_p_col_count", "8");

            Response response = session.post("http://portal.uc.cl/" + url, urlParameters);
            return Jsoup.parse(response.getContent().toString());
        }else{
            throw new IOException();
        }
    }

    public void ssbLogin() throws IOException, ServerUnavailable {
        session.get("https://ssb.uc.cl/ERPUC/twbkwbis.P_WWWLogin");

        UrlParameters data = new UrlParameters();
        data.addParameter("sid", username);
        data.addParameter("PIN", password);
        Response response = session.post("https://ssb.uc.cl/ERPUC/twbkwbis.P_ValLogin", data);
        Document homeSsb = Jsoup.parse(response.getContent().toString());
        if (homeSsb.toString().contains("Service Temporarily Unavailable")){
            throw new ServerUnavailable();
        }
    }

    private Document ssoLogin(Document ssoDocument) throws IOException {
        Element form = ssoDocument.body().getElementsByTag("form").get(0);
        String action = form.attr("action");
        Elements inputs = form.getElementsByTag("input");

        String lt = inputs.get(2).attr("value");
        String execution = inputs.get(3).attr("value");
        String _eventId = inputs.get(4).attr("value");

        UrlParameters urlParameters = new UrlParameters();
        urlParameters.addParameter("username", username);
        urlParameters.addParameter("password", password);
        urlParameters.addParameter("lt", lt);
        urlParameters.addParameter("execution", execution);
        urlParameters.addParameter("_eventId", _eventId);
        Response response = session.post("https://sso.uc.cl" + action, urlParameters);
        return Jsoup.parse(response.getContent().toString());
    }

    public Document webCursosLogin() throws IOException, LoginException {
        Document webCursosLoginPage = getWebcursosLoginPage();
        if (webCursosLoginPage.title().contains("Mi Espacio")){
            return webCursosLoginPage;
        }
        Document webCursosDocument = ssoLogin(webCursosLoginPage);
        if (webCursosDocument.title().contains("Mi Espacio")) {
            return webCursosDocument;
        }else{
            throw new LoginException();
        }
    }

    public Document getWebcursosLoginPage() throws IOException {
        return Jsoup.parse(session.get("http://webcurso.uc.cl/portal/login").getContent().toString());
    }

    public Document sibucLogin() throws IOException, LoginException {
        Document loginPage = getSibucLoginPage();
        if (loginPage.text().contains("Cerrar ses")){
            return loginPage;
        }
        Document sibucPage = ssoLogin(loginPage);
        if (!sibucPage.text().contains("Cerrar ses")){
            throw new LoginException();
        }
        return sibucPage;
    }

    public Document getSibucLoginPage() throws IOException {
        return Jsoup.parse(session.get("http://bibliotecas.uc.cl/index.php?tmpl=autentifica&option=com_rsform&view=rsform&formId=&Itemid=53").getContent().toString());
    }

    public void reload(LoadingCallback callback) throws IOException, LoginException {
        ramosEnCurso = new Ramos<>();
        callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando datos..."));
        if (portalSessionExpired()) {
            portalLogin();
        }
        cargarInfoPortal();
        try {
            callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando info personal..."));
            cargarInfoPersonal();
            callback.onProgressChange(new Progress(0, Progress.NORMAL, "Cargando info academica..."));
            cargarInfoAcademica(callback);
        } catch (SessionExpired e){
            // HIGHLY UNLIKELY DUE TO RECENT LOGIN
        }
        fichaAcademica = new FichaAcademica(this, callback);
        callback.onProgressChange(new Progress(0, Progress.NORMAL, "Datos cargados..."));
    }

    public ArrayList<Vacante> vacantesCorrespondientes(ArrayList<Vacante> vacantes){
        ArrayList<Vacante> vacantesCorrespondientes = new ArrayList<>();
        for (Vacante vacante : vacantes){
            if (vacanteCorresponde(vacante)){
                vacantesCorrespondientes.add(vacante);
            }
        }
        return vacantesCorrespondientes;
    }

    public Document labmatLogin() throws IOException {
        session.get("http://labmat.puc.cl/login");
        UrlParameters parameters = new UrlParameters();
        parameters.addParameter("usuario", username + "@uc.cl");
        parameters.addParameter("clave", password);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        session.post("http://labmat.puc.cl/login/actionLogin", parameters, headers);
        Response response = session.get("http://labmat.puc.cl/dashboard");
        Document document = Jsoup.parse(response.getContent().toString());
        return document;
    }

    public Document sidingLogin() throws IOException {
        UrlParameters parameters = new UrlParameters();
        parameters.addParameter("login", username);
        parameters.addParameter("passwd", password);
        parameters.addParameter("sw", "");
        parameters.addParameter("sh", "");
        parameters.addParameter("cd", "");
        session.post("https://intrawww.ing.puc.cl/siding/index.phtml", parameters);
        return Jsoup.parse(session.get("https://intrawww.ing.puc.cl/siding/index.phtml").getContent().toString());
    }

    public int getPrdoAdmisionCode(){
        String periodo;
        if (prdoAdmision.contains("Primer")){
            periodo = "20";
        }else if (prdoAdmision.contains("Segundo")){
            periodo = "22";
        }else {
            periodo = "24";
        }
        String prdoText = prdoAdmision.substring(0, prdoAdmision.indexOf(' ')) + periodo;
        return Integer.valueOf(prdoText);
    }

    public boolean vacanteCorresponde(Vacante vacante){
        return (vacante.escuela.equals("Vacantes libres") ||
                ((vacante.escuela.length() == 0 || (vacante.escuela.length() > 0 && vacante.escuela.contains(escuela))) &&
                        (vacante.prdoAdmision.length() == 0 || (vacante.prdoAdmision.length() > 0 && vacante.prdoAdmision.equals(String.valueOf(getPrdoAdmisionCode())))) &&
                        (vacante.programa.length() == 0 || (vacante.programa.length() > 0 && vacante.programa.contains(programa))) &&
                        (vacante.nivel.length() == 0 || (vacante.nivel.length() > 0 && vacante.nivel.contains(nivel)))
                ));
    }

    public boolean puedeTomarRamo(RamoBuscaCursos ramoBuscaCursos) {
        return cumpleRequisito(ramoBuscaCursos) && ramoTieneVacantes(ramoBuscaCursos);
    }

    public boolean cumpleRequisito(RamoBuscaCursos ramo){
        return ramo.cumpleRequisito(this);
    }

    public boolean ramoTieneVacantes(RamoBuscaCursos ramo){
        return ramo.alumnoTieneVacantes(this);
    }

    public void reload() throws IOException, LoginException {
        reload(new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing
            }
        });
    }

    public void reloadFichaAcademica(LoadingCallback callback) throws IOException, LoginException {
        fichaAcademica = new FichaAcademica(this, callback);
        callback.onProgressChange(new Progress(0, Progress.NORMAL, "Datos Cargados..."));
    }

    public void reloadFichaAcademica() throws IOException, LoginException {
        reloadFichaAcademica(new LoadingCallback() {
            @Override
            public void onProgressChange(Progress progress) {
                // Do nothing
            }
        });
    }

    public float getPga(){
        float pga = 0;
        float creditosTotales = creditosTotales();

        for (Semestre s : fichaAcademica.getSemestres()){
            for (RamoCursado r : s.getRamosCursados()){
                if (r.getCreditos() > 0 && creditosTotales() > 0){
                    try {
                        pga += ((float) r.getCreditos() / creditosTotales) * Float.parseFloat(r.getNota());
                    }catch (NumberFormatException e){
                        pga += ((float) r.getCreditos() / creditosTotales) * Float.parseFloat(r.getNota().substring(0, 3));
                    }
                }
            }
        }
        return pga;
    }

    public float creditosTotales(){
        float creditos = 0;
        for (Semestre s : fichaAcademica.getSemestres()){
            for (RamoCursado r : s.getRamosCursados()){
                creditos += r.getCreditos();
            }
        }
        return creditos;
    }

    public Ramos<RamoAlumno> getRamosEnCurso(){
        return ramosEnCurso;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public String getPais() {
        return pais;
    }

    public String getRut() {
        return rut;
    }

    public Date getFechaDeNacimiento() {
        return fechaDeNacimiento;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getNumeroDeAlumno() {
        return numeroDeAlumno;
    }

    public int getGeneracion() {
        return generacion;
    }

    public String getSituacion() {
        return situacion;
    }

    public Session getSession(){
        return session;
    }

    public byte[] getFotoPortal(){
        return fotoPortal;
    }

    public FichaAcademica getFichaAcademica() {
        return fichaAcademica;
    }

    public String getEscuela() {
        return escuela;
    }

    public String getNivel() {
        return nivel;
    }

    public String getPrograma() {
        return programa;
    }

    public String getPrdoAdmision() {
        return prdoAdmision;
    }

    public void sendEmail(Message message) throws MessagingException{
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp-externo.puc.cl");
        props.put("mail.smtp.socketFactory.port", "25");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", "25");

        javax.mail.Session session = javax.mail.Session.getInstance(props, null);
        Transport tr = session.getTransport("smtp");
        tr.connect(username + "@uc.cl", password);
        message.saveChanges();
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

    public MimeMessage newMessage() throws MessagingException {
        MimeMessage message = new MimeMessage(getSmtpSession());
        message.setFrom(new InternetAddress(username + "@uc.cl"));
        return message;
    }

    public javax.mail.Session getSmtpSession(){
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp-externo.puc.cl");
        props.put("mail.smtp.socketFactory.port", "25");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", "25");
        return javax.mail.Session.getInstance(props, null);
    }

    public Store getMailStore() throws MessagingException{
        if (store == null){
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.imap.port", "993");
            props.setProperty("mail.imap.ssl.enable", "true");
            props.setProperty("mail.imap.ssl.trust", "*");
            javax.mail.Session session = javax.mail.Session.getInstance(props, null);
            store = session.getStore();
        }
        if (!store.isConnected()) {
            if (segundaClave == null) {
                store.connect("gimap.puc.cl", username, password);
            }else{
                store.connect("imap.gmail.com", username + "@uc.cl", segundaClave);
            }
        }
        return store;
    }

    public String getSegundaClave() {
        return segundaClave;
    }

    public void setSegundaClave(String segundaClave) {
        this.segundaClave = segundaClave;
    }
}
