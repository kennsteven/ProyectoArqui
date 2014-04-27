/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JJ
 */
public class RegistrosIntermedios {
     public static int lmd =0;
    public static int aloEX = 0;
    public static int aloMem = 0;
    

    public static int[] registros = new int[31];
   
    public static int[][] matrizIRS = new int[5][4];
    
    public static int pc = 0;
    
    public static int rl = 0;
    public static int rx = 0;
    
    // resgiastros intermedios de ID y EX//
     public static int[] abInm = new int[3];
     
    /*Mias*/
    public static boolean puedoLeerALUOutput=true;// Bandera
    public static boolean puedoLeerLMD=true;// Bandera
    public static boolean puedoLeerRegBEX_Mem=true;// Bandera
    //Registros intermedios MEM y WB
    public static int ALUOutput=0;
    public static int ALUOutput2=0;
    public static int LMD=0;
    public static int regBEX_Mem=0;
    public static boolean falloCacheDatos=false;
    public static boolean noWriteAllocate=false;
    Semaphore mutexAB = new Semaphore(1);
    Semaphore mutexID = new Semaphore(1);
    Semaphore mutexEX = new Semaphore(1);
    Semaphore mutexWB = new Semaphore(1);
    Semaphore mutexM = new Semaphore(1);
    
    public void getAB() throws InterruptedException{
        mutexAB.acquire();
    }
        
   public void setAB() throws InterruptedException{
        mutexAB.release();
   }
    
    public void setID() throws InterruptedException{
        mutexAB.release();
   }
    
    public void inicializarSemaforos(){
        try {
          //  mutexABEx.acquire();
            //al soltar mutexABEx activa el del mutexABId
          //  mutexABId.acquiere();
            mutexID.acquire();
            mutexEX.acquire();
            mutexWB.acquire();
            mutexM.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(RegistrosIntermedios.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void soltarOrden(){
        mutexEX.release();
        HiloIF if1 = new HiloIF();
        Thread t1 = new Thread(if1);
        t1.start();
        mutexID.release();
        
        HiloID id = new HiloID();
        Thread t2 = new Thread(id);
        t2.start();
        
        HiloEX ex = new HiloEX();
        Thread t3 = new Thread(ex);
        t3.start();
        
        HiloMem mem = new HiloMem();
        Thread t4 = new Thread(mem);
        t4.start();
        
        HiloWB wb = new HiloWB();
        Thread t5 = new Thread(wb);
        t5.start();
        
    }
}
