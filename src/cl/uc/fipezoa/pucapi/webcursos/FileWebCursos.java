package cl.uc.fipezoa.pucapi.webcursos;

/**
 * Created by MainScientist on 6/15/16.
 */
public class FileWebCursos {

    String downloadUrl;
    String nombre;
    String size;
    String dateUploaded;
    String uploadedBy;

    public FileWebCursos(String downloadUrl, String nombre, String size, String dateUploaded, String uploadedBy) {
        this.downloadUrl = downloadUrl;
        this.nombre = nombre;
        this.size = size;
        this.dateUploaded = dateUploaded;
        this.uploadedBy = uploadedBy;
    }
}
