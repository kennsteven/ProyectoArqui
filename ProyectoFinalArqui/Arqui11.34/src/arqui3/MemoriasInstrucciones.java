package arqui3;
import arqui3.Arqui3;
import java.util.Arrays;

public class MemoriasInstrucciones {

    int[][] cacheInstrucciones;
    public static int[] memoriaInstrucciones;
    public static int[] instruccionRecuperada;
    
    public MemoriasInstrucciones(){
         //Los primeros 4 la primer palabra del bloque, los 4 a 8 la segunda
        cacheInstrucciones = new int[10][4];
        memoriaInstrucciones = new int[640];
        instruccionRecuperada = new int[4];
        limpiarMemoria();
        llenarMemoriasInstrucciones();
    }
    
  
    public  void limpiarMemoria(){
        for (int i = 0; i < cacheInstrucciones.length; i++) {
            for (int j = 0; j < cacheInstrucciones[0].length; j++) {
                if(i == cacheInstrucciones.length - 2){
                    cacheInstrucciones[i][j] = -1;
                }else{
                    cacheInstrucciones[i][j] = 1;
                }
            }
        }
        
         for (int i = 0; i < memoriaInstrucciones.length; i++) {
            memoriaInstrucciones[i]= 0;
        }
        
    }
    
    
    public void llenarMemoriasInstrucciones(){
        for(int i = 0; i < memoriaInstrucciones.length;i++ ){
            memoriaInstrucciones[i] = 1;
        }
       // System.out.println(Arrays.toString(memoriaInstrucciones));
    }
    


    public int[][] getCacheInstrucciones() {
        return cacheInstrucciones;
    }


    public int[] getMemoriaInstrucciones() {
        return memoriaInstrucciones;
    }

    public void setCacheInstrucciones(int[][] cacheInstrucciones) {
        this.cacheInstrucciones = cacheInstrucciones;
    }

    public void setMemoriaInstrucciones(int[] memoriaInstrucciones) {
        this.memoriaInstrucciones = memoriaInstrucciones;
    }
    

    
   public int getPalabraDeInstrucciones(int dir, int bloque){
        int palabra;
        if(bloque*8==dir){
            palabra= 0;

        }else{ 
            palabra= 1;
            }
        return palabra;
   }
   
   /*Si el PC=-10 es porque deseamos cargar una burbuja para 
    *limpiar el pipeline
    */
    public int[] traerIstruccion(int pc) {
     
            int bloque = calcularBloque(pc);
            int palabra = calcularPalabra(pc);
            verificarInstruccionCache(bloque, palabra);
        
        return instruccionRecuperada;
        
    }

    private void verificarInstruccionCache(int bloque, int palabra ){
        //se utiliza esta cormula por que es mapeo directo
        int tmp = bloque%4;
   
        if(bloque != cacheInstrucciones[8][tmp]){//Si se da un fallo de cache
            pasarPricipalCacheInstrucciones(bloque);
            Arqui3.seguir[0] = Arqui3.memoriasI.getRetraso();// si hubo fallo de cache 
            cacheInstrucciones[8][tmp] = bloque ;
        }
    
        instruccionRecuperada[0] = cacheInstrucciones[(palabra *4)+0][tmp];
        instruccionRecuperada[1] = cacheInstrucciones[(palabra *4)+1][tmp];
        instruccionRecuperada[2] = cacheInstrucciones[(palabra *4)+2][tmp];
        instruccionRecuperada[3] = cacheInstrucciones[(palabra *4)+3][tmp];
            
    }

    private int calcularBloque(int pc) {
        int bloque = pc/8;//revisar, la divicion redondea
        return bloque;
    }
    
    private int calcularPalabra(int pc) { // devuelve la palabra 0 o 1
        int palabra = pc/4;
        if (palabra%2 == 1){// es impar
            return 1;
        }
            return 0;
    }
    
    private void pasarPricipalCacheInstrucciones(int bloque) {
        for (int i = 0; i < 8; i++) {
            cacheInstrucciones[i][bloque%4] = memoriaInstrucciones[bloque*8+i];       
        }
    }
   
    public void imprimirMemInstrucciones(){
         System.out.println("MEmDatos");
     System.out.println(Arrays.toString(cacheInstrucciones));
    }
    
    public String imprimirCacheInstucciones (){
        String s= new String();
          for (int i = 0; i < cacheInstrucciones.length ; i++) {
             s +=  Arrays.toString(cacheInstrucciones[i])+ "\n" ;
        }   
        return s;
    }
    
    public boolean falloCache(int pc){
        boolean r = false;
        int bloque = calcularBloque(pc);
        int tmp = bloque%4;
        
        //Si se da un fallo de cache
        if(bloque != cacheInstrucciones[8][tmp]){
            r = true;
        }
        return r;
    }

    public int getRetraso() {
        int retraso = (2 * (Arqui3.m + Arqui3.b));
        return retraso;
    }
}
