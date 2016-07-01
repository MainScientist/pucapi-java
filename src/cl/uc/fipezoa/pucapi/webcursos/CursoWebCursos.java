package cl.uc.fipezoa.pucapi.webcursos;

import cl.uc.fipezoa.requests.Response;
import cl.uc.fipezoa.requests.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by MainScientist on 6/15/16.
 */
public class CursoWebCursos {

    private WebCursosPage webCursosPage;
    private Session session;
    public String filesUrl;
    public FolderWebCursos root;

    public CursoWebCursos(WebCursosPage webCursosPage, String url) throws IOException {
        this.webCursosPage = webCursosPage;
        session = webCursosPage.getAlumnoUC().getSession();
        Response resp = session.get(url);
        Document page = Jsoup.parse(resp.getContent().toString());
        Elements tools = page.getElementById("toolMenu").getElementsByTag("a");
        for (Element e : tools){
            switch (e.text()){
                case "Recursos":
                    cargarRecursos(e.attr("href"));
                    break;
            }
        }
    }

    private void cargarRecursos(String url) throws IOException {
        filesUrl = getFilesURL(url);
        int index = filesUrl.indexOf("/tool-reset");
        filesUrl = "http://webcurso.uc.cl/portal/tool/" + filesUrl.substring(index + 12);
        root = new FolderWebCursos(this, filesUrl, "root", true);
    }

    private String getFilesURL(String recursosUrl) throws IOException {
        Response resp = session.get(recursosUrl);
        Document page = Jsoup.parse(resp.getContent().toString());
        for (Element e: page.getElementById("col1").getElementsByTag("a")){
            if (e.hasAttr("title") && e.attr("title").equals("Recargar")){
                return e.attr("href");
            }
        }
        return "";
    }

    public Session getSession() {
        return session;
    }
}
