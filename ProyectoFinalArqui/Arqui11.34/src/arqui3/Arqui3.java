/*
 * Universidad de Costa Rica , 2012
 * Arquitectura de Computadoras 
 * Autores: kenneth alvarado A90300, Jeffrey Castro A91507, Juan Carvajal A91378
 */

package arqui3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.JLabel;

/*Posee las variables globales necesarias para el fordwarding y se encarga de la resolucion del fordwarding*/
public class Arqui3 {

    // matriz para guardar donde vienen los operandos de cada instruccion que se decodifica 
    public static int[][] tablaInstrucciones = {{8, 1, -1, 3}, {32, 1, 2, -1},
        {34, 1, 2, -1}, {12, 1, 2, -1}, {14, 1, 2, -1}, {35, 1, -1, 3},
        {43, 1, -1, 3}, {4, 1, 0, 3}, {5, 1, -1, 3}, {3, 2, -1, 1},
        {2, 1, -1, -1}, {11, 1, -1, 3}, {22, 1, -1, 3}, {63, -1, -1, -1}};
    //instruccion, destino, ciclos a estar listo, de donde sacar ex_m = 1, m_wb = 2 inicialmente
 
    
    // matriz para guardar donde vienen los resgistros detino de cada instruccion , asi como tambien en cuanto tiempo y donde va estar lista 
    public static int[][] tablaRegistrosDestino = {{8, 2, 3, 1}, {32, 3, 3, 1},// la primera indica la instruccion la segunda en que posicion de la instruccion esta el registro destino 
        {34, 3, 3, 1}, {12, 3, 3, 1}, {14, 3, 3, 1}, {35, 2, 4, 3}, {2, -1}, {63, -1}, {43, -1, -1, -1}, {4, -1, -1, -1}, {5, -1, -1, -1},
        {3, 11111, 3, 1},// la instruccion 3 tiene implicitamente el destino en R31
        {11, 2, 4, 2},// especial ya que mete en rl otra vara tambien 
        {22, 2, 4, 4}};// especial
   
    // registros de la aplicacion 
    public static int[] registros = new int[32];// los registro de la maquina 
    public static int RL = 0;   
    
    
    public static int pc = 0; // el contador de programa 
    public static int reloj = 0;
    public static int m = 0;
    public static int b = 0;
    public static int quantum = 0;
    
    // inicializacion de las memorias 
    public static MemoriasInstrucciones memoriasI = new MemoriasInstrucciones();
    public static MemoriasDatos memoriasD = new MemoriasDatos();
    
    // REgistros intermedios entre las etapas 
    public static int[][] matrizIRS = new int[5][5];
    //registros intermedios IF y ID 
    public static int npc = 0;
    // resgiastros intermedios de ID y EX
    public static int[] abInm = new int[3];
    //Registros en intermedios EX y MEM
    public static int ALUOutput = 0;
    public static int registroB = 0;
    //Registros intermedios MEM y WB
    public static int ALUOutput2 = 0;
    public static int LMD = 0;
    // public static int regBEX_Mem=0;
    public static boolean falloCacheDatos = false;
    public static boolean noWriteAllocate = false;
   
    // cola donde metemos los los registros que estan siendo usados por otras instrucciones 
    public static List<Forwarding> forwardingTabla = new ArrayList<Forwarding>();
   // para guardar los contextos de cada hilo 
    public static ManejadorDeContextos manejadorContexto = new ManejadorDeContextos();
    
    // para sincronizacion 
    public static Semaphore mutexAB = new Semaphore(1);
    public static Semaphore mutexIF_ID = new Semaphore(1);
    public static Semaphore mutexEX_ID = new Semaphore(1);
    public static Semaphore mutexEX_MEM = new Semaphore(1);
    public static Semaphore mutexWB = new Semaphore(1);
    public static Semaphore mutexM_WB = new Semaphore(1);
    public static int[] seguir = new int[5];
    public static int[] seguirXForwarding = new int[5];
    public static boolean fallosSeguidos = false;
    public static int[] instruccionActual;
    public static int[] instruccionActualTmp;
    public static boolean meterBurbuja = false;
    public static boolean meterBurbujaXFordwarding = false;
    public static boolean clonar = false;
    public static boolean finalizoHiloContexto = false;
    public static boolean meter63 = false;
    public static boolean terminoQuantum = false;
    
    // contadores  
    public static int contadorQuantum = 0;
    public static int contadorRetrasos = 0;
    public static int duracionhiloactual =0;
    public static int[] cantidadinstruccionesporhilo = new int[70];// para saber el tamaño de cada hilo por lo tanto saber donde inicia , no sabemos el tamaño 
    
    /*****************************************************************************************************/
    /*****************************************************************************************************/
    /*****************************************************************************************************/
    
    
    public Arqui3(){ // constructor 
        for (int i = 0; i < matrizIRS.length ; i++) {
             Arqui3.matrizIRS[i][4]= -1;
        }
    }

    public void empiezaporprincipal(){// le damonos una vuelta a los contextos para que empice por el hilo principal 
        for (int i = 0; i < manejadorContexto.hilos.size()-1; i++) {
            manejadorContexto.cambiarContexto();
        }
    }
    
    
    public void prepararSeguir() {// limpia un vector que sirve para la sincronizacion de los hilos 
        for (int i = 0; i < 5; i++) {
            if (seguir[i] == -1) {
                seguir[i] = 0;
            }
        }
    }

    /*****************************************************************************************************/
    /*****************************************************************************************************/
    /*****************************************************************************************************/
    /*INSTRUCCIONES QUE SE ENCARGAN DE FORWARDING */
    
    public static int buscarEnTablaForwardnigPorPC(int pcAbuscar) {// busca en la tabla de forwardings por el pc y devuelve el indice  
        int respuesta = -1;
       if(pcAbuscar != -1){
            for (int i = forwardingTabla.size() - 1; i >= 0; i--) {//de ABAJO ARRIBA
                if (forwardingTabla.get(i).getVector()[3] == pcAbuscar && Arqui3.forwardingTabla.get(i).getVector()[0]!=-1 ) { // si el pc es el mismo 
                    respuesta = i;
                    break;
                }
            }
       }
        return respuesta;
    }

   public static int buscarEnTablaForwardnigPorPC2(int pcAbuscar) {// busca en la tabla de forwardings por el pc y devuelve el indice  
        int respuesta = -1;
       if(pcAbuscar != -1){
            for (int i = 0; i<forwardingTabla.size(); i++) {//de ABAJO ARRIBA
                if (forwardingTabla.get(i).getVector()[3] == pcAbuscar && Arqui3.forwardingTabla.get(i).getVector()[0]!=-1) { // si el pc es el mismo 
                    respuesta = i;
                    break;
                }
            }
       }
        return respuesta;
    }
   
    
    //buscar por registro necesitado
    public static int buscarEnListaDeForwarding(int registroNecesitado, int[] instruccion) {  //Devuelve el indice en la tabla de forwarding que contiene el registro que necesitamos 
        int respuesta = -1;
        System.out.println("El registro necesitado es: " + registroNecesitado);
        for (int i = forwardingTabla.size() - 1; i >= 0; i--) {//de abajo a arriba
            if (forwardingTabla.get(i).getVector()[0] == registroNecesitado && instruccion[4] != forwardingTabla.get(i).getVector()[3]) {
                respuesta = i;
                break;
            }
        }
        return respuesta;
    }

    
    public static int cargarDesdeFordwarding(int indiceTmp1) {// Devuelve el valor de lugar donde se le solicite 
        int respuesta = Integer.MIN_VALUE;
        int tmp = 0;
        if (forwardingTabla.get(indiceTmp1).getVector()[1] == 0) {// para saber si esta listo temp
            tmp = forwardingTabla.get(indiceTmp1).getVector()[2];

            if (tmp == 1) { // esta en el ALO
                respuesta = ALUOutput;
            }
            if (tmp == 2) {// esta en el ALO de men 
                respuesta = ALUOutput2;
            }
            if (tmp == 3) { //Sacar de LMD
                respuesta = LMD;
            }
            if (tmp==4){ // es especial solo para la SC
                if (ALUOutput2==Integer.MIN_VALUE){
                    respuesta=0;   
                }else{   
                  respuesta= memoriasD.traerDato((ALUOutput2-640)/4);
                }
            }
            if(tmp  == 31){// caso especial *
                respuesta = registros[30];  
            }
        } else {
            //retrasar por fordwarding
            respuesta = Arqui3.seguirXForwarding[2];
            respuesta = Integer.MIN_VALUE;
        }
        return respuesta;
    }

    
    // recibe el registro   que desea buscas en la table de forwarding 
    // devuelve el indice 
    public static int saberForwardingID(int instruccion[], int indiceInstruccion) {// recibe el num de reg que deseo buscar
        int respuesta = -100;
        int indice = 0;
        boolean r = false;
        //Seber cual instruccion es, y si necesita como operados solo A, o A y B
        for (int i = 0; i < Arqui3.tablaInstrucciones.length; i++) {        
            if (Arqui3.tablaInstrucciones[i][0] == instruccion[0]) {
                indice = i;
                break;
            }      
        }
      //  System.out.println("Saber Fordwarding " + indice + " el A o B " + indiceInstruccion + Arrays.toString(instruccion));
        if (Arqui3.tablaInstrucciones[indice][indiceInstruccion] != -1) {//if para saber si ocupa el registro
            respuesta = buscarEnListaDeForwarding(instruccion[tablaInstrucciones[indice][indiceInstruccion]], instruccion);
        }
        return respuesta;
    }

    
    // cada vez entra una instruccion si este escribe en un registro el se encarga de meterlo a la colas de resgistros en forwarding 
    public void llenarListaFordwarding(int[] instruccion) {
        Forwarding f = new Forwarding();
        int indice = -1;
        System.out.println("Finalizo contexto " + finalizoHiloContexto + "termino contexto "+terminoQuantum );
        if ((!finalizoHiloContexto && !terminoQuantum) || !terminoQuantum ) {
           // System.out.println("La instrucion llenarFordwarding " + Arrays.toString(instruccion));
            //Buscamos en la tabla donde se encuentran los datos de cada instruccion con respecto al fordwarding
            for (int i = 0; i < Arqui3.tablaRegistrosDestino.length; i++) {
                if (Arqui3.tablaRegistrosDestino[i][0] == instruccion[0]) {
                    indice = i;
                    break;
                }
            }
            if (indice != -1){
            if (tablaRegistrosDestino[indice][1] != -1) { // i lo encontro 
                if(tablaRegistrosDestino[indice][0] == 3 ){
                    f.getVector()[0] = 31;//El registro destino
                    f.getVector()[1] = 4;//En cuanto tiempo esta listo
                    f.getVector()[2] = 4;//De donde sacarlo
                    f.getVector()[3] = pc - 4;//De donde sacarlo    
                }else{
                    f.getVector()[0] = instruccionActual[tablaRegistrosDestino[indice][1]];//El registro destino
                    f.getVector()[1] = tablaRegistrosDestino[indice][2];//En cuanto tiempo esta listo
                    f.getVector()[2] = tablaRegistrosDestino[indice][3];//De donde sacarlo
                    f.getVector()[3] = pc - 4;//De donde sacarlo
                }
                
                if (forwardingTabla.size() == 5) {
                    forwardingTabla.remove(0);
                    forwardingTabla.add(f);
                } else {
                    forwardingTabla.add(f);
                }
            }}
        }


    }
    

    /*****************************************************************************************************/
    /*****************************************************************************************************/
    /*****************************************************************************************************/
    /***PARA LA SINCRONIZACION DE LOS HILOS**/
    
    
    // pasa la instrucciones entre 
    public void pasarInstrucciones(int[] instruccionIf) {
        //System.out.println("Aca pasarInstr seguir0 = " + seguir[0] + " seguir3 = " + seguir[3]);

        if (meterBurbujaXFordwarding) {
           // System.out.println("Aca esta en el if 0");
            Arqui3.matrizIRS[4] = Arqui3.matrizIRS[3].clone();
            Arqui3.matrizIRS[3] = Arqui3.matrizIRS[2].clone();
            Arqui3.matrizIRS[2] = Arqui3.matrizIRS[1].clone();
            Arrays.fill(Arqui3.matrizIRS[1], 0);
            Arqui3.matrizIRS[1][4]= -1;
            meterBurbujaXFordwarding = false;
            clonar = true;
            instruccionActualTmp = instruccionIf.clone();
        } else {
            if (seguir[0] == 0 && seguir[3] == 0) {
                //System.out.println("Aca esta en el if 1");
                Arqui3.matrizIRS[4] = Arqui3.matrizIRS[3].clone();
                Arqui3.matrizIRS[3] = Arqui3.matrizIRS[2].clone();
                Arqui3.matrizIRS[2] = Arqui3.matrizIRS[1].clone();
                Arqui3.matrizIRS[1] = Arqui3.matrizIRS[0].clone();
                Arqui3.matrizIRS[0] = instruccionIf.clone();
                //System.out.println("El vector es " + Arrays.toString(instruccionIf));
                if (fallosSeguidos) {
                    matrizIRS[0] = instruccionActualTmp.clone();
                }
                //revisar si debe estar aqui o dentro del if
                System.out.println("El mae es: " + Arrays.toString(instruccionActualTmp));
                llenarListaFordwarding(instruccionActual);
                fallosSeguidos = false;
            }

            //No se puede seguir en IF
            if (seguir[0] != 0 && seguir[3] == 0) {
                System.out.println("Aca esta en el if 2");

                Arqui3.matrizIRS[4] = Arqui3.matrizIRS[3].clone();
                Arqui3.matrizIRS[3] = Arqui3.matrizIRS[2].clone();
                Arqui3.matrizIRS[2] = Arqui3.matrizIRS[1].clone();
                Arqui3.matrizIRS[1] = Arqui3.matrizIRS[0].clone();
                Arrays.fill(matrizIRS[0], 0);
                Arqui3.matrizIRS[0][4]= -1;
                if (seguir[0] == 3) {// para que no le 
                    instruccionActualTmp = instruccionIf.clone();
                    System.out.println("El mae es: " + Arrays.toString(instruccionActualTmp));
                    //llenarListaFordwarding(instruccionActual);
                    fallosSeguidos = true;
                }
            }

            //No se puede seguir en if y en mem o no se puede seguir solamente en mem
            if (seguir[3] != 0) {

                Arqui3.matrizIRS[4] = Arqui3.matrizIRS[3].clone();
                Arrays.fill(matrizIRS[3], 0);
                Arqui3.matrizIRS[3][4]= -1;

                System.out.println("Aca esta en el if 3");
                if (fallosSeguidos) {// para que no le 
                    matrizIRS[0] = instruccionActualTmp.clone();
                    //llenarListaFordwarding(instruccionActual);
                }
                fallosSeguidos = false;
            }
        }


    }
    
    // metodo principal , hace un ciclo de mips 
    // crea los hilos, los pone a ejecutar el orden wb -> if
    // pasa los resgistros intermedios
    // cambia de contextos si termino el programa o el quantum 
    
    public void iteracion() throws InterruptedException {
        
        // crea los hilos 
        HiloIF if1 = new HiloIF();
        Thread t1 = new Thread(if1);
        HiloID id = new HiloID();
        Thread t2 = new Thread(id);
        HiloEX ex = new HiloEX();
        Thread t3 = new Thread(ex);
        HiloMem mem = new HiloMem();
        Thread t4 = new Thread(mem);
        HiloWB wb = new HiloWB();
        Thread t5 = new Thread(wb);
        
        // detienen lo semaforos 
        mutexEX_ID.acquire();
        mutexIF_ID.acquire();
        mutexEX_MEM.acquire();
        mutexM_WB.acquire();
        
        // empezamos los hilos 
        t5.start();
        t4.start();
        t3.start();
        t2.start();
        t1.start();
        
        // esperamos a que todos terminen 
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
       
        prepararSeguir();
 
        pasarInstrucciones(instruccionActual);
        //imprimirLista();
     
       
        if (seguir[0] == 0 && seguir[3] == 0) {// si no esta detenido por fallo de cache 
          contadorQuantum--;
        }
        System.out.println("el quantum " + contadorQuantum);
        if (instruccionActual[0] == 63 && contadorQuantum > 0) {// si el hilo finalizo 
            duracionhiloactual= quantum - contadorQuantum;
            contadorQuantum = 0;
            finalizoHiloContexto = true;
            meter63 = true;
        }
        if (contadorQuantum <= 0) {// quiere dcir que termino en quantum y tiene que meter burbujas para limpiar pipeline 
            terminoQuantum = true;
            if(!meter63){
                meterBurbuja = true;
                if (contadorQuantum < -4 ) {// ya ha colocado un burbuja anteriormente
                  if (manejadorContexto.hilos.size()-1 !=0){ // si metio sufucientes burbujas para limpiar el pipeline 
                        manejadorContexto.cambiarContexto();
                        contadorQuantum = quantum;
                        meterBurbuja = false;
                        duracionhiloactual =quantum;// para que vuelva a sumar 
                  }else {
                      if(manejadorContexto.hilos.size()==1 && finalizoHiloContexto==false ){
                           contadorQuantum=quantum;
                           meterBurbuja = false;
                      }else{
                        manejadorContexto.sacarContexto(0);
                        Contexto viejo=manejadorContexto.hilos.get(0);
                        manejadorContexto.hilos.remove(0);
                        manejadorContexto.hilosFinalizados.add(viejo);
                      }
                  }    
                }
            }
            meter63 = false;        
        }
    }
    
    public static void imprimirLista() {// para probar imprime la cola de forwardings 

        System.out.println("\n \nLa lista es: ");
        System.out.println("{");
        for (int i = 0; i < forwardingTabla.size(); i++) {
            Forwarding f;// = new Forwarding();
            f = forwardingTabla.get(i);
            System.out.println(Arrays.toString(f.getVector()));
        }
        System.out.println("}");
    }
 }
