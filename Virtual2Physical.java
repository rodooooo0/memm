import java.util.Vector;

// La clase Virtual2Physical contiene metodos para traducir
// direcciones de memoria virtual a numeros de pagina.
public class Virtual2Physical
{
  // pageNum toma una direccion de memoria virtual y devuelve el numero de pagina virtual
  // al que pertenece esa direccion.
  public static int pageNum ( long memaddr , int numpages , long block )
  {
    int i = 0;
    long high = 0;
    long low = 0;
    
    // Itera a traves de todas las paginas virtuales posibles
    for (i = 0; i <= numpages; i++)
    {
      // Calcula el limite inferior de la pagina actual
      low = block * i;
      // Calcula el limite superior de la pagina actual
      high = block * ( i + 1 );
      // Comprueba si la direccion de memoria se encuentra dentro de los limites de la pagina
      if ( low <= memaddr && memaddr < high )
      {
        // Si se encuentra, devuelve el numero de la pagina (i)
        return i;
      }
    }
    // Si la direccion no se encuentra en ninguna pagina valida, devuelve -1 para indicar un error
    return -1;
  }
}