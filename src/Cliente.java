import java.rmi.RemoteException;

public class Cliente extends ImpPeer {

    protected Cliente() throws RemoteException {
    }

    public static void main(String[] args) {
        String name = "PeerC";
        try {
            Cliente cl = new Cliente();
            cl.initiate(name,cl);
            cl.menuInicial();

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
