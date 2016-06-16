package socketserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * 上次未实现功能：没有判断一个稳定连接的客户端（可通过几次握手成功确定，几次握手成功后在将用户放入服务端缓存），
 * 
 * Created by orange on 16/6/8.
 * 
 */

public class TCPLongConnectionServer {
	//4c37987f8e13461dbf0c133af338c039小米 IP:172.16.100.32        04fb00752bc94d84b718d9a41e024c7a三星 IP: 172.16.100.90
	private static boolean isStart=true;
	private static ServerResponseTask serverResponseTask;
	private static ConcurrentHashMap<String, Socket> onLineClient=new ConcurrentHashMap<String, Socket>();
    public TCPLongConnectionServer(){

    }
    public static void main(String[] args){
        ServerSocket serverSocket=null;
        ExecutorService executorService=Executors.newCachedThreadPool();
        try {
            serverSocket=new ServerSocket(9013);
            	while(isStart){
                Socket socket=serverSocket.accept();
                System.out.println("用户的IP地址为："+socket.getInetAddress().getHostAddress());
                serverResponseTask=new ServerResponseTask(socket,new TCPLongConnectServerHandlerData.TCPResultCallBack() {
					
				@Override
				public void connectSuccess(Protocol reciveMsg) {
								// 新加入的客户端成功连接后
					if (reciveMsg != null) {
						onLineClient.put(reciveMsg.getMsgTargetUUID(), socket);
							Socket targetClient = getConnectClient(reciveMsg.getMsgTargetUUID());
							if (targetClient != null) {
									System.out.println("return:"+reciveMsg.toString());
									serverResponseTask.addWriteTask(reciveMsg, targetClient);// TODO
								} else {
									System.out.println("Newclient is null");
									}
								}
						}
					});
                if(socket.isConnected()){
                	executorService.execute(serverResponseTask);
                }
                printAllClient();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket!=null){
                try {
                	isStart=false;
                    serverSocket.close();
                    if(serverSocket!=null)
                    	serverResponseTask.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *   发送消息给指定用户
     * @param clientId
     * @return
     */
    public boolean sendToClient(String clientId){
    	if(onLineClient.contains(clientId)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 打印已经链接的客户端
     */
    public static void printAllClient(){
    	if (onLineClient==null) {
			return ;
		}
    	Iterator<String> inter=onLineClient.keySet().iterator();
    	while(inter.hasNext()){
    		System.out.println("client:"+inter.next());
    	}
    }
    
    public static Socket getConnectClient(String clientID){
    	return onLineClient.get(clientID);//与小米手机进行通信
    }
}
