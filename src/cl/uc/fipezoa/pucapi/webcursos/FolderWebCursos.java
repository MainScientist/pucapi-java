package cl.uc.fipezoa.pucapi.webcursos;

import cl.uc.fipezoa.requests.Response;
import cl.uc.fipezoa.requests.Session;
import cl.uc.fipezoa.requests.UrlParameters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by MainScientist on 6/15/16.
 */
public class FolderWebCursos {

    CursoWebCursos curso;
    String nombre;
    Session session;
    ArrayList<FolderWebCursos> folders = new ArrayList<>();
    ArrayList<FileWebCursos> files = new ArrayList<>();

    public FolderWebCursos(CursoWebCursos curso, String url, String nombre, boolean root) throws IOException {
        this.curso = curso;
        this.nombre = nombre;
        session = curso.getSession();


        if (root){
            loadRoot(url);
        }else{
            loadNonRoot(url);
        }

    }

    private void loadRoot(String url) throws IOException {
        Document page = Jsoup.parse(session.get(url).getContent().toString());
        loadFolders(page);
    }

    private void loadNonRoot(String url) throws IOException {
        UrlParameters params = new UrlParameters();
        params.addParameter("source", "0");
        params.addParameter("collectionId", url);
        params.addParameter("navRoot", "");
        params.addParameter("criteria", "title");
        params.addParameter("sakai_action", "doNavigate");
        params.addParameter("rt_action", "");
        params.addParameter("selectedItemId", "");
        Response resp = session.post(curso.filesUrl, params);
        Document page = Jsoup.parse(resp.getContent().toString());
        loadFolders(page);
    }

    public void loadFolders(Document page) throws IOException {
        Element table = page.getElementsByTag("table").first();
        // System.out.println("-------------------------");
        int i = 0;
        for (Element e : table.getElementsByTag("tr")) {
            Elements columns = e.getElementsByTag("td");
            if (columns.size() > 0) {
                Elements links = columns.get(2).getElementsByAttribute("title");
                Element nameElement = links.first();
                if (nameElement.attr("title").equals("Abrir esta carpeta")) {
                    nameElement = links.get(1);
                }
                String resource_name = nameElement.text();
                String resurce_type = nameElement.attr("title");
                String downloadLink = nameElement.attr("href");
                if (!resurce_type.equals("Carpeta")) {
                    String size = columns.get(9).text();
                    FileWebCursos file = new FileWebCursos(downloadLink, resource_name, size, "", "");
                    files.add(file);
                    // System.out.format("%s (%s), %s - Download from: %s\n", resource_name, resurce_type, size, downloadLink);
                } else if (i != 0) {
                    downloadLink = nameElement.attr("onclick");
                    int index1 = downloadLink.indexOf("/group/");
                    int index2 = downloadLink.indexOf("'", index1);
                    String collectionId = downloadLink.substring(index1, index2);
                    // System.out.format("%s (%s) - Load from: %s\n", resource_name, resurce_type, collectionId);
                    FolderWebCursos newFolder = new FolderWebCursos(curso, collectionId, resource_name, false);
                    folders.add(newFolder);
                }
                i++;
            }
        }
    }

    @Override
    public String toString() {
        String r = nombre + "\n";
        for (FolderWebCursos f : folders){
            String[] lines = f.toString().split("\n");
            for (String line : lines){
                r += "\t" + line + "\n";
            }
        }
        for (FileWebCursos f : files){
            r += "\t" + f.nombre + "\n";
        }
        return r.substring(0, r.length()-1);
    }
}
