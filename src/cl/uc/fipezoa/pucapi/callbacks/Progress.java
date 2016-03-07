package cl.uc.fipezoa.pucapi.callbacks;

/**
 * Created by fipezoa on 2/3/2016.
 */
public class Progress {

    public static String ERROR = "cl.uc.fipezoa.callbacks.progress.ERROR";
    public static String NORMAL = "cl.uc.fipezoa.callbacks.progress.NORMAL";

    public String message;
    public int percent;
    public String type;

    public Progress(int percent, String type, String message){
        this.percent = percent;
        this.message = message;
        this.type = type;
    }
}
