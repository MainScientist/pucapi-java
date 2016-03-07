package cl.uc.fipezoa.pucapi.buscacursos;

import cl.uc.fipezoa.pucapi.buscacursos.RamoBuscaCursos;

import java.io.IOException;

/**
 * Created by fipezoa on 2/17/2016.
 */
public class RamoAlumno extends RamoBuscaCursos {

    public boolean cargado = false;
    public boolean cargadoParcialmente = false;

    public RamoAlumno(RamoBuscaCursos ramo){
        super( ramo.getPeriodo(), ramo.getSigla(), ramo.getNrc(), ramo.getCreditos(), ramo.getSeccion(), ramo.getNombre(),
                ramo.getCampus(), ramo.getUnidadAcademica(), ramo.getProfesores(), ramo.dictadoEnIngles(), ramo.permiteRetiro(),
                ramo.requiereAprobEspecial(), ramo.getVacantesTotales(), ramo.getVacantesDisponibles(), ramo.tieneUnaSeccion());
        if (ramo.programaCargado()){
            this.programa = ramo.programa;
            this.programaCargado = true;
        }
        if (ramo.requisitoCargado()){
            this.requisito = ramo.requisito;
            this.requisitoCargado = true;
        }
        if (ramo.vacantesCargadas()){
            this.vacantesReservadas = ramo.vacantesReservadas;
            this.vacantesCargadas = true;
        }
        if (ramo.completamenteCargado()){
            this.cargado = true;
        }
        cargadoParcialmente = true;
        cargarModulos(ramo);
        horarioStrings = ramo.getHorarioStrings();
    }

    public void cargarModulos(RamoBuscaCursos ramoBuscaCursos){
        for (Modulo modulo: ramoBuscaCursos.getModulos()){
            this.modulos.add(modulo);
            modulo.setRamo(this);
        }
    }

    public RamoAlumno(String sigla, int seccion){
        super(sigla, seccion);
    }

    public void cargar(boolean caragarTodo) throws IOException {
        FiltroBuscaCursos filtro = new FiltroBuscaCursos();
        filtro.setSigla(this.sigla);
        RamoBuscaCursos ramo = BuscaCursos.buscarCurso(filtro, this.seccion, caragarTodo);
        cargarDeRamo(ramo);
    }

    private void cargarDeRamo(RamoBuscaCursos ramo){
        this.periodo = ramo.periodo;
        this.nrc = ramo.nrc;
        this.creditos = ramo.creditos;
        this.nombre = ramo.nombre;
        this.campus = ramo.campus;
        this.unidadAcademica = ramo.unidadAcademica;
        this.profesores = ramo.profesores;
        this.permiteRetiro = ramo.permiteRetiro;
        this.dictadoEnIngles = ramo.dictadoEnIngles;
        this.requiereAprobEspecial = ramo.requiereAprobEspecial;
        this.vacantesTotales = ramo.vacantesTotales;
        this.vacantesDisponibles = ramo.vacantesDisponibles;
        this.tieneUnaSeccion = ramo.tieneUnaSeccion;
        if (ramo.programaCargado()){
            this.programa = ramo.programa;
            this.programaCargado = true;
        }
        if (ramo.requisitoCargado()){
            this.requisito = ramo.requisito;
            this.requisitoCargado = true;
        }
        if (ramo.vacantesCargadas()){
            this.vacantesReservadas = ramo.vacantesReservadas;
            this.vacantesCargadas = true;
        }
        if (ramo.completamenteCargado()){
            this.cargado = true;
        }
        cargadoParcialmente = true;
    }

    public void setSala(String dia, int numero, String sala){
        for (Modulo modulo : modulos){
            if (modulo.getDia().equals(dia) && modulo.getNumero() == numero){
                modulo.setSala(sala);
                break;
            }
        }
    }

}
