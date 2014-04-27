/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth Jeffrey Juan
 */
class HiloIF implements Runnable{
     int[] instrucciontemp;
    
     // Constructor primero lo que inicializa es un vector en la que se almacenara
     // una instruccion temporal
    public HiloIF(){
        instrucciontemp= new int[5];
    }
    
    /*Se encarga de agrandar el vector de instrucciones en uno, para almacenar ademas el pc
     de la instruccion que esta haciendo fetch*/
    public int[] agrandarInstruccion(int [] inst){
        int [] v = new int[5];
        for (int i = 0; i < inst.length; i++) {
            v[i] = inst[i];
        }
        v[4] = Arqui3.pc;
        return v;
    }
    /*
     */
    public void cargarInstruccion(){
        instrucciontemp =  agrandarInstruccion(Arqui3.memoriasI.traerIstruccion(Arqui3.pc));
        System.out.println("Etapa IF cargo  " + Arrays.toString(instrucciontemp) + "Arqui pc: " + Arqui3.pc);
    }
    

    
    public void run(){
        try {
            Arqui3.mutexIF_ID.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloIF.class.getName()).log(Level.SEVERE, null, ex);
        }
           System.out.println("seguir en If :" + Arrays.toString(Arqui3.seguir));
           System.out.println("Arqui3.meterBurbuja: "+Arqui3.meterBurbuja + "Arqui3.meterBurbujaXFordwarding ="+Arqui3.meterBurbujaXFordwarding);
        
        if((Arqui3.seguir[0] == 0 && Arqui3.seguir[3] <= 0) && Arqui3.meterBurbuja == false &&  Arqui3.meterBurbujaXFordwarding==false ){// si if no esta detenido ni ex 
            cargarInstruccion();
         
             Arqui3.npc = Arqui3.pc;
            
            Arqui3.instruccionActual = instrucciontemp.clone();
       
            System.out.println(Arrays.toString(Arqui3.instruccionActual));
            /*if(Arqui3.pc == Integer.MIN_VALUE){
                Arqui3.pc = 0;
            }*/
            Arqui3.pc += 4;
               
        }
       if(Arqui3.meterBurbuja==true){
            Arqui3.npc = -1;
            Arrays.fill(Arqui3.instruccionActual, 0);
           Arqui3.instruccionActual[4]= -1;
            System.out.println("metiendo burbuja memIns ");
        }
        
        
        Arqui3.seguir[0] -= 1;
        Arqui3.mutexIF_ID.release();
    }
        
}
