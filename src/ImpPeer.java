
import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.*;

public class ImpPeer extends UnicastRemoteObject implements Peer {
	protected ImpPeer() throws RemoteException {
	}

	// Propriedades Peer

	private String name = "";
	private PublicKey pubkey;
	private PrivateKey privKey;
	private long timeStampSelf;
	private long timeStampSelf2;
	// =================================================================================================================
	// Listas com informacoes dos outros peers
	private final LinkedList<String> listaPeers = new LinkedList<>();
	private final LinkedList<PublicKey> listaPub = new LinkedList<>();
	// ============================================================================================
	// Propriedades Recursos
	private int estadoRec1 = 0; // 0 - Não está usando | 1 - Querendo | 2 - Usando
	private int estadoRec2 = 0; //
	private int contadorRec1 = 0;
	private int contadorRec2 = 0;
	private final LinkedList<String> filaEsperaRecurso1 = new LinkedList<>();
	private final LinkedList<String> filaEsperaRecurso2 = new LinkedList<>();
	// =================================================================================================================
	// getters and setters recursos

	public int getEstadoRec1() {
		return estadoRec1;
	}

	public void setEstadoRec1(int estadoRec1) {
		this.estadoRec1 = estadoRec1;
	}

	public int getEstadoRec2() {
		return estadoRec2;
	}

	public void setEstadoRec2(int estadoRec2) {
		this.estadoRec2 = estadoRec2;
	}

	public int getContadorRec1() {
		return contadorRec1;
	}

	public void setContadorRec1(int contadorRec1) {
		this.contadorRec1 = contadorRec1;
	}

	public int getContadorRec2() {
		return contadorRec2;
	}

	public void setContadorRec2(int contadorRec2) {
		this.contadorRec2 = contadorRec2;
	}

	// =================================================================================================================
	// getters and setters Propriedades Peer

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PublicKey getPubkey() {
		return pubkey;
	}

	public void setPubkey(PublicKey pubkey) {
		this.pubkey = pubkey;
	}

	public PrivateKey getPrivKey() {
		return privKey;
	}

	public void setPrivKey(PrivateKey privKey) {
		this.privKey = privKey;
	}

	public long getTimeStampSelf() {
		return timeStampSelf;
	}

	public void setTimeStampSelf(long timeStampSelf) {
		this.timeStampSelf = timeStampSelf;
	}

	public long getTimeStampSelf2() {
		return timeStampSelf2;
	}

	public void setTimeStampSelf2(long timeStampSelf2) {
		this.timeStampSelf2 = timeStampSelf2;
	}

	// =================================================================================================================
	// Recursos
	@Override
	public String recurso1() throws RemoteException {
		System.out.println("Usou recurso 1");
		return "Usou recurso 1";
	}

	@Override
	public String recurso2() throws RemoteException {
		System.out.println("Usou recurso 2");
		return "Usou recurso 2";
	}

	// =================================================================================================================
	// inicia Peer
	@Override
	public void initiate(String name, Remote obj) throws RemoteException {
		try {
			System.out.println("Iniciando " + name + "..."); // mostra que o processo de inicio do peer comecou
			setName(name); // configura o nome
			System.out.println("procurando Registry");
			LocateRegistry.getRegistry(1099); // procura pelo servico de nomes
			Naming.rebind(name, obj); // adiciona o peer ao servico de nomes
			Peer peer = (Peer) Naming.lookup(name); // define o peer
			peer.geraChaves(); // gera as chaves publicas e privadas
			System.out.println("Registry found!");
			System.out.println("Peer " + name + " iniciado com sucesso!!!");
			System.out.println(LocateRegistry.getRegistry().list().length);
		} catch (RemoteException e) { // caso registro do servico de nomes nao seja encontrado, cria um e adiciona o
			// peer
			try {
				System.out.println("Iniciando " + name + "...");
				LocateRegistry.createRegistry(1099);
				System.out.println("Registry Created!");
				Naming.rebind(name, obj);
				Peer peer = (Peer) Naming.lookup(name); // define o peer
				peer.geraChaves(); // gera as chaves publicas e privadas
				System.out.println("nome adicionado com sucesso!!");
				System.out.println(LocateRegistry.getRegistry().list().length);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		} catch (MalformedURLException | InvalidKeyException | NoSuchAlgorithmException | SignatureException
				| NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// =================================================================================================================

	// =================================================================================================================
	// Agrawalla
	@Override
	public void request(int recurso) throws RemoteException {
		String msg;
		// se recurso 1
		if (recurso == 1) {
			setEstadoRec1(1); // define que quer recurso 1
			setTimeStampSelf(new Date().getTime()); // armazena o timestamp do pedido
			msg = "" + getName() + "_" + recurso + "_" + getTimeStampSelf(); // gera menssagem
			// msg -> nome_qualRecurso_timestamp
		} else {
			// se recurso 2
			setEstadoRec2(1);
			setTimeStampSelf2(new Date().getTime());
			msg = "" + getName() + "_" + recurso + "_" + getTimeStampSelf2();
		}

		try {
			System.out.println("Estado " + getEstadoRec1() + " " + msg);
			byte[] assinatura = assinaMenssagem(msg, getPrivKey()); // gera assinatura para msg.
			Registry res = LocateRegistry.getRegistry();
			String[] nomes = res.list(); // pega relacao de nomes para pedir o acesso
			for (String nome : nomes) {
				System.out.println(nome);
				if (nome.equals(getName())) {
					if (nomes.length == 1) { // caso so tenha ele no servico de nomes, vai para a secao critica
						if (recurso == 1) {
							setEstadoRec1(2);
						} else {
							setEstadoRec2(2);
						}
					} // caso apareca o proprio nome pula.

				} else {
					Peer cl = (Peer) Naming.lookup(nome);
					cl.analyseRequest(msg, assinatura);
				}
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | MalformedURLException
				| NotBoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void analyseRequest(String msg, byte[] assinatura) throws RemoteException {
		String[] info = msg.split("_"); // separa a mensagem
		// PeerInfo infoHere = null;
		String nome = info[0];
		PublicKey pubK = getPubkey();

		try {
			Peer clie = (Peer) Naming.lookup(info[0]); // procura o remetente da menssagem
			if (!listaPeers.contains(clie.getName())) { // caso n esteja na lista de peers, adiciona
				listaPeers.addFirst(clie.getName());
				;
				listaPub.addFirst(clie.getPubkey());

			}
			pubK = listaPub.get(listaPeers.indexOf(nome));

			if (validaAssinatura(msg, pubK, assinatura)) { // valida a menssagem com a assinatura

				if ("1".equals(info[1])) { // caso o recurso seja o 1

					if (getEstadoRec1() == 0) {
						String msgem = "" + getName() + "_" + info[1] + "_" + "OK";
						byte[] assin = assinaMenssagem(msgem, getPrivKey());

						Peer cli = (Peer) Naming.lookup(nome);
						cli.reply(msgem, assin);

					} else if (getEstadoRec1() == 1) {
						System.out.println("B: " + getTimeStampSelf() + "|A: " + Long.parseLong(info[2]));
						Registry res = LocateRegistry.getRegistry();
						if (getTimeStampSelf() > Long.parseLong(info[2])) {
							String msgem = "" + getName() + "_" + info[1] + "_" + "OK";
							byte[] assin = assinaMenssagem(msgem, getPrivKey());
							Peer cli = (Peer) Naming.lookup(nome);
							cli.reply(msgem, assin);
						} else if (getTimeStampSelf() < Long.parseLong(info[2])) {
							filaEsperaRecurso1.add(msg);
						}
					} else {
						filaEsperaRecurso1.add(msg);
					}
				} else { // caso o recurso seja o 2

					if (getEstadoRec2() == 0) {
						String msgem = "" + getName() + "_" + info[1] + "_" + "OK";
						byte[] assin = assinaMenssagem(msgem, getPrivKey());

						Peer cli = (Peer) Naming.lookup(nome);
						cli.reply(msgem, assin);

					} else if (getEstadoRec2() == 1) {
						Registry res = LocateRegistry.getRegistry();
						if (getTimeStampSelf2() > Long.parseLong(info[2])) {
							String msgem = "" + getName() + "_" + info[1] + "_" + "OK";
							byte[] assin = assinaMenssagem(msgem, getPrivKey());
							Peer cli = (Peer) Naming.lookup(nome);
							cli.reply(msgem, assin);
						} else if (getTimeStampSelf2() < Long.parseLong(info[2])) {
							filaEsperaRecurso2.add(msg);
						}
					} else {
						filaEsperaRecurso2.add(msg);
					}
				}
			}

		} catch (NotBoundException | MalformedURLException | NoSuchAlgorithmException | SignatureException
				| InvalidKeyException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void reply(String msg, byte[] assinatura) throws RemoteException {
		try {
			System.out.println("Entrou pro reply");
			String[] inf = msg.split("_");
			System.out.println(inf[0] + " " + inf[1] + " " + inf[2]);
			// PeerInfo infoHere = new PeerInfo();
			String nom = inf[0];
			PublicKey pubK = getPubkey();
			Peer clie = (Peer) Naming.lookup(inf[0]); // procura o remetente da menssagem
			if (!listaPeers.contains(clie.getName())) { // caso n esteja na lista de peers, adiciona
				listaPeers.addFirst(clie.getName());
				;
				listaPub.addFirst(clie.getPubkey());

			}
			pubK = listaPub.get(listaPeers.indexOf(nom));

			System.out.println(listaPeers.indexOf(nom));

			if (validaAssinatura(msg, pubK, assinatura)) {

				Registry res = LocateRegistry.getRegistry();
				if ("1".equals(inf[1])) {
					if ("OK".equals(inf[2])) {
						setContadorRec1(getContadorRec1() + 1);
						System.out.println("setou contador");
						System.out.println(res.list().length + " " + getContadorRec1());
						if (getContadorRec1() == (res.list().length - 1)) {
							System.out.println("secao critica");
							setEstadoRec1(2);
						}
					}
				} else {
					if ("2".equals(inf[1])) {
						if ("OK".equals(inf[2])) {
							setContadorRec2(getContadorRec2() + 1);
							if (getContadorRec2() == (res.list().length - 1)) {
								setEstadoRec2(2);

							}
						}
					}
				}

			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void releaseResource(int recurso) throws RemoteException {
		if (recurso == 1) {
			setEstadoRec1(0);
			setContadorRec1(0);
			for (String msgAssinatura : filaEsperaRecurso1) {
				replyOldRequests(msgAssinatura);
			}
		} else {
			setEstadoRec2(0);
			setContadorRec2(0);
			for (String msgAssinatura : filaEsperaRecurso2) {
				replyOldRequests(msgAssinatura);
			}
		}

	}

	@Override
	public void replyOldRequests(String msg) throws RemoteException {
		try {
			String[] info = msg.split("_"); // separa a mensagem
			// PeerInfo infoHere = null;
			String nome = info[0];
			if ("1".equals(info[1])) {
				if (getEstadoRec1() == 0) {
					String msgem = "" + getName() + "_" + info[1] + "_" + "OK";
					byte[] assin = assinaMenssagem(msgem, getPrivKey());

					Peer cli = (Peer) Naming.lookup(nome);
					cli.reply(msgem, assin);

				}
			} else {
				if (getEstadoRec2() == 0) {
					String msgem = "" + getName() + "_" + info[1] + "_" + "OK";
					byte[] assin = assinaMenssagem(msgem, getPrivKey());

					Peer cli = (Peer) Naming.lookup(nome);
					cli.reply(msgem, assin);

				}

			}
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// =================================================================================================================
	// chaves e afins
	@Override
	public void geraChaves() throws NoSuchAlgorithmException, RemoteException {
		// TODO Auto-generated method stub

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
		SecureRandom secRan = new SecureRandom();

		kpg.initialize(1024, secRan);
		java.security.KeyPair keyP = kpg.generateKeyPair();
		setPrivKey(keyP.getPrivate());
		setPubkey(keyP.getPublic());
	}

	@Override
	public boolean validaAssinatura(String mensagem, PublicKey publicKey, byte[] assinatura)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException {
		Signature clientSig = Signature.getInstance("DSA");
		clientSig.initVerify(publicKey);
		clientSig.update(mensagem.getBytes());

		return clientSig.verify(assinatura);

	}

	@Override
	public byte[] assinaMenssagem(String mensagem, PrivateKey privateKey)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException {
		Signature sig = Signature.getInstance("DSA");
		sig.initSign(privateKey);
		sig.update(mensagem.getBytes());
		return sig.sign();
	}

	// =================================================================================================================
	// Menus
	@Override
	public void menuInicial() throws RemoteException {
		int opcao = 0;
		Scanner sc = new Scanner(System.in);
		do {

			if (getEstadoRec1() == 0 & getEstadoRec2() == 0) {
				System.out.println("===================================");
				System.out.println("      1 - Requisitar Recurso 1     ");
				System.out.println("      2 - Requisitar Recurso 2     ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				switch (opcao) {
				case 1 -> request(1);
				case 2 -> request(2);
				}

			} else if (getEstadoRec1() == 0 & getEstadoRec2() == 1) {
				System.out.println("===================================");
				System.out.println("     1 - Requisitar Recurso 1      ");
				System.out.println("    Aguardando Acesso Recurso 2    ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				if (opcao == 1) {
					request(1);
				}
			} else if (getEstadoRec1() == 0 & getEstadoRec2() == 2) {
				System.out.println("===================================");
				System.out.println("      1 - Requisitar Recurso 1     ");
				System.out.println("      2 - Usar Recurso 2           ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				switch (opcao) {
				case 1 -> request(1);
				case 2 -> menuUsoRecurso(2);
				}
			} else if (getEstadoRec1() == 1 & getEstadoRec2() == 0) {
				System.out.println("===================================");
				System.out.println("     Aguardando Acesso Recurso 1   ");
				System.out.println("      2 - Requisitar Recurso 2     ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				if (opcao == 2) {
					request(2);
				}
			} else if (getEstadoRec1() == 1 & getEstadoRec2() == 1) {
				System.out.println("===================================");
				System.out.println("     Aguardando Acesso Recurso 1   ");
				System.out.println("     Aguardando Acesso Recurso 2   ");
				System.out.println("===================================");

			} else if (getEstadoRec1() == 1 & getEstadoRec2() == 2) {
				System.out.println("===================================");
				System.out.println("     Aguardando Acesso Recurso 1   ");
				System.out.println("      2 - Usar Recurso 2           ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				if (opcao == 2) {
					menuUsoRecurso(2);
				}
			} else if (getEstadoRec1() == 2 & getEstadoRec2() == 0) {
				System.out.println("===================================");
				System.out.println("      1 - Usar recurso 1           ");
				System.out.println("      2 - Requisitar Recurso 2     ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				switch (opcao) {
				case 1 -> menuUsoRecurso(1);
				case 2 -> request(2);
				}
			} else if (getEstadoRec1() == 2 & getEstadoRec2() == 1) {
				System.out.println("===================================");
				System.out.println("      1 - Usar recurso 1           ");
				System.out.println("    Aguardando Acesso Recurso 2    ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				if (opcao == 1) {
					menuUsoRecurso(1);
				}
			} else if (getEstadoRec1() == 2 & getEstadoRec2() == 2) {
				System.out.println("===================================");
				System.out.println("      1 - Usar Recurso 1           ");
				System.out.println("      2 - Usar Recurso 2           ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				switch (opcao) {
				case 1 -> menuUsoRecurso(1);
				case 2 -> menuUsoRecurso(2);
				}
			}
		} while (opcao < 3);
	}

	@Override
	public void menuUsoRecurso(int recurso) throws RemoteException {
		int opcao = 0;
		Scanner sc = new Scanner(System.in);
		do {
			if (recurso == 1) {
				System.out.println("===================================");
				System.out.println("      1 - Executar Recurso 1       ");
				System.out.println("      2 - Liberar Recurso 1        ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				switch (opcao) {
				case 1 -> recurso1();
				case 2 -> releaseResource(1);
				}
			} else {
				System.out.println("===================================");
				System.out.println("      1 - Executar Recurso 2       ");
				System.out.println("      2 - Liberar Recurso 2        ");
				System.out.println("===================================");
				System.out.println("Opcao selecionada:");
				if (sc.hasNextInt()) {
					opcao = sc.nextInt();
				}
				switch (opcao) {
				case 1 -> recurso2();
				case 2 -> releaseResource(2);
				}
			}
		} while (opcao != 2);
	}

}
