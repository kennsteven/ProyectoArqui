/*
 * Universidad de Costa Rica , 2012
 * Arquitectura de Computadoras 
 * Autores: kenneth alvarado A90300, Jeffry Castro A91507, Juan Carvajal A91378
 */

package arqui3;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

class HiloEX implements Runnable{
   
    
    int aloTemporal = 0;
    int bTemporal = 0;
    int []instruccion;// para guardar la intruccion que se esta ejecutando 
    

    
    
    // para sumar lo que este en los resgistros que resibi desde id 
   private void suma(int t) {
        int suma = 0;
        int indice = identificarInstruccion(t);
        for(int i = 0; i < 3; i++){
            if(Arqui3.tablaInstrucciones[indice][i+1] != -1){
                suma += Arqui3.abInm[i];
            }
        }
        aloTemporal = suma;
    }
    // para restar lo que este en los resgistros que resibi desde id 
    private void resta() {
        int resta = Arqui3.abInm[0] - Arqui3.abInm[1];
        aloTemporal = resta;
    }
   // para multiplicar lo que este en los resgistros que resibi desde id 
    private void multiplicacion() {
        int multi = Arqui3.abInm[0] * Arqui3.abInm[1];
        aloTemporal = multi;
    }
    
    // para dividir lo que este en los resgistros que resibi desde id 
    private void division() {
        int div = Arqui3.abInm[0] / Arqui3.abInm[1];
        aloTemporal = div;
    }
    
    
    // devuelve el indice de la instruccion ejecutando en la tabla de instrucciones
    public int identificarInstruccion(int cod){
        int indice = 0;
        for (int i = 0; i < Arqui3.tablaInstrucciones.length; i++) {
            if(Arqui3.tablaInstrucciones[i][0] == cod){
                indice = i;
                break;
            }
        }
        return indice;
    }
    
    // ejecuta la operacion dependiendo de la instruccion que se esta ejecutando
    public void operacionAEjecutar(){ 
        int t = Arqui3.matrizIRS[1][0];
        if(t == 8 || t == 32|| t == 35 ||t == 43 || t == 11 || t == 3 || t == 22){
            suma(t);
        }
        if(t == 34){
            resta();
                    
        }
        if(t == 12){
            multiplicacion();
        }
        if(t == 14){
            division();
        }
        
        if(t == 22){// verificamos la condicion del SC 
            if(aloTemporal != Arqui3.RL){ // aca le mae debria busca rl de forwarding pero es un despiche 
                aloTemporal = Integer.MIN_VALUE;
            }
        }
        restaFordwarding();
    }
    
    // si resolvi alguna instruccion que escribe un registro que esta en la lista de forwarding lo pone como disponible 
    public void restaFordwarding(){ 
        if(Arqui3.forwardingTabla.size() >= 1) {// si la tabala no esta vacia      
            int indice = Arqui3.buscarEnTablaForwardnigPorPC2(instruccion[4]);// busca la instruccion 
            if(indice != -1){
                if(Arqui3.forwardingTabla.get(indice).getVector()[1] == 3 ){// si tubo que estar listo en 
                    Arqui3.forwardingTabla.get(indice).getVector()[1] = 0;
                     Arqui3.imprimirLista();
                }
            }
        }
    }   
    
    
    public void run(){
        try {
            Arqui3.mutexEX_MEM.acquire(); // espera a que men suelte
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloEX.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         if( Arqui3.seguir[3] <= 0 ){// si no esta detenido en EX              
            instruccion = Arqui3.matrizIRS[1].clone();
            operacionAEjecutar();
            Arqui3.ALUOutput=aloTemporal;
            Arqui3.registroB= Arqui3.abInm[1];
            System.out.println("Etapa EX cargo: "+ Arqui3.pc + Arrays.toString(Arqui3.matrizIRS[1]));
            System.out.println("ALO" + Arqui3.ALUOutput);
            
        }
        Arqui3.seguir[2] -= 1;
        Arqui3.mutexEX_MEM.release();
        Arqui3.mutexEX_ID.release();
    }
    

    
}
