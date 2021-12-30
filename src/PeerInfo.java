
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;


@SuppressWarnings("serial")
public class PeerInfo  extends UnicastRemoteObject implements Serializable{
    protected PeerInfo() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	private String name;
    private PublicKey pubKey;

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
