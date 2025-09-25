import java.util.*;

public class PageFault {

  /**
   * El algoritmo de reemplazo de paginas para el simulador de gestion de memoria.
   * Este metodo es llamado cuando ocurre un fallo de pagina.
   *
   * ALGORITMO IMPLEMENTADO: First-Fit
   *
   * Logica:
   * 1. Recorrer el vector de paginas virtuales (`mem`) desde el principio (pagina 0).
   * 2. La PRIMERA pagina que se encuentre con una direccion fisica valida (page.physical != -1)
   * sera seleccionada como la victima a reemplazar.
   * 3. No es necesario seguir buscando una vez que se encuentra la primera victima.
   *
   * @param mem es el vector que contiene las paginas en memoria.
   * @param virtPageNum es el numero total de paginas virtuales.
   * @param replacePageNum es la pagina que necesita ser cargada en memoria.
   * @param controlPanel es la interfaz grafica para actualizarla.
   */
  public static void replacePage(Vector mem, int virtPageNum, int replacePageNum, ControlPanel controlPanel) {
    int victimId = -1; // Usaremos esta variable para guardar el ID de la pagina victima.

    // 1. Bucle para encontrar a la primera victima.
    // Buscamos desde la pagina virtual 0 en adelante.
    for (int i = 0; i < virtPageNum; i++) {
      Page currentPage = (Page) mem.elementAt(i);
      // ?Esta pagina esta actualmente en la memoria fisica?
      if (currentPage.physical != -1) {
        // !Si! La encontramos. Esta es nuestra victima.
        victimId = i;
        // 2. Rompemos el bucle. No necesitamos buscar mas.
        break;
      }
    }

    // Si por alguna razon no se encontro ninguna pagina para reemplazar (memoria vacia),
    // lo cual no deberia ocurrir en un fallo de pagina, salimos para evitar errores.
    if (victimId == -1) {
      return;
    }

    // 3. Obtenemos los objetos de la pagina victima y la nueva pagina.
    Page victimPage = (Page) mem.elementAt(victimId);
    Page newPage = (Page) mem.elementAt(replacePageNum);
    
    // Guardamos el numero del marco fisico que la victima esta usando.
    int physicalFrame = victimPage.physical;

    // 4. Actualizamos la interfaz grafica.
    // Quitamos la pagina victima de la vista de memoria fisica.
    // Se usa physicalFrame para indicar el marco fisico correcto a limpiar.
    controlPanel.removePhysicalPage(physicalFrame);
    
    // 5. Realizamos el reemplazo.
    // La nueva pagina ahora ocupara el marco fisico de la victima.
    newPage.physical = physicalFrame;
    
    // Agregamos la nueva pagina a la vista grafica, en el marco que acabamos de liberar.
    controlPanel.addPhysicalPage(replacePageNum, newPage.physical);

    // 6. Reseteamos los valores de la pagina victima, ya que ahora esta fuera de la memoria fisica.
    victimPage.inMemTime = 0;
    victimPage.lastTouchTime = 0;
    victimPage.R = 0;
    victimPage.M = 0;
    victimPage.physical = -1; // MUY IMPORTANTE: Marcar que ya no esta en memoria.
  }
}