/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JJ
 */
public class ManejadorDeContextos {
    public static int indice=0;
    //public static int hilo;
    public ManejadorDeContextos() {
      
    }
    
    public static List<Contexto> hilos = new ArrayList<Contexto>();
    public static List<Contexto> hilosFinalizados = new ArrayList<Contexto>();

    
    public static void ubicarHilos(){
      int hilo=0;
       int ultimodelanterior =-4;
       int inicio = 0;
       //System.out.println(Arrays.toString(Arqui3.memoriasI.memoriaInstrucciones));
       for(int i =0; i < Arqui3.cantidadinstruccionesporhilo.length; i++){
            if(Arqui3.cantidadinstruccionesporhilo[i]!=0){
                Contexto nuevo=new Contexto();
                hilos.add(nuevo);
                hilos.get(hilo).pc = inicio;
                hilos.get(hilo).idHilo=hilo;
                hilo++;  
                inicio +=Arqui3.cantidadinstruccionesporhilo [i]*4; 
            }
            
        }
    }
    
    public void guardarContexto(int numeroContexto){
      hilos.get(numeroContexto).registros=Arqui3.registros.clone();
      hilos.get(numeroContexto).pc=Arqui3.pc;
      hilos.get(numeroContexto).rl=-1;
     if (Arqui3.memoriasI.memoriaInstrucciones[Arqui3.pc]== 22 &&  Arqui3.memoriasI.memoriaInstrucciones[Arqui3.pc-4]== 11){
          hilos.get(numeroContexto).pc-= 4;
      }
     if (Arqui3.memoriasI.memoriaInstrucciones[Arqui3.pc]== 4 &&  Arqui3.memoriasI.memoriaInstrucciones[Arqui3.pc-4]== 22){
        //  hilos.get(numeroContexto).pc-= 8;
      }
     
       hilos.get(numeroContexto).duracion += Arqui3.duracionhiloactual;
    }
    
    public void sacarContexto(int numeroContexto){
      Arqui3.registros=hilos.get(numeroContexto).registros;
      Arqui3.pc=hilos.get(numeroContexto).pc;
      Arqui3.RL=hilos.get(numeroContexto).rl;
      Arqui3.finalizoHiloContexto = false;
      Arqui3.meter63 = false;
      Arqui3.terminoQuantum = false;
        
    }

    public void cambiarContexto(){
       guardarContexto(0);
       Contexto viejo=hilos.get(0);
       hilos.remove(0);
       if(!Arqui3.finalizoHiloContexto){
           hilos.add(viejo);
       }else{
           System.out.println("Finalizo El hilo");
           hilosFinalizados.add(viejo);
       }
       sacarContexto(0);
       indice++;
       if (indice > hilos.size()){
           indice = 0;
       }
    }
    
    public String imprimirContexto(){
        String respuesta = "Por orden de finalizacion:\n\n";
        int i = 0;
        for (Contexto contexto : hilosFinalizados) {
            respuesta += "Hilo ID: " + contexto.idHilo + "\n";
            respuesta += "RL: " + contexto.rl + "\n";
            respuesta += "PC: " + contexto.pc + "\n"; 
            respuesta += "Ciclos de duracion: " + contexto.duracion + "\n"; 
            respuesta +="Registros: " + Arrays.toString(contexto.registros) + "\n\n\n";
            i++;
        }
        
        return respuesta;
    }
    
    public boolean SaberFinalizoPrograma(int cantidadHilos){
        boolean respuesta = false;
        if(hilosFinalizados.size() == cantidadHilos && hilos.size() == 0){
            respuesta  = true;
        }
        return respuesta;
    }
}  


