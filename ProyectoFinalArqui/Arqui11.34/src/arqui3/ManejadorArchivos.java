/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arqui3;

import arqui3.Arqui3;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

/**
 *
 * @author jeff
 */
public class ManejadorArchivos {
 
    int contadorPalabra=0;
    Arqui3 mio;
    int cantHilos;
    int hiloLeyendo=0; 
    
    public ManejadorArchivos(Arqui3 mae, int cantidadHilos){
        mio = mae ;
        cantHilos=cantidadHilos;
    }
    
	/*Lee los datos archivos de un directorio*/
    public void cargarDirectorio(String directorio)throws Exception{
    File[] files = new File(directorio).listFiles();
    File ultimo =new File("");
        for (int i=0; i < files.length;i++) {
           File f= files[i]; 
            if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()){
                if(f.getCanonicalPath().contains("hilo-pr")){
                 System.out.println("entre a hilo ultimo");   
                  ultimo=f;
                }else{
                  if(cantHilos>0){
                    cargarArchivo(f.getCanonicalPath());
                    System.out.println(f.getCanonicalPath());
                    cantHilos--;
                  }
                }
            }
        }
        cargarArchivo(ultimo.getCanonicalPath());
         System.out.println("Memoria de Instucciones = "+ Arrays.toString(Arqui3.memoriasI.memoriaInstrucciones));
    }
    
	/*Lee el contenido de un solo archivo*/
    public void cargarArchivo(String dataDir) throws Exception {
        File archivo = new File(dataDir);
        String linea="";
       
        if (!archivo.isDirectory() && !archivo.isHidden() && archivo.exists() && archivo.canRead() ){
                FileReader fr = new FileReader (archivo);
                BufferedReader br = new BufferedReader(fr);
            // ultimo=fr. 
                int tamañodehilo=0;// para guardar el numero de lineas de cada archivo para saber donde termina 
                linea=br.readLine();
                 while(linea != null){
                    cargarInstruccion(linea);
                    linea=br.readLine();
                    tamañodehilo++;
                }
                 mio.cantidadinstruccionesporhilo[hiloLeyendo]=tamañodehilo; 
            hiloLeyendo ++;
            }else{
                System.out.println("No lei archivo: "+dataDir);
            }
        
    //  int resultado=w.numDocs();
    // w.close();
      //  return resultado;
    }
    
    public void cargarInstruccion(String linea){
        String[] tokens=linea.split(" ");
        for(int i=0; i< 4; i++){
            int posicion=contadorPalabra*4+i;    
            mio.memoriasI.memoriaInstrucciones[posicion]=Integer.parseInt(tokens[i]);    
              
        }
        contadorPalabra++;
    }
    
    ///public static void main(String []args)throws Exception{
     // ManejadorArchivos ma=new ManejadorArchivos();
   //  ma.cargarDirectorio("2012-2-CI1323-TG");
    //}
}
