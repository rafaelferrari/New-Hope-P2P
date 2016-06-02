import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FlowLayout;

public class MainAppFrame extends JFrame implements KeyListener{
	
	// TODO: Este vetor só vale para uma tabela 10x10!!!!!!!!!!!!
	private static JLabel label[] = new JLabel[100];
	private static ImageIcon bg_icon, fb_icon, ss_iconl, ss_iconr, ss_iconu, ss_icond, ss_icon2l, ss_icon2r, ss_icon2u, ss_icon2d;
	
	// ID UNICA DO USUARIO
	public static int myshipID;
	
	// Multiplayer
	public static Multiplayer mp;
	public static final String gameServer = "192.168.25.64";
	public static final int gameServerUDP = 6666;
	
	public static final int iniX = 2;
	public static final int iniY = 2;
	public static final short iniOR = Constants.UP;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				try {
					MainAppFrame frame = new MainAppFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				ArrayList<Item> itens = new ArrayList<Item>();
				Item aShip = new Item(iniX,iniY,iniOR,Constants.ITEM_SHIP,1234);
				myshipID = 1234;
//				itens.add(aShip);
				Engine e = new Engine();
				Engine.addShip(aShip);
				bg_icon = e.createImageIcon("icons/bg.png");
				fb_icon = e.createImageIcon("icons/fb2.png");
				ss_iconl = e.createImageIcon("icons/ss3_l.png");
				ss_iconr = e.createImageIcon("icons/ss3_r.png");
				ss_iconu = e.createImageIcon("icons/ss3_u.png");
				ss_icond = e.createImageIcon("icons/ss3_d.png");
				
				ss_icon2l = e.createImageIcon("icons/ss1_l.png");
				ss_icon2r = e.createImageIcon("icons/ss1_r.png");
				ss_icon2u = e.createImageIcon("icons/ss1_u.png");
				ss_icon2d = e.createImageIcon("icons/ss1_d.png");
				
				isItDeadYet();
				e.moveFireballs();
//				e.readPlayerCmds();
//				e.printTable();
				printTable();
				e.calcColision();
					try {
						mp = new Multiplayer(gameServer, gameServerUDP);
					} catch (UnknownHostException | SocketException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						mp.connect2GameServer(iniX, iniY, iniOR);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					mp.receberMensagens();
//					mp.enviarMensagem("P;1,2:3");
			
				
			}
		});
	}
	
	public static void printTable() {
		Thread t = new Thread(new Runnable() {           
	        public void run() { 
	        	Item[][] table = new Item[Constants.SIZE_V][Constants.SIZE_H];
				int[] posicao = {0, 0};
				boolean tbchange;
				
				while(true) {
					tbchange = false;
					while(!tbchange) {
                		try {
    						Thread.sleep(100);
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
                		synchronized (Engine.mutex_tablechanged) {
                			tbchange = Engine.tablechanged;
    					}
                	}
					synchronized (Engine.mutex_tablechanged) {
            			Engine.tablechanged = false;
					}
					
					table = new Item[Constants.SIZE_V][Constants.SIZE_H];
					synchronized (Engine.mutex_presentItems) {
						for(Item it: Engine.presentItems) {
							posicao = it.position;
							table[posicao[0]][posicao[1]] = it;
						}
					}
									
					Item it;
					for(int i = 0; i < Constants.SIZE_V; i++) {
						for(int j = 0; j < Constants.SIZE_H; j++) {
							it = table[i][j];
							if (it == null) {
//								label[xy2label(i, j)].setText(" ");
								label[xy2label(i, j)].setIcon(bg_icon);
							} else if (it.type == Constants.ITEM_FB) {
//								label[xy2label(i, j)].setText("o");
								label[xy2label(i, j)].setIcon(fb_icon);
							} else if (it.type == Constants.ITEM_SHIP){
								if (it.orientation == Constants.LEFT)
//									label[xy2label(i, j)].setText("<");
									if (it.id == myshipID) {
										label[xy2label(i, j)].setIcon(ss_iconl);
									} else {
										label[xy2label(i, j)].setIcon(ss_icon2l);
									}
								else if (it.orientation == Constants.RIGHT)
//									label[xy2label(i, j)].setText(">");
									if (it.id == myshipID) {
										label[xy2label(i, j)].setIcon(ss_iconr);
									} else {
										label[xy2label(i, j)].setIcon(ss_icon2r);
									}
								else if (it.orientation == Constants.UP)
//									label[xy2label(i, j)].setText("A");
									if (it.id == myshipID) {
										label[xy2label(i, j)].setIcon(ss_iconu);
									} else {
										label[xy2label(i, j)].setIcon(ss_icon2u);
									}
								else if (it.orientation == Constants.DOWN)
//									label[xy2label(i, j)].setText("V");
									if (it.id == myshipID) {
										label[xy2label(i, j)].setIcon(ss_icond);
									} else {
										label[xy2label(i, j)].setIcon(ss_icon2d);
									}
								else {
									System.out.println("/nErro fatal de orientacao. " + it.orientation);
									System.exit(0);
								}
							} else {
								System.out.println("/nErro fatal de tipo de celula.");
								System.exit(0);
							}
						}
					}
										
				}
				
				
	        }				
	    });
		t.start();				
	}
	
	public static int xy2label(int x, int y) {
		// TODO: Esta relação só vale para uma tabela 10x10!!!!!!!!!!
		return y + 10*x;
		
	}
	/**
	 * Create the frame.
	 */
	public MainAppFrame() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 640);
		
		getContentPane().setFocusable(true);
		getContentPane().setFocusTraversalKeysEnabled(false);
		getContentPane().setLayout(new GridLayout(10, 10, 0, 0));
		
		JLabel lblD = new JLabel("");
		getContentPane().add(lblD);
		lblD.setFont(new Font("Monospaced", Font.PLAIN, 11));
		
		label[0] = lblD;
		
		JLabel lblD_1 = new JLabel("");
		getContentPane().add(lblD_1);
		lblD_1.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[1] = lblD_1;
		
		JLabel lblD_2 = new JLabel("");
		getContentPane().add(lblD_2);
		lblD_2.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[2] = lblD_2;
		
		JLabel lblD_3 = new JLabel("");
		getContentPane().add(lblD_3);
		lblD_3.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[3] = lblD_3;
		
		JLabel lblD_4 = new JLabel("");
		getContentPane().add(lblD_4);
		lblD_4.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[4] = lblD_4;
		
		JLabel lblD_5 = new JLabel("");
		getContentPane().add(lblD_5);
		lblD_5.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[5] = lblD_5;
		
		JLabel lblD_6 = new JLabel("");
		getContentPane().add(lblD_6);
		lblD_6.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[6] = lblD_6;
		
		JLabel lblD_7 = new JLabel("");
		getContentPane().add(lblD_7);
		lblD_7.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[7] = lblD_7;
		
		JLabel lblD_8 = new JLabel("");
		getContentPane().add(lblD_8);
		lblD_8.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[8] = lblD_8;
		
		JLabel lblD_9 = new JLabel("");
		getContentPane().add(lblD_9);
		lblD_9.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[9] = lblD_9;
		
		JLabel lblD_10 = new JLabel("");
		getContentPane().add(lblD_10);
		lblD_10.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[10] = lblD_10;
		
		JLabel lblD_11 = new JLabel("");
		getContentPane().add(lblD_11);
		lblD_11.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[11] = lblD_11;
		
		JLabel lblD_12 = new JLabel("");
		getContentPane().add(lblD_12);
		lblD_12.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[12] = lblD_12;
		
		JLabel lblD_13 = new JLabel("");
		getContentPane().add(lblD_13);
		lblD_13.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[13] = lblD_13;
		
		JLabel lblD_14 = new JLabel("");
		getContentPane().add(lblD_14);
		lblD_14.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[14] = lblD_14;
		
		JLabel lblD_15 = new JLabel("");
		getContentPane().add(lblD_15);
		lblD_15.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[15] = lblD_15;
		
		JLabel lblD_16 = new JLabel("");
		getContentPane().add(lblD_16);
		lblD_16.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[16] = lblD_16;
		
		JLabel lblD_17 = new JLabel("");
		getContentPane().add(lblD_17);
		lblD_17.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[17] = lblD_17;
		
		JLabel lblD_18 = new JLabel("");
		getContentPane().add(lblD_18);
		lblD_18.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[18] = lblD_18;
		
		JLabel lblD_19 = new JLabel("");
		getContentPane().add(lblD_19);
		lblD_19.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[19] = lblD_19;
		
		JLabel lblD_20 = new JLabel("");
		getContentPane().add(lblD_20);
		lblD_20.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[20] = lblD_20;
		
		JLabel lblD_21 = new JLabel("");
		getContentPane().add(lblD_21);
		lblD_21.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[21] = lblD_21;
		
		JLabel lblD_22 = new JLabel("");
		getContentPane().add(lblD_22);
		lblD_22.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[22] = lblD_22;
		
		JLabel lblD_23 = new JLabel("");
		getContentPane().add(lblD_23);
		lblD_23.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[23] = lblD_23;
		
		JLabel lblD_24 = new JLabel("");
		getContentPane().add(lblD_24);
		lblD_24.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[24] = lblD_24;
		
		JLabel lblD_25 = new JLabel("");
		getContentPane().add(lblD_25);
		lblD_25.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[25] = lblD_25;
		
		JLabel lblD_26 = new JLabel("");
		getContentPane().add(lblD_26);
		lblD_26.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[26] = lblD_26;
		
		JLabel lblD_27 = new JLabel("");
		getContentPane().add(lblD_27);
		lblD_27.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[27] = lblD_27;
		
		JLabel lblD_28 = new JLabel("");
		getContentPane().add(lblD_28);
		lblD_28.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[28] = lblD_28;
		
		JLabel lblD_29 = new JLabel("");
		getContentPane().add(lblD_29);
		lblD_29.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[29] = lblD_29;
		
		JLabel lblD_30 = new JLabel("");
		getContentPane().add(lblD_30);
		lblD_30.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[30] = lblD_30;
		
		JLabel lblD_31 = new JLabel("");
		getContentPane().add(lblD_31);
		lblD_31.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[31] = lblD_31;
		
		JLabel lblD_32 = new JLabel("");
		getContentPane().add(lblD_32);
		lblD_32.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[32] = lblD_32;
		
		JLabel lblD_33 = new JLabel("");
		getContentPane().add(lblD_33);
		lblD_33.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[33] = lblD_33;
		
		JLabel lblD_34 = new JLabel("");
		getContentPane().add(lblD_34);
		lblD_34.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[34] = lblD_34;
		
		JLabel lblD_35 = new JLabel("");
		getContentPane().add(lblD_35);
		lblD_35.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[35] = lblD_35;
		
		JLabel lblD_36 = new JLabel("");
		getContentPane().add(lblD_36);
		lblD_36.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[36] = lblD_36;
		
		JLabel lblD_37 = new JLabel("");
		getContentPane().add(lblD_37);
		lblD_37.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[37] = lblD_37;
		
		JLabel lblD_38 = new JLabel("");
		getContentPane().add(lblD_38);
		lblD_38.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[38] = lblD_38;
		
		JLabel lblD_39 = new JLabel("");
		getContentPane().add(lblD_39);
		lblD_39.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[39] = lblD_39;
		
		JLabel lblD_40 = new JLabel("");
		getContentPane().add(lblD_40);
		lblD_40.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[40] = lblD_40;
		
		JLabel lblD_41 = new JLabel("");
		getContentPane().add(lblD_41);
		lblD_41.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[41] = lblD_41;
		
		JLabel lblD_42 = new JLabel("");
		getContentPane().add(lblD_42);
		lblD_42.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[42] = lblD_42;
		
		JLabel lblD_43 = new JLabel("");
		getContentPane().add(lblD_43);
		lblD_43.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[43] = lblD_43;
		
		JLabel lblD_44 = new JLabel("");
		getContentPane().add(lblD_44);
		lblD_44.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[44] = lblD_44;
		
		JLabel lblD_45 = new JLabel("");
		getContentPane().add(lblD_45);
		lblD_45.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[45] = lblD_45;
		
		JLabel lblD_46 = new JLabel("");
		getContentPane().add(lblD_46);
		lblD_46.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[46] = lblD_46;
		
		JLabel lblD_47 = new JLabel("");
		getContentPane().add(lblD_47);
		lblD_47.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[47] = lblD_47;
		
		JLabel lblD_48 = new JLabel("");
		getContentPane().add(lblD_48);
		lblD_48.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[48] = lblD_48;
		
		JLabel lblD_49 = new JLabel("");
		getContentPane().add(lblD_49);
		lblD_49.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[49] = lblD_49;
		
		JLabel lblD_50 = new JLabel("");
		getContentPane().add(lblD_50);
		lblD_50.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[50] = lblD_50;
		
		JLabel lblD_51 = new JLabel("");
		getContentPane().add(lblD_51);
		lblD_51.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[51] = lblD_51;
		
		JLabel lblD_52 = new JLabel("");
		getContentPane().add(lblD_52);
		lblD_52.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[52] = lblD_52;
		
		JLabel lblD_53 = new JLabel("");
		getContentPane().add(lblD_53);
		lblD_53.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[53] = lblD_53;
		
		JLabel lblD_54 = new JLabel("");
		getContentPane().add(lblD_54);
		lblD_54.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[54] = lblD_54;
		
		JLabel lblD_55 = new JLabel("");
		getContentPane().add(lblD_55);
		lblD_55.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[55] = lblD_55;
		
		JLabel lblD_56 = new JLabel("");
		getContentPane().add(lblD_56);
		lblD_56.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[56] = lblD_56;
		
		JLabel lblD_57 = new JLabel("");
		getContentPane().add(lblD_57);
		lblD_57.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[57] = lblD_57;
		
		JLabel lblD_58 = new JLabel("");
		getContentPane().add(lblD_58);
		lblD_58.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[58] = lblD_58;
		
		JLabel lblD_59 = new JLabel("");
		getContentPane().add(lblD_59);
		lblD_59.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[59] = lblD_59;
		
		JLabel lblD_60 = new JLabel("");
		getContentPane().add(lblD_60);
		lblD_60.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[60] = lblD_60;
		
		JLabel lblD_61 = new JLabel("");
		getContentPane().add(lblD_61);
		lblD_61.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[61] = lblD_61;
		
		JLabel lblD_62 = new JLabel("");
		getContentPane().add(lblD_62);
		lblD_62.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[62] = lblD_62;
		
		JLabel lblD_63 = new JLabel("");
		getContentPane().add(lblD_63);
		lblD_63.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[63] = lblD_63;
		
		JLabel lblD_64 = new JLabel("");
		getContentPane().add(lblD_64);
		lblD_64.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[64] = lblD_64;
		
		JLabel lblD_65 = new JLabel("");
		getContentPane().add(lblD_65);
		lblD_65.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[65] = lblD_65;
		
		JLabel lblD_66 = new JLabel("");
		getContentPane().add(lblD_66);
		lblD_66.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[66] = lblD_66;
		
		JLabel lblD_67 = new JLabel("");
		getContentPane().add(lblD_67);
		lblD_67.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[67] = lblD_67;
		
		JLabel lblD_68 = new JLabel("");
		getContentPane().add(lblD_68);
		lblD_68.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[68] = lblD_68;
		
		JLabel lblD_69 = new JLabel("");
		getContentPane().add(lblD_69);
		lblD_69.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[69] = lblD_69;
		
		JLabel lblD_70 = new JLabel("");
		getContentPane().add(lblD_70);
		lblD_70.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[70] = lblD_70;
		
		JLabel lblD_71 = new JLabel("");
		getContentPane().add(lblD_71);
		lblD_71.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[71] = lblD_71;
		
		JLabel lblD_72 = new JLabel("");
		getContentPane().add(lblD_72);
		lblD_72.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[72] = lblD_72;
		
		JLabel lblD_73 = new JLabel("");
		getContentPane().add(lblD_73);
		lblD_73.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[73] = lblD_73;
		
		JLabel lblD_74 = new JLabel("");
		getContentPane().add(lblD_74);
		lblD_74.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[74] = lblD_74;
		
		JLabel lblD_75 = new JLabel("");
		getContentPane().add(lblD_75);
		lblD_75.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[75] = lblD_75;
		
		JLabel lblD_76 = new JLabel("");
		getContentPane().add(lblD_76);
		lblD_76.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[76] = lblD_76;
		
		JLabel lblD_77 = new JLabel("");
		getContentPane().add(lblD_77);
		lblD_77.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[77] = lblD_77;
		
		JLabel lblD_78 = new JLabel("");
		getContentPane().add(lblD_78);
		lblD_78.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[78] = lblD_78;
		
		JLabel lblD_79 = new JLabel("");
		getContentPane().add(lblD_79);
		lblD_79.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[79] = lblD_79;
		
		JLabel lblD_80 = new JLabel("");
		getContentPane().add(lblD_80);
		lblD_80.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[80] = lblD_80;
		
		JLabel lblD_81 = new JLabel("");
		getContentPane().add(lblD_81);
		lblD_81.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[81] = lblD_81;
		
		JLabel lblD_82 = new JLabel("");
		getContentPane().add(lblD_82);
		lblD_82.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[82] = lblD_82;
		
		JLabel lblD_83 = new JLabel("");
		getContentPane().add(lblD_83);
		lblD_83.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[83] = lblD_83;
		
		JLabel lblD_84 = new JLabel("");
		getContentPane().add(lblD_84);
		lblD_84.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[84] = lblD_84;
		
		JLabel lblD_85 = new JLabel("");
		getContentPane().add(lblD_85);
		lblD_85.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[85] = lblD_85;
		
		JLabel lblD_86 = new JLabel("");
		getContentPane().add(lblD_86);
		lblD_86.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[86] = lblD_86;
		
		JLabel lblD_87 = new JLabel("");
		getContentPane().add(lblD_87);
		lblD_87.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[87] = lblD_87;
		
		JLabel lblD_88 = new JLabel("");
		getContentPane().add(lblD_88);
		lblD_88.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[88] = lblD_88;
		
		JLabel lblD_89 = new JLabel("");
		getContentPane().add(lblD_89);
		lblD_89.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[89] = lblD_89;
		
		JLabel lblD_90 = new JLabel("");
		getContentPane().add(lblD_90);
		lblD_90.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[90] = lblD_90;
		
		JLabel lblD_91 = new JLabel("");
		getContentPane().add(lblD_91);
		lblD_91.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[91] = lblD_91;
		
		JLabel lblD_92 = new JLabel("");
		getContentPane().add(lblD_92);
		lblD_92.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[92] = lblD_92;
		
		JLabel lblD_93 = new JLabel("");
		getContentPane().add(lblD_93);
		lblD_93.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[93] = lblD_93;
		
		JLabel lblD_94 = new JLabel("");
		getContentPane().add(lblD_94);
		lblD_94.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[94] = lblD_94;
		
		JLabel lblD_95 = new JLabel("");
		getContentPane().add(lblD_95);
		lblD_95.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[95] = lblD_95;
		
		JLabel lblD_96 = new JLabel("");
		getContentPane().add(lblD_96);
		lblD_96.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[96] = lblD_96;
		
		JLabel lblD_97 = new JLabel("");
		getContentPane().add(lblD_97);
		lblD_97.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[97] = lblD_97;
		
		JLabel lblD_98 = new JLabel("");
		getContentPane().add(lblD_98);
		lblD_98.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[98] = lblD_98;
		
		JLabel lblD_99 = new JLabel("");
		getContentPane().add(lblD_99);
		lblD_99.setFont(new Font("Monospaced", Font.PLAIN, 11));
		label[99] = lblD_99;
		
		getContentPane().addKeyListener(this);
	}
	
	public static Item getShipByID(int shipid) {
		for (Item i : Engine.presentItems) {
			if (i.id == shipid) {
				return i;
			}
		}
		return null;
	}
	
	// Thread que verifica se a nave pertencida morreu
	public static void isItDeadYet() {
		Thread t = new Thread(new Runnable() {
            public void run() { 
            	
            	while (true) {
            		System.out.print("");
            		if (Engine.lastcolision != null) {
            			if (Engine.lastcolision.id == myshipID) {
            				break;
            			}
            		}
            	}
            	Engine.deadShip(getShipByID(myshipID));
            	// TODO: Enviar mensagem para todos os outros players
            	
            	mp.enviarMensagem("D;");
            }
		});
		t.start();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		if (!Engine.presentItems.isEmpty()) {
		
			short key = Constants.IDLE;
	        char c = e.getKeyChar();
	        
	        switch(c) {
	        case 'w':	key = Constants.UP;
	        			break;
	        case 'a':	key = Constants.LEFT;
						break;
	        case 's':	key = Constants.DOWN;
						break;
	        case 'd':	key = Constants.RIGHT;
						break;
	        case ' ':	key = Constants.SHOOT;
						break;
			default:	key = Constants.IDLE;
						break;
	        }
	         
	        short shipOrientation;
	    	Item ship = null;
	    	int[] newpos = {0,0};
	    	Item fb;
	    	int shipIndex;
			synchronized (Engine.mutex_presentItems) {
				ship = getShipByID(myshipID);
				shipIndex = Engine.presentItems.indexOf(ship);
			}
			if (ship != null) {
				shipOrientation= ship.orientation;
				
				// Se a tecla corresponde à orientação, anda
				if (key == shipOrientation) {
				        				
					switch(key) {
					case Constants.LEFT:	newpos[0] = ship.position[0];
								newpos[1] = ship.position[1]-1;
								break;
					case Constants.RIGHT:	newpos[0] = ship.position[0];
								newpos[1] = ship.position[1]+1;
								break;
					case Constants.UP:	newpos[0] = ship.position[0]-1;
								newpos[1] = ship.position[1];
								break;
					case Constants.DOWN:	newpos[0] = ship.position[0]+1;
								newpos[1] = ship.position[1];
								break;			
					}
					
					// Garante que o ship não saia da tabela
					newpos[0] = Math.min(Constants.SIZE_V-1, Math.max(0, newpos[0]));
					newpos[1] = Math.min(Constants.SIZE_H-1, Math.max(0, newpos[1]));
					
					Engine.moveShip(ship, newpos, ship.orientation);
					
					// Envia mensagem p/ todos avisando que mudou posição
					mp.enviarMensagem("P;"+newpos[0]+","+newpos[1]+":"+ship.orientation);
					
				// Se a tecla for a de atirar, atire (se possível)
				} else if (key == Constants.SHOOT) {
					Engine.newFireball(ship);
					
					switch(ship.orientation) {
					case Constants.LEFT:	newpos[0] = ship.position[0];
								newpos[1] = ship.position[1]-1;
								break;
					case Constants.RIGHT:	newpos[0] = ship.position[0];
								newpos[1] = ship.position[1]+1;
								break;
					case Constants.UP:	newpos[0] = ship.position[0]-1;
								newpos[1] = ship.position[1];
								break;
					case Constants.DOWN:	newpos[0] = ship.position[0]+1;
								newpos[1] = ship.position[1];
								break;			
					}
					// Envia mensagem p/ todos avisando que criou fireball
					mp.enviarMensagem("F;"+newpos[0]+","+newpos[1]+":"+ship.orientation);
					
				// Se for alguma tecla não reconhe
				} else if (key == Constants.IDLE){
					// DO NOTHING
				// Se não, muda a orientação de acordo com a tecla            			
				} else if ((key >= Constants.LEFT) &&(key <= Constants.DOWN)) {
					Engine.moveShip(ship, ship.position, key);
					
					// Envia mensagem p/ todos avisando que mudou orientação
					mp.enviarMensagem("P;"+ship.position[0]+","+ship.position[1]+":"+key);
					
				} 
			}
		}
			
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
