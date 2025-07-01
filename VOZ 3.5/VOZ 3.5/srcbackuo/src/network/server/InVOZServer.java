package network.server;

public interface InVOZServer extends Runnable {
  InVOZServer init();
  
  InVOZServer start(int paramInt) throws Exception;
  
  InVOZServer setAcceptHandler(ISessionAcceptHandler paramISessionAcceptHandler);
  
  InVOZServer close();
  
  InVOZServer dispose();
  
  InVOZServer randomKey(boolean paramBoolean);
  
  InVOZServer setDoSomeThingWhenClose(IServerClose paramIServerClose);
  
  InVOZServer setTypeSessioClone(Class paramClass) throws Exception;
  
  ISessionAcceptHandler getAcceptHandler() throws Exception;
  
  boolean isRandomKey();
  
  void stopConnect();
}


/* Location:              C:\Users\VoHoangKiet\Downloads\TEA_V5\lib\GirlkunNetwork.jar!\com\girlkun\network\server\InEMTIServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */