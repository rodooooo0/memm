import java.lang.Thread;
import java.io.*;
import java.util.*;

// La clase Kernel es el corazon del simulador. Controla el ciclo de ejecucion principal,
// lee comandos y configuracion, y gestiona el espacio de memoria virtual.
public class Kernel extends Thread {

  // El numero de paginas virtuales es fijo en 63 por dependencias de la GUI.
  private static int virtPageNum = 63;

  // SEGMENTOS: Define el mapeo de paginas virtuales a segmentos.
  // Es una nueva caracteristica para la segmentacion.
  public static final int[][] SEGMENTS = {
    // S1: paginas 0-2
    {0,1,2},
    // S2: paginas 3-8
    {3,4,5,6,7,8},
    // S3: paginas 9-14
    {9,10,11,12,13,14},
    // S4: paginas 15-22
    {15,16,17,18,19,20,21,22},
    // S5: paginas 23-31
    {23,24,25,26,27,28,29,30,31}
  };

  private String command_file;
  private String config_file;
  private ControlPanel controlPanel;
  private Vector memVector = new Vector(); // Actua como la tabla de paginas
  private Vector instructVector = new Vector(); // Almacena todas las instrucciones del archivo de comandos
  public int runs; // Numero de instrucciones ejecutadas
  public int runcycles; // Numero total de instrucciones
  public long block = (int) Math.pow(2, 12); // Tamano de la pagina en bytes
  public static byte addressradix = 10;
  private int numphysicalpages;

  // Inicializa el kernel leyendo los archivos de configuracion y comandos.
  public void init(String commands, String config) {
    File f;
    command_file = commands;
    config_file = config;
    String line;
    String tmp;
    String command;
    byte R = 0;
    byte M = 0;
    int i = 0;
    int id = 0;
    int physical = 0;
    int inMemTime = 0;
    int lastTouchTime = 0;
    long high = 0;
    long low = 0;

    // Lee el numero de paginas virtuales desde el archivo de configuracion (si se proporciona)
    if (config != null) {
      f = new File(config);
      try {
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        while ((line = in.readLine()) != null) {
          if (line.startsWith("numpages")) {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            virtPageNum = Common.s2i(st.nextToken()) - 1;
          }
        }
        in.close();
      } catch (IOException e) { /* Ignorar */ }
    }

    // Calcula el numero de paginas fisicas (la mitad de las virtuales)
    numphysicalpages = (virtPageNum + 1) / 2;

    // Inicializa la tabla de paginas (memVector) con valores por defecto
    for (i = 0; i <= virtPageNum; i++) {
      high = (block * (i + 1)) - 1;
      low = block * i;
      memVector.addElement(new Page(i, -1, R, M, 0, 0, high, low));
    }

    // Lee configuraciones adicionales como memset, pagesize y addressradix
    if (config != null) {
      try {
        f = new File(config);
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        while ((line = in.readLine()) != null) {
          if (line.startsWith("memset")) {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            id = Common.s2i(st.nextToken());
            tmp = st.nextToken();
            physical = tmp.startsWith("x") ? -1 : Common.s2i(tmp);
            R = Common.s2b(st.nextToken());
            M = Common.s2b(st.nextToken());
            inMemTime = Common.s2i(st.nextToken());
            lastTouchTime = Common.s2i(st.nextToken());

            Page page = (Page) memVector.elementAt(id);
            page.physical = physical;
            page.R = R;
            page.M = M;
            page.inMemTime = inMemTime;
            page.lastTouchTime = lastTouchTime;
          }
          if (line.startsWith("pagesize")) {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            tmp = st.nextToken();
            block = Long.parseLong(tmp, 10);
            for (i = 0; i <= virtPageNum; i++) {
              Page page = (Page) memVector.elementAt(i);
              page.high = (block * (i + 1)) - 1;
              page.low = block * i;
            }
          }
          if (line.startsWith("addressradix")) {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            addressradix = Byte.parseByte(st.nextToken());
          }
        }
        in.close();
      } catch (IOException e) { /* Ignorar */ }
    }

    // Lee el archivo de comandos y llena el vector de instrucciones
    f = new File(commands);
    try {
      DataInputStream in = new DataInputStream(new FileInputStream(f));
      while ((line = in.readLine()) != null) {
        if (line.startsWith("//") || line.trim().isEmpty()) continue;

        if (line.startsWith("READ") || line.startsWith("WRITE")) {
          command = line.startsWith("READ") ? "READ" : "WRITE";
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();

          String token1 = st.nextToken();
          String addressStr;
          int radix = 10;

          if (token1.equalsIgnoreCase("hex")) {
            radix = 16;
            addressStr = st.nextToken();
          } else if (token1.equalsIgnoreCase("bin")) {
            radix = 2;
            addressStr = st.nextToken();
          } else if (token1.equalsIgnoreCase("oct")) {
            radix = 8;
            addressStr = st.nextToken();
          } else {
            addressStr = token1;
          }

          if (addressStr.contains("-")) {
            String[] parts = addressStr.split("-");
            long startAddr = Long.parseLong(parts[0].trim(), radix);
            long endAddr = Long.parseLong(parts[1].trim(), radix);
            instructVector.addElement(new Instruction(command, startAddr, endAddr));
          } else {
            long addr = Long.parseLong(addressStr.trim(), radix);
            instructVector.addElement(new Instruction(command, addr));
          }
        }
      }
      in.close();
    } catch (IOException | NumberFormatException e) {
      System.out.println("Error al leer el archivo de comandos: " + e.getMessage());
    }

    // Establece el numero total de instrucciones y reinicia el contador de pasos
    runcycles = instructVector.size();
    runs = 0;

    // Agrega las paginas fisicas iniciales a la GUI
    for (i = 0; i <= virtPageNum; i++) {
      Page page = (Page) memVector.elementAt(i);
      if (page.physical != -1) {
        controlPanel.addPhysicalPage(i, page.physical);
      }
    }
  }

  // Enlaza el ControlPanel para la comunicacion con la GUI
  public void setControlPanel(ControlPanel newControlPanel) {
    controlPanel = newControlPanel;
  }

  // Obtiene una pagina especifica y le dice al ControlPanel que la pinte
  public void getPage(int pageNum) {
    Page page = (Page) memVector.elementAt(pageNum);
    controlPanel.paintPage(page);
  }

  // NUEVO: Determina a que segmento pertenece un numero de pagina dado.
  // Devuelve el indice del segmento o -1 si no se encuentra.
  public int getSegmentForPage(int pageNum) {
    if (pageNum < 0 || pageNum > 31) {
      return -1;
    }
    for (int s = 0; s < SEGMENTS.length; s++) {
      for (int p = 0; p < SEGMENTS[s].length; p++) {
        if (SEGMENTS[s][p] == pageNum)
          return s;
      }
    }
    return -1;
  }

  // El bucle de ejecucion principal para la simulacion continua.
  public void run() {
    while (runs < runcycles) {
      step();
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) { /* Ignorar */ }
    }
  }

  // NUEVO: Maneja un fallo de pagina buscando un marco libre o llamando al algoritmo de reemplazo.
  private void handlePageFault(Page page, int pageNum) {
    controlPanel.pageFaultValueLabel.setText("YES");

    boolean[] framesOcupados = new boolean[numphysicalpages];
    for (int i = 0; i <= virtPageNum; i++) {
      Page p = (Page) memVector.elementAt(i);
      if (p.physical != -1) {
        framesOcupados[p.physical] = true;
      }
    }

    int marcoLibre = -1;
    for (int i = 0; i < numphysicalpages; i++) {
      if (!framesOcupados[i]) {
        marcoLibre = i;
        break;
      }
    }

    if (marcoLibre != -1) {
      page.physical = marcoLibre;
      controlPanel.addPhysicalPage(pageNum, page.physical);
    } else {
      PageFault.replacePage(memVector, virtPageNum, pageNum, controlPanel);
    }
  }

  // Ejecuta un solo paso de la simulacion.
  public void step() {
    // Si ya se ejecutaron todas las instrucciones, no hace nada.
    if (runs >= runcycles) return;

    // Obtiene la instruccion actual del vector de instrucciones.
    Instruction instruct = (Instruction) instructVector.elementAt(runs);
    controlPanel.instructionValueLabel.setText(instruct.inst);

    // Reinicia las etiquetas de segmento y paginas para el nuevo paso.
    controlPanel.segmentLabel.setText("");
    controlPanel.setPagesInvolved("");

    // Comprueba si la instruccion es un rango de direcciones de memoria.
    if (instruct.isRange()) {
      // Muestra el rango de direcciones en la GUI.
      controlPanel.addressValueLabel.setText(Long.toString(instruct.addr, addressradix) + " - " + Long.toString(instruct.endAddr, addressradix));

      // Calcula las paginas de inicio y fin del rango.
      int pageStart = Virtual2Physical.pageNum(instruct.addr, virtPageNum, block);
      int pageEnd = Virtual2Physical.pageNum(instruct.endAddr, virtPageNum, block);

      // Muestra las paginas virtuales involucradas en la GUI.
      controlPanel.setPagesInvolved(pageStart + ", " + pageEnd);

      // Obtiene los segmentos de inicio y fin del rango.
      int segStart = getSegmentForPage(pageStart);
      int segEnd = getSegmentForPage(pageEnd);

      // Comprueba si hay errores de segmentacion.
      if (segStart == -1 || segEnd == -1) {
        // La direccion esta fuera de los segmentos definidos.
        controlPanel.pageFaultValueLabel.setText("FUERA DE MEMORIA");
        runs++;
        return;
      } else if (segStart != segEnd) {
        // El rango abarca dos segmentos, lo cual es un error.
        controlPanel.pageFaultValueLabel.setText("ERROR SEGMEN");
        runs++;
        return;
      } else {
        // Si el segmento es valido, procesa todas las paginas del rango.
        controlPanel.pageFaultValueLabel.setText("NO");
        for (int p = pageStart; p <= pageEnd; p++) {
          Page page = (Page) memVector.elementAt(p);
          // Si la pagina no esta en la memoria fisica, maneja el fallo.
          if (page.physical == -1) {
            handlePageFault(page, p);
          }
          // Actualiza los bits de referencia y modificacion.
          page.R = 1;
          if ("WRITE".equals(instruct.inst)) page.M = 1;
          // Actualiza los contadores de tiempo.
          page.lastTouchTime = runs * 10;
          page.inMemTime = runs * 10;
        }
        // Pinta la primera pagina del rango en la GUI.
        getPage(pageStart);
      }
    } else {
      // Maneja instrucciones de una sola direccion.
      controlPanel.addressValueLabel.setText(Long.toString(instruct.addr, addressradix));
      int pageNum = Virtual2Physical.pageNum(instruct.addr, virtPageNum, block);

      controlPanel.setPagesInvolved(Integer.toString(pageNum));

      // Comprueba si la direccion esta fuera de un segmento.
      if (getSegmentForPage(pageNum) == -1) {
        controlPanel.pageFaultValueLabel.setText("FUERA DE MEMORIA");
        runs++;
        return;
      }

      controlPanel.pageFaultValueLabel.setText("NO");
      Page page = (Page) memVector.elementAt(pageNum);

      // Maneja el fallo de pagina si es necesario.
      if (page.physical == -1) {
        handlePageFault(page, pageNum);
      }

      // Actualiza los bits y contadores.
      page.R = 1;
      if ("WRITE".equals(instruct.inst)) page.M = 1;
      page.lastTouchTime = runs * 10;
      page.inMemTime = runs * 10;
      getPage(pageNum);
    }
    // Incrementa el contador de pasos y actualiza el tiempo en la GUI.
    runs++;
    controlPanel.timeValueLabel.setText(Integer.toString(runs * 10) + " (ns)");
  }

  // Restablece la simulacion a su estado inicial.
  public void reset() {
    memVector.removeAllElements();
    instructVector.removeAllElements();
    controlPanel.statusValueLabel.setText("STOP");
    controlPanel.timeValueLabel.setText("0");
    controlPanel.instructionValueLabel.setText("NONE");
    controlPanel.addressValueLabel.setText("NULL");
    controlPanel.pageFaultValueLabel.setText("NO");
    controlPanel.virtualPageValueLabel.setText("x");
    controlPanel.physicalPageValueLabel.setText("0");
    controlPanel.RValueLabel.setText("0");
    controlPanel.MValueLabel.setText("0");
    controlPanel.inMemTimeValueLabel.setText("0");
    controlPanel.lastTouchTimeValueLabel.setText("0");
    controlPanel.lowValueLabel.setText("0");
    controlPanel.highValueLabel.setText("0");
    init(command_file, config_file);
  }
}