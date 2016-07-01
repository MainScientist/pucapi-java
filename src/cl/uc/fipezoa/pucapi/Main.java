package cl.uc.fipezoa.pucapi;

import cl.uc.fipezoa.pucapi.buscacursos.*;
import cl.uc.fipezoa.pucapi.callbacks.LoadingCallback;
import cl.uc.fipezoa.pucapi.callbacks.Progress;
import cl.uc.fipezoa.pucapi.exceptions.LoginException;
import cl.uc.fipezoa.requests.Requests;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;

/**
 * Created by fipezoa on 1/25/2016.
 */
public class Main {

    public static void main(String[] args){
        try {
            String version = Jsoup
                    .parse(Requests
                            .get("https://docs.google.com/document/d/1QYDvp7ZTApy7IKQUH4DHXkDpJkkjpu4DVAiOxJ47V34/edit?usp=sharing")
                            .getContent()
                            .toString()
                    ).text();
            String newVersion = version.substring(0, version.indexOf("-")).trim();
            System.out.println(newVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FiltroBuscaCursos filtro = new FiltroBuscaCursos("2016-2");
        filtro.setSigla("IIC2233");
        try {
            Ramos<RamoBuscaCursos> ramos = BuscaCursos.buscarCursos(filtro, true);
            for (RamoBuscaCursos r : ramos){
                for (Modulo m : r.getModulos()){
                    System.out.format("%s %d %s \n", m.getDia(), m.getNumero(), m.getTipo());
                }
                System.out.println(r.sigla);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            AlumnoUC alumnoUC = PUC.login(MainData.USUARIO_SIN_UC, MainData.PASSWORD);
        }catch (LoginException e){

        } catch (IOException e) {
            e.printStackTrace();
        }

//        try{
//
//            AlumnoUC alumnoUC = PUC.login("usuarioSINuc", "Password", new LoadingCallback() {
//                @Override
//                public void onProgressChange(Progress progress) {
//                    System.out.println(progress.message);
//                }
//            });
//
//            alumnoUC.setSegundaClave("SEGUNDA CLAVE");
//
//
//            for (RamoAlumno ramo : alumnoUC.getRamosEnCurso()){
//                System.out.println(ramo.getSigla());
//                for (Modulo modulo : ramo.getModulos()){
//                    System.out.println(modulo.getDia() + "-" + String.valueOf(modulo.getNumero()) + ": " + modulo.getTipo() + ", " + modulo.getSala());
//                }
//                for (HorarioString horarioString : ramo.getHorarioStrings()){
//                    System.out.println(horarioString.dias + "\t" + horarioString.tipo + "\t" + horarioString.sala);
//                }
//                System.out.println("-----------------------------");
//            }
//
//
//
//            Store store = alumnoUC.getMailStore();
//            Folder inbox = store.getFolder("INBOX");
//            inbox.open(Folder.READ_WRITE);
//            Message message = inbox.getMessage(inbox.getMessageCount());
//            for (Address address : message.getFrom()) {
//                System.out.println("FROM:" + address.toString());
//            }
//            message.setFlag(Flags.Flag.FLAGGED, true);
//            System.out.println("SENT DATE:" + message.getSentDate());
//            System.out.println("SUBJECT:" + message.getSubject());
//            System.out.println("CONTENT:" + message.getContent());
//            javax.mail.Folder[] folders = store.getDefaultFolder().list("*");
//            for (Folder folder : folders) {
//                System.out.println(folder.getFullName());
//                if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0){
//                    System.out.println("Holds folders");
//                }
//                if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0){
//                    System.out.println("Holds messages");
//                }
//            }
//            inbox.close(true);
//            store.close();
//
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        catch (LoginException e) {
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }

//        try {
//            AlumnoUC alumnoUC = PUC.login("fipezoa", "FelipeI07", new LoadingCallback(){
//
//                @Override
//                public void onProgressChange(Progress progress) {
//                    System.out.println(progress.message);
//                }
//            });
//            FileOutputStream fos = new FileOutputStream("D://Joker/alumnoUc.puc");
//            ObjectOutputStream oos = new ObjectOutputStream(fos);
//            oos.writeObject(alumnoUC);
//
//            fos.close();
//            oos.close();
//
//            FileInputStream fis = new FileInputStream("D://Joker/alumnoUc.puc");
//            ObjectInputStream ois = new ObjectInputStream(fis);
//            AlumnoUC alumnoUCLeido = (AlumnoUC) ois.readObject();
//            System.out.println(alumnoUCLeido.getNombre());
//            System.out.println(alumnoUCLeido.getRamosEnCurso().size());
//
//            ois.close();
//            fis.close();
//
//        }catch (IOException | ClassNotFoundException e){
//            e.printStackTrace();
//        }catch (LoginException e) {
//            System.out.println("BAD CREDENTIALS!");
//        }

    }
}
