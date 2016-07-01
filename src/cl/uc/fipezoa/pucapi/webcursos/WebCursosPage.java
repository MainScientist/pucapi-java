package cl.uc.fipezoa.pucapi.webcursos;

import cl.uc.fipezoa.pucapi.AlumnoUC;
import cl.uc.fipezoa.pucapi.exceptions.LoginException;
import cl.uc.fipezoa.requests.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Created by MainScientist on 6/15/16.
 */
public class WebCursosPage {

    private AlumnoUC alumnoUC;

    public WebCursosPage(AlumnoUC alumnoUC) throws IOException, LoginException {
        this.alumnoUC = alumnoUC;
        Document page = alumnoUC.webCursosLogin();
        Element linkList = page.getElementById("siteLinkList");
        for (Element element : linkList.getElementsByTag("a")) {
            String url = element.attr("href");
            if (!url.equals("#")) {
                CursoWebCursos curso_page = new CursoWebCursos(this, url);
                FolderWebCursos folder = curso_page.root;
                System.out.println("-------");
                System.out.println(element.attr("title"));
                System.out.println(folder);
            }
        }
        Element selectNav = page.getElementById("selectNav");
        for (Element e : selectNav.getElementsByTag("option")){
            if (e.hasAttr("value") && e.hasAttr("title")){
                CursoWebCursos curso_page = new CursoWebCursos(this, e.attr("value"));
                FolderWebCursos folder = curso_page.root;
                System.out.println("-------");
                System.out.println(e.attr("title"));
                System.out.println(folder);
            }
        }
    }

    public AlumnoUC getAlumnoUC() {
        return alumnoUC;
    }


}
