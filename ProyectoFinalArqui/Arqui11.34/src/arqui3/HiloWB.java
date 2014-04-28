/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

/**
 *
 * @author Kenneth Alvarado
 */
class HiloWB implements Runnable{
    int valorAescribir =0;
    int []instruccion;
    int codInstEjecutando ;
    public static int [][] registrosDestino = {{8,2},{32,3},{34,3},{12,3},{14,3},{35,2},// meten lo de alo2 en el registro
            {3,-1},// especial por que mete en el r31 lo que habia en el pc 
            {11,2},// mete en registrolo que hay en memoria (lmd)
            {22,2}};// dependiendo de la condicion mete un 0
    
    public HiloWB(){
        instruccion = Arqui3.matrizIRS[3].clone();
        codInstEjecutando = Arqui3.matrizIRS[3][0];
    }
    public int identificarInstruccion(int cod){
       
        for (int i = 0; i < registrosDestino.length; i++) {
            if(registrosDestino[i][0] == cod){
              return i;
            }
        }
        return -1;
    }
    
    private void buscaValorAguardar() {
       int registrodestino =Arqui3.matrizIRS[3][2];// donde viene el registro destino de SC
       valorAescribir=  Arqui3.registros[registrodestino];// le mete lo que antes tenia en el registro destino de la sc 
        
        if(codInstEjecutando == 35 || codInstEjecutando == 11){ // si son load metemos lo que hay en en lmd 
            valorAescribir = Arqui3.LMD;
        }else { // si sin las normales 
            if (codInstEjecutando != 22 ){valorAescribir = Arqui3.ALUOutput2;}
        }  
        if (codInstEjecutando== 22 && Arqui3.ALUOutput2 == Integer.MIN_VALUE){ // esto es por de el SC dependiendo de la condicion mete un 0 en el rdestino
            valorAescribir=0;
        }
        
    }
    
 
   public void escribir(){
       
       if(codInstEjecutando == 11){
           Arqui3.RL = Arqui3.ALUOutput2;
       }
       
       if(codInstEjecutando != 3){ //no es el JAL
            int ind = identificarInstruccion(Arqui3.matrizIRS[3][0]);
                if (ind != -1 ){ // para saber si no es una instrucciones que ocupa escribir 
                    int registrodestino =Arqui3.matrizIRS[3][registrosDestino[ind][1]];
                    int temporal=  Arqui3.registros[registrodestino];
                    
                    Arqui3.registros[registrodestino] = valorAescribir;
                       
                }
        }else {
                 //JAL hay que guardar en registro 31 el valor del pc de la instruccion
                 Arqui3.registros[31] = instruccion[4] + 4; // revisar cual pc estamos metiendo
        } 
       
   }
    
    
    public void run(){
      //  System.out.println("Entro WB");
        buscaValorAguardar();
        escribir();
        Arqui3.seguir[4] -= 1;
        if(Arqui3.forwardingTabla.size() > 0 && instruccion[4] != -1){
            int ind = Arqui3.buscarEnTablaForwardnigPorPC2(instruccion[4]); // de arriba 
            if(ind != -1 && Arqui3.forwardingTabla.get(ind).getVector()[1]== 0 ) {
                System.out.println(ind);
                Arqui3.forwardingTabla.get(ind).getVector()[0] = -1;
                
            }
        }
        
        Arqui3.mutexM_WB.release();
    }

   
    
}
