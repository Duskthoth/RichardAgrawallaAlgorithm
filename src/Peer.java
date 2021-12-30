import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.*;

public interface Peer extends Remote {
	// recursos de todos os peers
	public String recurso1() throws RemoteException;

	public String recurso2() throws RemoteException;

	// inicia peer
	public void initiate(String name, Remote obj) throws RemoteException;

	// Agrawalla stuff
	public void request(int recurso) throws RemoteException;

	public void analyseRequest(String msg, byte[] assinatura) throws RemoteException;

	public void replyOldRequests(String msg) throws RemoteException;

	public void reply(String msg, byte[] assinatura) throws RemoteException;

	public void releaseResource(int recurso) throws RemoteException;

	// Assinatura digital
	public void geraChaves() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException;

	public boolean validaAssinatura(String mensagem, PublicKey publicKey, byte[] assinatura)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException;

	public byte[] assinaMenssagem(String mensagem, PrivateKey privateKey)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException;

	// extra
	public String getName() throws RemoteException;

	public PublicKey getPubkey() throws RemoteException;

	public void menuInicial() throws RemoteException;

	public void menuUsoRecurso(int recurso) throws RemoteException;
}
