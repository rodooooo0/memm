public class Instruction 
{
  public String inst;
  public long addr;
  public long endAddr; // -1 si no es rango

  public Instruction(String inst, long addr)
  {
    this.inst = inst;
    this.addr = addr;
    this.endAddr = -1;
  }

  public Instruction(String inst, long addr, long endAddr)
  {
    this.inst = inst;
    this.addr = addr;
    this.endAddr = endAddr;
  }

  public boolean isRange()
  {
    return endAddr != -1;
  }
}