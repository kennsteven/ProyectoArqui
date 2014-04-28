/*
 * Universidad de Costa Rica , 2012
 * Arquitectura de Computadoras 
 * Autores: kenneth alvarado A90300, Jeffry Castro A91507, Juan Carvajal A91378
 */

package arqui3;


// clase para guardar los registros que estan siendo usados por otras instrucciones 
class Forwarding { 
    
    private int [] vector = new int[4];
        //pos 0 = el registro que usa la operacion
        //pos 1 = si esta listo = 0, otro numero si no
        //pos 2 = de donde hay que sacar la vara:
            //1 = ALO, 2 = AL2, 3 = LMD
        //pos 3 = pc

    
    public int[] getVector() {
        return vector;
    }
    
}
