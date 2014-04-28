/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth Jeffrey Juan
 */
public class HiloID implements Runnable{
    int [] instruccionID;
    int pcTemporal = 0;
    int[] abInmTemporal = new int[3];
    
    public HiloID(){
        //descometar
        instruccionID = Arqui3.matrizIRS[0].clone();
    }
    
    public int identificarInstruccion(){
        int indice = -1;
        for (int i = 0; i < Arqui3.tablaInstrucciones.length; i++) {
            if(Arqui3.tablaInstrucciones[i][0] == instruccionID[0]){
                indice = i;
                break;
            }
        }
        //System.out.println("\n" + indice);
        return indice;
    }
    
    public void cargarAB(int indice){
        //mete en A
       // System.out.println(Arqui3.tablaInstrucciones[indice][1]);
        if(indice != -1 &&  Arqui3.tablaInstrucciones[indice][1] != -1){// quiere decir que ocupa A
            int tmpA = Arqui3.saberForwardingID(instruccionID,1);
            System.out.println("El tmpA es " + tmpA);
            if(tmpA != -1 && Arqui3.buscarEnTablaForwardnigPorPC(instruccionID[4])!=tmpA ){
                if(Arqui3.cargarDesdeFordwarding(tmpA) != Integer.MIN_VALUE){ //Esta listo
                    
                    abInmTemporal[0] = Arqui3.cargarDesdeFordwarding(tmpA);
                    System.out.println("Se metio A: " + abInmTemporal[0]);

                }else{
                    //RETRASADO POR FORDWARDING
                    System.out.println("Se metio a  A burbuja ");
                    Arqui3.meterBurbujaXFordwarding = true;
                }
            }else{ //El A esta en los registros
                abInmTemporal[0] = Arqui3.registros[instruccionID[Arqui3.tablaInstrucciones[indice][1]]];
            }
        }
        
        if( indice != -1 &&  Arqui3.tablaInstrucciones[indice][2] != -1){// quiere decir que ocupa b
            int tmpB = Arqui3.saberForwardingID(instruccionID,2);
            if(tmpB != -1 && Arqui3.buscarEnTablaForwardnigPorPC(instruccionID[4])!=tmpB){//Quiere que el A esta fordwarding
                if(Arqui3.cargarDesdeFordwarding(tmpB) != Integer.MIN_VALUE){ //Esta listo
                    abInmTemporal[1] = Arqui3.cargarDesdeFordwarding(tmpB);
                    System.out.println("Se metio b : " + abInmTemporal[1]);

                }else{
                    //RETRASADO POR FORDWARDING
                    System.out.println("Se metio a  B burbuja ");

                    Arqui3.meterBurbujaXFordwarding = true;
                }
            }else{ //El B esta en los registros
                abInmTemporal[1] = Arqui3.registros[instruccionID[Arqui3.tablaInstrucciones[indice][2]]];    
            }
        }

        //mete en Inmdiato 
        if(indice != -1 &&  Arqui3.tablaInstrucciones[indice][3] != -1){
            abInmTemporal[2] = instruccionID[Arqui3.tablaInstrucciones[indice][3]];
        }
    }
    
    
    private void resolverBranches(int cod) {
        int rx =-1;
        if (cod == 4 || cod == 5 || cod == 2){// perguntamos si son los branches que ocupan el registro 
           // Arqui3.meterBurbujaXFordwarding = false;
            rx = Arqui3.registros[Arqui3.matrizIRS[0][1]];
            // hay que preguntar si el registro esta en fordwarding 
            int tmpA = Arqui3.saberForwardingID(instruccionID,1);
            System.out.println("resolvio  " + tmpA);
            if(tmpA != -1 && Arqui3.buscarEnTablaForwardnigPorPC(instruccionID[4])!=tmpA){//Quiere decir que el r esta fordwarding
                
                if(Arqui3.cargarDesdeFordwarding(tmpA) != Integer.MIN_VALUE){ //Esta listo?
                    rx = Arqui3.cargarDesdeFordwarding(tmpA); // mete el valor 
                }else{
                    //RETRASADO POR FORDWARDING
                    Arqui3.meterBurbujaXFordwarding = true;
                }
  
            }
        }
        
        if(cod == 4 && rx == 0 && Arqui3.meterBurbujaXFordwarding == false){//BEQZ
            
            pcTemporal = Arqui3.pc + 4*Arqui3.matrizIRS[0][3]; // deberia de ser pc , pero como nosotros no detenemos que retrasar seria npc 
            
        }
        
        System.out.println("EL PPPPPP Rx = " + rx + " meterBurbuja " + Arqui3.meterBurbujaXFordwarding);
        if(cod==5 && rx != 0 && Arqui3.meterBurbujaXFordwarding == false){//BNEZ
            
            pcTemporal = Arqui3.pc + 4*Arqui3.matrizIRS[0][3];
        }
        
        if(cod==3){//JAL este es especial porque ocupa pasar pc a R31
            //Solo para probar
            //Arqui3.registros[31] = Arqui3.pc;
            pcTemporal = Arqui3.pc + Arqui3.matrizIRS[0][3];
            System.out.println("Entre al JAL " + pcTemporal + " el pc en la instruccion es: " + Arqui3.matrizIRS[0][3] + " el pc " + Arqui3.pc);
        }
        
        if(cod==2 && Arqui3.meterBurbujaXFordwarding == false){//JR
            pcTemporal=  rx;
        }
    }
    
       
    @Override
    public void run() {
        try {
            Arqui3.mutexEX_ID.acquire(); // espera hasta que ex libere la juagada 
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloIF.class.getName()).log(Level.SEVERE, null, ex);
        }
        
          if( Arqui3.seguir[3] <= 0 ){ // si no esta detenido en EX 
                
            pcTemporal = Arqui3.pc;
            int ind  = identificarInstruccion();
            int cod = instruccionID[0];
            System.out.println("Etapa ID "+Arqui3.pc + Arrays.toString(Arqui3.matrizIRS[0]));
            if(cod == 4 || cod == 5  || cod == 2 ){// es para ver si es un branch 
                resolverBranches(cod);
                System.out.println("******************el nueno pc depues de resolver branch =  "+pcTemporal);
                Arqui3.pc  = pcTemporal ;
                Arrays.fill( Arqui3.abInm , 0) ;
                
                // tenemos que detener if !! 
            }else{
                if (cod != 0 ) {
                cargarAB(ind);
                    
                }
                if(cod == 3){// el 3 es un branch pero lo metemos aqui porq necesita hacer una suma
                      resolverBranches(cod);
                      Arqui3.pc  = pcTemporal ;
                }
                
                Arqui3.abInm=abInmTemporal.clone();
            }
        }
        Arqui3.seguir[1] -= 1;
        Arqui3.mutexEX_ID.release();
        Arqui3.mutexIF_ID.release();
      
    }
}