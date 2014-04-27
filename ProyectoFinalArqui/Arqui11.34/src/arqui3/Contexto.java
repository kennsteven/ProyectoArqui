/*
 * Universidad de Costa Rica , 2012
 * Arquitectura de Computadoras 
 * Autores: kenneth alvarado A90300, Jeffry Castro A91507, Juan Carvajal A91378
 */


package arqui3;

// clase que utilizamos como struc para guardar las cosas de cada hilo

public class Contexto {
// Clase que almacena todo lo necesario para realizar el cambio de contexto
// incluyendo los 31 registros junto con el registro l el cual es lo utiliza 
// solamente LL y SC , ademas agrega el pc del programa, ultimo al cambiar contexto 
// del hilo principal junto con la duracion 
//    
    int registros[]=new int [32]; 
    int pc;
    int rl;
    int idHilo;
    int duracion;  
}
