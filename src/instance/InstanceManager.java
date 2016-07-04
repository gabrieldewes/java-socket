/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instance;

import java.io.*;  
import java.net.*;  

/**
 *
 * @author gabriel
 */

class InstanceManager {
  
    private static ServerSocket socket;  
    private static InstanceListener sub_listener;  
      
    /** Porta aleatória, porém statica e de numero alto */  
    private static int PORT = 44121;  
    /** Constante, que indica até que porta no máximo será permitida a conexão*/  
    private static final int MAX_PORT = 44140;  
    /** Endereço do Host */  
    private static InetAddress ip_host;  
    /** Mensagem para notificar nova instancia. PRECISA terminar com \n */  
    private static final String MENSAGEM = "Malandramente?\n";  
    /** Resposta enviada quando detectar nova instancia. PRECISA terminar com \n */  
    private static final String RESPOSTA = "Vai Safada!\n";  
    /** Conectado como servidor? */  
    private static boolean started;  
    /** Conectado como cliente? */  
    private static boolean connected;  
    /** Abrir a aplicação em caso de erro na rede? */  
    private final static boolean ERRO = false;  
    /**  
     * Tenta conectar ao socket como servidor, 
     * caso a porta já estiver aberta, tenta conectar como cliente. 
     *  
     * Essa operação é repetida até conseguir conectar como servidor 
     * ou receber uma resposta correta de outra instancia do servidor. 
     *  
     * O numero da porta é incrementado até atingir o numero inicial da porta + 20 
     * nessa caso, ele para de tentar fazer conexões, e retorna o valor em caso de erro. 
     * 
     * @return
     * retorna true se conseguir abrir o servidor ou false caso contrário. 
     * retorna o valor de erro caso ocorra algum. 
     */  
    static boolean registerInstance() {
        try { ip_host = InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1});} 
        catch (UnknownHostException e) {  
            e.printStackTrace(System.err);  
            return ERRO;  
        }  
        while (!started && !connected) {  
            startServer();  
            if (started) return true; 
            startClient();  
            if (connected) return false;  
            if (PORT > MAX_PORT) {  
               System.err.println("Nenhuma porta disponível! ("+PORT+" ~ "+MAX_PORT+").");  
               break;  
            }  
        }  
        return ERRO;  
    }  
    /** 
     * Abre um socket como servidor. 
     * Se conseguir, inicia uma Thread para receber conexões de novas instancias 
     */  
    private static void startServer() {  
        try {  
            System.out.println("Iniciando novo servidor em "+ PORT +"...");  
            socket = new ServerSocket(PORT, 20, ip_host);  
            System.out.println("Conectado, escutando novas instâncias em "+ ip_host +":"+ PORT);  
            new InstanceThread().start();  
            started = true;  
        } catch (IOException ex) {  
            //ex.printStackTrace(System.err);  
            started = false;  
        }    
    }    
    /** 
     * Tenta conectar a um servidor que já está aberto. 
     * Envia a MENSAGEM e espera pela resposta. 
     *  
     * Se a resposta for igual a RESPOSTA esperada,  
     * significa que outra instancia já está em execução, neste caso, apenas termine  
     * a execução da instancia atual. 
     *  
     * Se a resposta for diferente (ou null) significa que outra aplicação está em execução 
     * nesta porta. Então, incrementa a porta para tentar conectar como sevidor novamente. 
     */  
    private static void startClient() {  
        System.out.println("Porta já está aberta, notificando a primeira instância...");  
        try (
            Socket clientSocket = new Socket(ip_host, PORT);  
            OutputStream out = clientSocket.getOutputStream();  
            BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) ) ) 
        {  
            out.write(MENSAGEM.getBytes());  
            String resposta = in.readLine();  
            System.out.println("Primeira instância notificada! ");  
            connected = !( resposta == null || !RESPOSTA.trim().equals(resposta.trim()) );  
            if (connected) {  
                System.err.println("Resposta Correta! \""+ resposta +"\"\nAplicação encerrada.");  
            } 
            else {  
                System.err.println("Resposta Incorreta: \""+ resposta +"\"");  
                PORT++;  
            }  
        } catch (IOException ex) {  
            connected = false;  
        }  
    }  
  
    /**
     * @param listener
     * Seta um listener para receber as notificações de nova instância.
     */  
    public static void setListener(InstanceListener listener) {  
        sub_listener = listener;  
    } 
    
    /** 
     * Notifica o listener que uma nova instancia foi detectada. 
     */  
    private static void fireNewInstance() {  
        if (sub_listener != null)
            sub_listener.newInstanceCreated();   
    }  
    
    /** 
     * Thread para aceitar conexões e receber mensagens pelo socket 
     *  
     * Ela deve ser iniciada quando a conexão do tipo servidor for aberta 
     * e ficará rodando enquando o programa estiver em execução. 
     *  
     * Quando receber uma nova conexão, primeiro verifica se a mensagem recebida 
     * corresponde com a MENSAGEM esperada. Caso seja igual, significa que é  
     * uma nova instancia da mesma aplicação, portanto, envia a RESPOSTA que o cliente 
     * está esperando para garantir que é a mesma aplição. Depois notifica o listener 
     * que uma nova instancia foi aberta. 
     *  
     * Caso a mensagem seja diferente, apenas fecha a conexão desconhecida. 
     */  
    private static class InstanceThread extends Thread {
        InstanceThread() {
            super();  
            /** 
             * Se esta for a única Thread ainda em execução na aplicação, 
             * não faz sentido continuar. 
             *  
             * Por isso, colocando daemon = true, 
             * impede que a aplicação continue aberta quando esta for 
             * a ultima Thread em execução 
             * (o que iria causar o programa a rodar eternamente) 
             */  
            setDaemon(true);  
        }  
        @Override  
        public void run() {  
            while (!socket.isClosed()) {  
                try (
                    Socket client = socket.accept();  
                    BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
                    OutputStream out = client.getOutputStream()) 
                {  
                    String message = in.readLine();  
                    if (MENSAGEM.trim().equals(message.trim())) {  
                        System.err.println("Nova instância do programa detectada: \n\"" + message + "\" => Resposta enviada.");  
                        out.write(RESPOSTA.getBytes());  
                        fireNewInstance();  
                    } 
                    else {  
                        System.err.println("Conexão desconhecida detectada: \"" + message + "\"");  
                        out.write("Vá embora meu filho! ".getBytes());  
                    }  
                } catch (IOException ex) {  
                    ex.printStackTrace(System.err);  
                }  
            }  
        }  
    }  
    
}
