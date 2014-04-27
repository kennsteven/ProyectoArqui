package arqui3;
import arqui3.Arqui3;
import java.util.Arrays;

public class MemoriasDatos {
    int[][] cacheDatos;
    public static int[] memoriaDatos;

    
    
    public MemoriasDatos(){
        cacheDatos = new int[4][4];
        memoriaDatos = new int[352];// logicamente la posicion 0 equivale a 640 a 2048
        limpiarMemoriaDatos();
        limpiarMemoriasCache();
    }
    

    public  void limpiarMemoriasCache(){
        for (int i = 0; i < cacheDatos.length; i++) {
            for (int j = 0; j < cacheDatos[0].length; j++) {
                cacheDatos[i][j] = 0;
                if(i == 2){
                    cacheDatos[i][j] = -1;
                }
            }
        }
    }
    
    public  void limpiarMemoriaDatos(){
        for (int i = 0; i < memoriaDatos.length; i++) {
            memoriaDatos[i] = 1;
        }
    }

    public int[][] getCacheDatos() {
        return cacheDatos;
    }

    public int[] getMemoriaDatos() {
        return memoriaDatos;
    }

    public void setCacheDatos(int[][] cacheDatos) {
        this.cacheDatos = cacheDatos;
    }

    public void setMemoriaDatos(int[] memoriaDatos) {
        this.memoriaDatos = memoriaDatos;
    }

   public void almacenarDato(int dir, int dato){
     int bloque=calcularBloque(dir);
     int enCahe=estaEnCache(dir,bloque);
     Arqui3.falloCacheDatos=true;
       if(enCahe<0){//No esta en cache No write allocate
       
           Arqui3.noWriteAllocate=true;
           escribirMem(dir,dato);
       }else{ //esta en cache
      
           escribirCacheDatos(dir,dato);
           escribirMem(dir,dato);
       }
       imprimirCacheDatos();
       imprimirMemDatos();
    }
   //Revisar no estoy seguro si falta hacer mas
   public void escribirMem(int dir,int dato){
       memoriaDatos[dir]=dato;
    }
  /********************************************************************/ 
    
    public int traerDato(int direccion ){
     int bloque=calcularBloque(direccion);
     int dato=estaEnCache(direccion,bloque); 
     if(dato<0){
         Arqui3.falloCacheDatos=true;
         dato=getDatoMem(direccion,bloque);
      }else{
         Arqui3.falloCacheDatos=false;

     }
     escribirCacheDatos(direccion, dato);
        return dato;
    }
  /**************************************************************************/  
   public int estaEnCache(int dir, int bloque){
        int resultado=-1;
        for(int i=0; i < cacheDatos.length; i++){
            if(cacheDatos[2][i]==bloque){
            resultado=cacheDatos[getPalabra(dir, bloque)][i];
            }
        }
        return resultado;
    }
  /**************************************************************************/  
   public void escribirCacheDatos(int dir, int dato){
    //invalido bloque
       int bloque=calcularBloque(dir);
       int columna=bloque%4;
       cacheDatos [3][columna] = 0;//mapeo directo
       cacheDatos[getPalabra(dir, bloque)][columna] = dato;//mapeo directo
       
   }
    
/**************************************************************************/
    public int getDatoMem(int dir, int bloque){
        int resultado;
        int columna=bloque%4;// obtiene el indice
        int palabra=-1;
        int value=dir%2;
        if(value==0){
            cacheDatos[0][columna] = memoriaDatos[dir];       
            cacheDatos[1][columna] = memoriaDatos[dir+1]; 
            cacheDatos[2][columna] = bloque;
            cacheDatos[3][columna] = 1; 
        }else{
            cacheDatos[0][columna] = memoriaDatos[dir-1];       
            cacheDatos[1][columna] = memoriaDatos[dir]; 
            cacheDatos[2][columna] = bloque;
            cacheDatos[3][columna] = 1;
        }
                
        resultado=cacheDatos[getPalabra(dir, bloque)][columna];
        return resultado;
    }
       
   
        
   public int getPalabra(int dir, int bloque){
        int palabra=0;
        if(bloque*2==dir){
            palabra= 0;
        }else{ 
            palabra= 1;
            }
        return palabra;
   }
   
  
    private int calcularBloque(int direccion) {
        int bloque = direccion/2;//revisar, la divicion redondea
        return bloque;
    }

    
    public void imprimirMemDatos(){
         System.out.println("MEmDatos");
     System.out.println(Arrays.toString(memoriaDatos));
    }
    
    public String imprimirCacheDatos(){
        String lala= new String();
        for (int i = 0; i < cacheDatos.length ; i++) {
             lala += Arrays.toString(cacheDatos[i])+ "\n";
  
        }  
        return lala;
    }
    
    public int getRetraso() {
        int retraso = (2 * (Arqui3.m + Arqui3.b));
        return retraso;
    }
    
    
}
