/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

import arqui3.Arqui3;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeff
 */
public class HiloMem implements Runnable{
   
   int []instruccionID;
    
   public HiloMem(){
     instruccionID = Arqui3.matrizIRS[2].clone();
   }
   
   public int getPosicion(int pos){
       int posicion=(pos-640)/4;//339
       return posicion;
   }
   
   
   public boolean validarAlineamiento(int direccion){
        System.out.println("Validando el aluOutPut: "+direccion);
       boolean esValido=false;
       if(direccion%4==0 && direccion >= 620   && direccion < 2048){
           esValido=true;
       }
       
       return esValido;
   }
    
   
   
   public void decodificar() throws InterruptedException{
       //int direccion=Arqui3.ALUOutput;
       Arqui3.falloCacheDatos= false;
       int direccion=-1;
       int codigoInstrucion =instruccionID[0];
       System.out.println("entre switch mi ins: " +codigoInstrucion);
       
       if(codigoInstrucion == 11 || codigoInstrucion == 35 || codigoInstrucion == 43 ||  codigoInstrucion == 22 ){// no es una que ocupa men 
              if(validarAlineamiento(Arqui3.ALUOutput) || Arqui3.ALUOutput != Integer.MIN_VALUE){// si es valida 
                   direccion=getPosicion(Arqui3.ALUOutput);
                   switch(codigoInstrucion){  
                        case 11: // LoadLink
                                Arqui3.LMD=Arqui3.memoriasD.traerDato(direccion);
                                System.out.println("cargando lo que hay en la direccion "+direccion+" en lmd " + Arqui3.memoriasD.traerDato(direccion));
                                 Arqui3.RL = Arqui3.ALUOutput;// eso se deberia hacer en wb pero habria que agragar ottro caso de forwarding 
                                break;
                        case 35: // Load normal
                                System.out.println("cargando lo que hay en la direccion "+direccion+" en lmd");
                                Arqui3.LMD=Arqui3.memoriasD.traerDato(direccion);    
                                break;
                        case 43://Store, registro=dato, aluOutput=Direccion
                                System.out.println("va meter en : "+direccion+"  esto : "+ Arqui3.registros[instruccionID[2]]);
                                Arqui3.memoriasD.almacenarDato(direccion,Arqui3.registros[instruccionID[2]]);
                                break;

                        case 22://Store conditional REvisar el Store conditional
                                int tmp = Arqui3.buscarEnListaDeForwarding(instruccionID[2],instruccionID);
                                if (tmp !=-1 && Arqui3.forwardingTabla.get(tmp).getVector()[3] < instruccionID[4]){// si esta en la tabla de forwarding y es una adelante mia  
                                      Arqui3.memoriasD.almacenarDato(direccion,Arqui3.ALUOutput2);
                                    
                                }else{
                                    Arqui3.memoriasD.almacenarDato(direccion,Arqui3.registros[instruccionID[2]]);
                                }
                                 
                            break;

                 }
              }else{
                       System.out.println("Error de alineamiento");
              }
                   restaFordwarding();
               
       }
        Arqui3.ALUOutput2=Arqui3.ALUOutput;// pasamos el alo al siguiente
        //System.out.println("es una instruccion de burbuja o alguna que no use mem");
        int dondeEstaEnFordwarding = Arqui3.buscarEnTablaForwardnigPorPC2(instruccionID[4]);
        if ( dondeEstaEnFordwarding!= -1){ // si esta en la tabla de forwardings

            if(Arqui3.forwardingTabla.get(dondeEstaEnFordwarding).getVector()[2] == 1 && //Estuvo lista en ALO1
                    Arqui3.forwardingTabla.get(dondeEstaEnFordwarding).getVector()[1] == 0){//Si ya esta lista
                // actualizamos la tabla de fordwardings , para que el hilo que la ocupe sepa que esta en alo2 
                Arqui3.forwardingTabla.get(dondeEstaEnFordwarding).getVector()[2]++;
            }
        }
        
        
        
       
       
   }
   
    public void restaFordwarding(){
        //metodo para ver si resta al la instruccion o no
        if(Arqui3.forwardingTabla.size() >= 1) {
           
            int indice = Arqui3.buscarEnTablaForwardnigPorPC2(instruccionID[4]);
            System.out.println("El indice es: " + indice);
            if(indice != -1 ){
                //Tambien hay que verificar que no dependa de una anterior 
                System.out.println("La pos en el vector es: " + Arqui3.forwardingTabla.get(indice).getVector()[1]);

                if(Arqui3.forwardingTabla.get(indice).getVector()[1] == 4){
                    Arqui3.forwardingTabla.get(indice).getVector()[1] = 0;
                     System.out.println("SE METIO A CAMMBIAR EL VALOR DE FORD");
                     Arqui3.imprimirLista();
                }
            }
            
        }

    }  
    
    @Override
    public void run() {
        try {
            Arqui3.mutexM_WB.acquire(); // si WB ya solto  la jugada 
  
            if(Arqui3.seguir[3] <= 0){ // si no hay fallo de cache de datos y no esta deternido en EX
               if (instruccionID[0]!=0){
                    System.out.println("estoy en MEM  ");
                    decodificar();
                    System.out.println("despues de decodificar "+Arqui3.falloCacheDatos);
                    if(Arqui3.falloCacheDatos){
                        Arqui3.seguir[3] = Arqui3.memoriasD.getRetraso();
                       
                    }
                }
            }            
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloMem.class.getName()).log(Level.SEVERE, null, ex);
        }
        Arqui3.seguir[3] -= 1;
        Arqui3.mutexM_WB.release();
        Arqui3.mutexEX_MEM.release();
    }
}
