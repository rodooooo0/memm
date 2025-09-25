public class Page
{
  // El ID de la pagina virtual
  public int id;
  // El marco de memoria fisica donde reside la pagina
  // -1 si no esta en la memoria fisica
  public int physical;
  // El bit de referencia (1 si fue accedida recientemente)
  public byte R;
  // El bit de modificacion (1 si fue escrita recientemente)
  public byte M;
  // El tiempo total que la pagina ha estado en memoria fisica
  public int inMemTime;
  // El tiempo de la ultima vez que la pagina fue accedida
  public int lastTouchTime;
  // El limite superior del rango de direcciones virtuales de esta pagina
  public long high;
  // El limite inferior del rango de direcciones virtuales de esta pagina
  public long low;

  // El constructor inicializa todos los atributos de la pagina
  public Page( int id, int physical, byte R, byte M, int inMemTime, int lastTouchTime, long high, long low )
  {
    this.id = id;
    this.physical = physical;
    this.R = R;
    this.M = M;
    this.inMemTime = inMemTime;
    this.lastTouchTime = lastTouchTime;
    this.high = high;
    this.low = low;
  }
}