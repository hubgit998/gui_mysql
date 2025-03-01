
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.swing.*;

/*
 * gui6では
 * 検索結果で表示されているレコードをCSVで保存する
 * （または画面に現在表示されているレコードを保存する）
 * おそらく現在の表示に導いたSQLでselectして保存するのが簡単
 */

public class CustomerManagerGUI6 {
    private static final String URL = "jdbc:mysql://localhost:8889/dbtesuto";
    private static final String USER = "root";
    private static final String PASS = "root";

    
	public static void main(String args[]) {
		// 顧客情報listにsqlを流し込み
	    ArrayList<Customer> customers = CustomerManagerGUI6.getCustomers();
	    try {
	        // OSに関係なく、クロスプラットフォームで統一された外観にする
	        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    } catch (UnsupportedLookAndFeelException | ClassNotFoundException |
	            InstantiationException | IllegalAccessException e) {
	        e.printStackTrace();
	    }
	   
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setLayout(null);
		
		matomePanel matome = new matomePanel(customers);
		frame.add(matome.titlePane);
		frame.add(matome.filedSP);
		frame.add(matome.massegePane);
		frame.add(matome.buttonPane);
		frame.setBackground(new Color(255,255,255));
		frame.setVisible(true);
		       
		matome.messUpdateTextArea("▶︎ 顧客一覧表示　顧客を追加　顧客を更新　顧客を削除　検索して更新　終了");
		matome.messUpdateTextArea("▶︎ 操作ボタンを押してください");
	
	}///////////////main ここまで//////////////////
	
	public static String getUrl() {
		return URL;
	}
	public static String getUser() {
		return USER;
	}
	public static String getPass() {
		return PASS;
	}
	
	//sqlを引き出してリストに注入　mainで使えるようにcustmをreturnしている
	public static ArrayList<Customer> getCustomers() {
	   final String SQL = "SELECT * FROM customers";
	   ArrayList<Customer> custm = new ArrayList<>();
	
	   try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
	        Statement stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(SQL)) {
	
	       // 顧客リストにデータを追加
	       while (rs.next()) {
	           custm.add(new Customer(
	                   rs.getInt("id"),
	                   rs.getString("name"),
	                   rs.getString("email"),
	                   rs.getString("phone"),
	                   rs.getTimestamp("created_at")));
	       }
	   } catch (SQLException e) {
	       e.printStackTrace();
	   }
	   return custm;
	}
	
	public static  ArrayList<Customer> searchCustomer(String name,String email,String phone, JTextArea mess) {
	   ArrayList<Customer> custm = new ArrayList<>();
	   if (name.isBlank() && email.isBlank() && phone.isBlank()) {
	            mess.append("▶︎ キーワードを入力してください。\n");
	            return custm;
	        }
	        final String SQL = "SELECT * FROM customers WHERE name LIKE ? OR email LIKE ? OR phone LIKE ?";
	        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
	             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
	        	if (name.equals("")) {
					pstmt.setString(1, "");	
				}else {
					pstmt.setString(1, "%" + name + "%");
				}
	        	if (email.equals("")) {
					pstmt.setString(2, "");
				}else {
					pstmt.setString(2, "%" + email + "%");
				}
	        	if (phone.equals("")) {
					pstmt.setString(3, "");	
				}else {
					pstmt.setString(3, "%" + phone + "%");
				}
	            try (ResultSet rs = pstmt.executeQuery()) {
	            boolean found = false;
	                while (rs.next()) {
	                custm.add(new Customer(
	                            rs.getInt("id") ,
	                            rs.getString("name") ,
	                            rs.getString("email") ,
	                            rs.getString("phone"),
	                            rs.getTimestamp("created_at")));
	                found = true;
	                }
	                if (!found) {
	                mess.append("▶︎ 顧客が見つかりませんでした。"+ "\n");
	                }
	            }catch (SQLException e) {
	           e.printStackTrace();
	            }
	        } catch (SQLException e) {
	            mess.append("▶︎ データベースのエラーが発生しました。詳細: " + e.getMessage() + "\n");
	        }
	   return custm;
	}
	
	static void insertCustomer(String name,String  email,String  phone){
       final String SQL = "INSERT INTO customers (name, email, phone, created_at) VALUES (?, ?, ?, NOW())";

       try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement pstmt = conn.prepareStatement(SQL)) {

           pstmt.setString(1, name);
           pstmt.setString(2, email);
           pstmt.setString(3, phone);

           int rowsInserted = pstmt.executeUpdate();
           if (rowsInserted > 0) {
            System.out.println ("新しい顧客が正常に追加されました。");
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
	}
	static void insertCustomer(String name, String email, String phone,matomePanel matomePanel) {
	    final String SQL = "INSERT INTO customers (name, email, phone, created_at) VALUES (?, ?, ?, NOW())";
	
	    try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
	
	        pstmt.setString(1, name);
	        pstmt.setString(2, email);
	        pstmt.setString(3, phone);
	
	        int rowsInserted = pstmt.executeUpdate();
	        if (rowsInserted > 0) {
	            try (ResultSet rs = pstmt.getGeneratedKeys()) {
	                if (rs.next()) {
	                    int id = rs.getInt(1);
				        matomePanel.messUpdateTextArea("▶︎ 新しい顧客が正常に追加されました。");
				        matomePanel.messUpdateTextArea("　 ID: "+id+"  |  Name: "+name+"  |  E-mail: "+email+"  |  Phone: "+phone);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
} //CustomerManagerGUI

//ここからは同じファイルにあるが別のclassです
//顧客情報のフィールドと設定
class Customer {
	int id;
	String name;
	String email;
	String phone;
	String timeStamp;
	Timestamp timeStampR;
	
	public Customer(int id, String name, String email, String phone, String timeStamp) {
	    this.id = id;
	    this.name = name;
	    this.email = email;
	    this.phone = phone;
	    this.timeStamp = timeStamp;
	}
	//SQL用
	public Customer(int id, String name, String email, String phone, Timestamp timeStampR) {
	    this.id = id;
	    this.name = name;
	    this.email = email;
	    this.phone = phone;
	    this.timeStampR = timeStampR;
	}
}
//GUIの設定

class matomePanel extends JPanel{
	//機能のタイトルを表示する
	JPanel titlePane = new JPanel();
	JLabel titleL = new JLabel();
	
	//フィールドを表示する部分
	JPanel filedMatome = new JPanel();
	JScrollPane filedSP = new JScrollPane(filedMatome);
	JTextField idT = new JTextField(5);
	JTextField nameT = new JTextField(15);
	JTextField emailT = new JTextField(15);
	JTextField phoneT = new JTextField(10);
	JTextField timeStamp = new JTextField(13);
	
	JPanel massegePane = new JPanel();
	JTextArea mess = new JTextArea();
	
	JPanel buttonPane = new JPanel();
	JButton btn1 = new JButton("顧客一覧表示");
	JButton btn2 = new JButton("顧客を追加");
	JButton btn3 = new JButton("顧客を更新");
	JButton btn4 = new JButton("顧客を削除");
	JButton btn5 = new JButton("検索して更新・削除");
	JButton btn7 = new JButton("表示内容保存");
	JButton btn8 = new JButton("全体一覧保存");
	JButton btn9 = new JButton("終了");
	
	String serchName;
	String serchEmail;
	String serchPhone;
	
	public matomePanel(ArrayList<Customer> customers) {
	   filedSP.getVerticalScrollBar().setUnitIncrement(16);  // スクロールの速さを細かく調整
	   filedSP.getVerticalScrollBar().setBlockIncrement(50);  // ページスクロールの速さを調整
	
		//タイトルエリア
		titlePane.setBackground(new Color(180,180,180));
		titlePane.setBounds(0, 0, 800, 50);
		titlePane.setBorder(null);
		
		// GridBagLayoutで中央揃えを設定
		titlePane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER; // コンポーネントを中央に揃える
		gbc.weightx = 1.0; // 横方向の余白を埋める
		gbc.weighty = 1.0; // 縦方向の余白を埋める
		
		titleL.setFont(new Font("ms gothic", Font.BOLD, 16));
		titleL.setText("顧客情報一覧");
		titlePane.add(titleL, gbc);
		
		        // フィールドエリア フィールドを設置してlistを流し込むまでを担当
		filedMatome.setLayout(new BoxLayout(filedMatome, BoxLayout.Y_AXIS));  // フィールドを縦に並べるレイアウトに設定
		int cnt=0;

        for (Customer customer : customers) {
            // 1行顧客ごとに動的にJTextFieldを追加
        JPanel filedPane = new JPanel();
        filedPane.setLayout(new FlowLayout(FlowLayout.CENTER));  // 横並びにするためのレイアウトに設定
        filedPane.setPreferredSize(new Dimension(800, 25));

        	// 顧客ごとに動的にJTextFieldを追加
           
            if (cnt % 2==0) {
                filedPane.setBackground(new Color(240,240,240));
			}else {
			    filedPane.setBackground(new Color(230,230,230));
			}
            filedPane.add(createTextField(String.valueOf(customer.id), 3,false));
            filedPane.add(createTextField(customer.name, 12,false));
            filedPane.add(createTextField(customer.email, 18,false));
            filedPane.add(createTextField(customer.phone, 13,false));
            filedPane.add(createTextField(String.valueOf(customer.timeStampR), 14,false));

            filedMatome.add(filedPane);
            cnt++;
        }
        filedSP.setBounds(0, 50, 800, 324);
        filedSP.setBorder(null);

        // 垂直スクロールバーを非表示にするが、スクロール自体はできるようにする
        filedSP.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(0, 0));  // 幅0で高さを0に設定
        filedSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       
        // フィールドエリア　終わり
       
	//メッセージエリア
	massegePane.setLayout(null);
	massegePane.setBackground(new Color(220,220,220));
	massegePane.setBounds(0,373,800,140);
	//テキストエリアの設定
	//mess.setText("こんにちは、世界!");
	mess.setOpaque(true); // 背景を透明化
	mess.setLineWrap(true);
	//スクロール化
	JScrollPane messageSP = new JScrollPane(mess);
	messageSP.setBorder(null);
	messageSP.setBounds(50, 20, 700, 100);
	// 垂直スクロールバーを非表示にするが、スクロール自体はできるようにする
	messageSP.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(0, 0));  // 幅0で高さを0に設定
	messageSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	
	massegePane.add(messageSP);
	
	//操作用ボタン
	
	buttonPane.setBounds(0,512,800,88);
	buttonPane.setLayout(new FlowLayout());
	buttonPane.add(btn1);
	buttonPane.add(btn2);
	buttonPane.add(btn3);
	buttonPane.add(btn4);
	buttonPane.add(btn5);
	buttonPane.add(btn7);
	buttonPane.add(btn8);
	buttonPane.add(btn9);
	//表示一覧保存は初期画面では隠す
	btn7.setVisible(false);
	
//////////

	btn1.addActionListener(e->{
		btn8.setVisible(true);
		btn7.setVisible(false);
		ArrayList<Customer> updateCustomers = CustomerManagerGUI6.getCustomers();
		updateCustomerList(updateCustomers);//再表示
		System.out.println();
		});

//////////
	btn2.addActionListener(e -> {
		btn8.setVisible(true);
		btn7.setVisible(false);
	   // フィールドエリアをクリア
	   filedMatome.removeAll();
	   
	   messUpdateTextArea("▶︎ 新しい顧客名、E-mail、電話番号を入力してください。");
	
	   // 新しい入力フォームを追加
	   JPanel inputPane = new JPanel();
	   inputPane.setLayout(new FlowLayout(FlowLayout.CENTER));
	
	   JTextField nameT = new JTextField(12);
	   JTextField emailT = new JTextField(18);
	   JTextField phoneT = new JTextField(13);
	   JButton addButton = new JButton("追加");
	
	   inputPane.add(nameT);
	   inputPane.add(emailT);
	   inputPane.add(phoneT);
	   inputPane.add(addButton);
	
	   filedMatome.add(inputPane);
	   filedMatome.revalidate(); // レイアウト更新
	   filedMatome.repaint();    // 再描画
	
	   // 追加ボタンの処理
	   addButton.addActionListener(ev -> {
	       String name = nameT.getText().trim();
	       String email = emailT.getText().trim();
	       String phone = phoneT.getText().trim();
	
	       if (!name.isEmpty() && !email.isEmpty() && !phone.isEmpty()) {
	           // データベースに追加
	           CustomerManagerGUI6.insertCustomer(name, email, phone,this);
	
	           // 更新後のリストを取得し、再表示
	           ArrayList<Customer> updateCustomers = CustomerManagerGUI6.getCustomers();
	           updateCustomerList(updateCustomers);
	       } else {
	           messUpdateTextArea("▶︎ すべてのフィールドを入力してください。");
	       }
	   });
	});
///////////////

//////////
	///一覧に更新ボタンを配置　修正したら更新ボタンで反映
	btn3.addActionListener(e -> {
		btn8.setVisible(true);
		btn7.setVisible(false);
		// フィールドエリアをクリア
		filedMatome.removeAll();
		//全表示して
		ArrayList<Customer> editCustomers = CustomerManagerGUI6.getCustomers();
		//更新ボタンを設置する
		messUpdateTextArea("▶︎ 顧客名、E-mail、電話番号を編集し更新ボタンを押してください");
		editCustomerList(editCustomers);//再表示
	
		//ここからdeleteCustomerList()で処理
});
///////////////

///顧客を削除　全一覧表ー＞削除ボタンでダイアログ警告後「はい」で削除
	btn4.addActionListener(e -> {
		btn8.setVisible(true);
		btn7.setVisible(false);
		// フィールドエリアをクリア
		filedMatome.removeAll();
		//全表示して
		ArrayList<Customer> deleteCustomers = CustomerManagerGUI6.getCustomers();
		//削除ボタンを設置する
		messUpdateTextArea("▶︎ 削除する行の削除ボタンを押してください");
		deleteCustomerList(deleteCustomers);//再表示
	
		//ここからdeleteCustomerList()で処理
});
//////////////////////
	
	//////////
	///検索をしてから
	///一覧から　修正更新ボタンで反映
	///一覧から　削除ボタンで反映
	btn5.addActionListener(e -> {
		btn8.setVisible(false);
		btn7.setVisible(false);
		// フィールドエリアをクリア
		filedMatome.removeAll();
		
		messUpdateTextArea("▶︎ 顧客名、E-mail、電話番号で検索してください");
		
		// 新しい入力フォームを追加
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JTextField nameT = new JTextField(12);
		JTextField emailT = new JTextField(18);
		JTextField phoneT = new JTextField(13);
		JButton seachButton = new JButton("検索");
		
		inputPane.add(nameT);
		inputPane.add(emailT);
		inputPane.add(phoneT);
		inputPane.add(seachButton);
		
		filedMatome.add(inputPane);
		filedMatome.revalidate(); // レイアウト更新
		filedMatome.repaint();    // 再描画
		
		// 検索ボタンの処理
		seachButton.addActionListener(ev -> {
			btn8.setVisible(false);
			btn7.setVisible(true);
			serchName = nameT.getText().trim();
			serchEmail = emailT.getText().trim();
			serchPhone = phoneT.getText().trim();
			messUpdateTextArea("▶︎ 顧客名、E-mail、電話番号を編集し更新ボタンまたは削除ボタンが選択できます。");
			/// データベースで検索値をセット
			///一覧から　修正更新ボタンで反映
			///一覧から　削除ボタンで反映
			aftersearchCustomerList(CustomerManagerGUI6.searchCustomer(serchName, serchEmail, serchPhone,mess));
			});
	});
///////////////ここは表示された内容だけを保存するボタン
	btn7.addActionListener(e -> {
		// 表示中の顧客データを取得 
		ArrayList<Customer> custmNow = new ArrayList<>();
		custmNow = CustomerManagerGUI6.searchCustomer(serchName, serchEmail, serchPhone,mess);
	
	    // ファイル選択ダイアログを表示
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("保存先を選択");  // ダイアログのタイトル
	    fileChooser.setSelectedFile(new File("表示一覧.csv"));  // 初期ファイル名（任意）

	    // ユーザーがファイルを選択した場合のみ保存処理を行う
	    int userSelection = fileChooser.showSaveDialog(null);
	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        // ユーザーが保存先を選択した場合の処理
	        File fileToSave = fileChooser.getSelectedFile();
	        
	        // ファイル名が.csvでなければ追加
	        String fileName = fileToSave.getName();
	        if (!fileName.endsWith(".csv")) {
	            fileToSave = new File(fileToSave.getParentFile(), fileName + ".csv");
	        }

	        // CSVファイルに書き込み処理
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
	            // CSVのヘッダーを書き込む
	            writer.write("ID,名前,メール,電話番号,タイムスタンプ\n");

	            // 顧客データをCSV形式で書き込む
	            for (Customer customer : custmNow) {
	                String customerData = customer.id + "," + customer.name + "," + customer.email + "," + customer.phone + "," + customer.timeStampR;
	                writer.write(customerData + "\n");
	            }

	            // 成功メッセージを表示
	            messUpdateTextArea("▶︎ 顧客データを「" + fileToSave.getAbsolutePath() + "」に保存しました。");

	        } catch (IOException ex) {
	            // エラーハンドリング
	            messUpdateTextArea("▶︎ 顧客データの保存に失敗しました: " + ex.getMessage());
	        }
	    }
	});
	
///////////////	

	btn8.addActionListener(e -> {
	    // 顧客データを取得
	    ArrayList<Customer> commoncustomers = CustomerManagerGUI6.getCustomers();
	    
	    // ファイル選択ダイアログを表示
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("保存先を選択");  // ダイアログのタイトル
	    fileChooser.setSelectedFile(new File("全体一覧.csv"));  // 初期ファイル名（任意）

	    // ユーザーがファイルを選択した場合のみ保存処理を行う
	    int userSelection = fileChooser.showSaveDialog(null);
	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        // ユーザーが保存先を選択した場合の処理
	        File fileToSave = fileChooser.getSelectedFile();
	        
	        // ファイル名が.csvでなければ追加
	        String fileName = fileToSave.getName();
	        if (!fileName.endsWith(".csv")) {
	            fileToSave = new File(fileToSave.getParentFile(), fileName + ".csv");
	        }

	        // CSVファイルに書き込み処理
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
	            // CSVのヘッダーを書き込む
	            writer.write("ID,名前,メール,電話番号,タイムスタンプ\n");

	            // 顧客データをCSV形式で書き込む
	            for (Customer customer : commoncustomers) {
	                String customerData = customer.id + "," + customer.name + "," + customer.email + "," + customer.phone + "," + customer.timeStampR;
	                writer.write(customerData + "\n");
	            }

	            // 成功メッセージを表示
	            messUpdateTextArea("▶︎ 顧客データを「" + fileToSave.getAbsolutePath() + "」に保存しました。");

	        } catch (IOException ex) {
	            // エラーハンドリング
	            messUpdateTextArea("▶︎ 顧客データの保存に失敗しました: " + ex.getMessage());
	        }
	    }
	});
	
///////////////	
	btn9.addActionListener(e->System.exit(0));

}
public void editCustomerList(ArrayList<Customer> customers) {
	//内容を編集更新するメソッド
	// filedMatomeをクリア
	filedMatome.removeAll();
	// フィールドエリア フィールドを設置してlistを流し込むまでを担当
	filedMatome.setLayout(new BoxLayout(filedMatome, BoxLayout.Y_AXIS));  // フィールドを縦に並べるレイアウトに設定
		int cnt=0;
	    for (Customer customer : customers) {
	        // 1行顧客ごとに動的にJTextFieldを追加
	    JPanel filedPane = new JPanel();
	    filedPane.setLayout(new FlowLayout(FlowLayout.CENTER));  // 横並びにするためのレイアウトに設定
	
	    // 顧客ごとに動的にJTextFieldを追加
	        JTextField idT = new JTextField(String.valueOf(customer.id), 3);
	        idT.setEditable(false);  // 編集不可に設定
	        idT.setFocusable(false);   // 選択不可
	        JTextField nameT = new JTextField(customer.name, 12);
	        nameT.setName("nameT"); // 名前をセット
	        JTextField emailT = new JTextField(customer.email, 18);
	        emailT.setName("emailT");
	        JTextField phoneT = new JTextField(customer.phone, 13);
	        phoneT.setName("phoneT");
	        nameT.putClientProperty("customer_id", customer.id);
	        emailT.putClientProperty("customer_id", customer.id);
	        phoneT.putClientProperty("customer_id", customer.id);
	   
	        JButton updateButton = new JButton("更新");
	        updateButton.putClientProperty("customer_id", customer.id);
	        // 更新ボタンの処理 各行（レコード）のIDを持ち、当該レコードのみ更新
	        //同じレコードにあるボタンでのみ更新
	        updateButton.addActionListener(e -> {
	            JButton button = (JButton) e.getSource();
	            Container  parent =  button.getParent(); // buttonが配置されているJPanelなどのコンテナを取得
	    
	            //IDを取得
	            Object idObj = button.getClientProperty("customer_id");
	            int id = -1;  // idを-1で初期化しておく
	            if (idObj instanceof Integer) {
	                id = (Integer) idObj;
	            } else {
	                System.out.println("▶︎ IDの取得に失敗");
	                return;
	            }
	            // テキストフィールドを名前で取得 (getChildCount()とgetComponent()をループで回して名前で判断する)
	            String name = "";
	            String email = "";
	            String phone = "";
	
	            for (int i = 0; i < parent.getComponentCount(); i++) {
	                Component c = parent.getComponent(i);
	                if (c instanceof JTextField) {
	                    JTextField textField = (JTextField) c;
	                    String fieldName = textField.getName();  // getName()を一度取得
	
	                    // nullチェックと名前による判断
	                    if (fieldName != null) {
	                        if (fieldName.equals("nameT")) {
	                            name = textField.getText();
	                        } else if (fieldName.equals("emailT")) {
	                            email = textField.getText();
	                        } else if (fieldName.equals("phoneT")) {
	                            phone = textField.getText();
	                        }
	                    }
	                }
	            }
	            // データベースで検索値をセット
	            // SQL UPDATE文を実行
	            String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
	            try (Connection conn = DriverManager.getConnection(CustomerManagerGUI6.getUrl(), CustomerManagerGUI6.getUser(), CustomerManagerGUI6.getPass());
	            	 PreparedStatement pstmt = conn.prepareStatement(sql)) {
	                pstmt.setString(1, name);
	                pstmt.setString(2, email);
	                pstmt.setString(3, phone);
	                pstmt.setInt(4, id);
	                pstmt.executeUpdate();
	                messUpdateTextArea("▶︎ 顧客情報が更新されました。");
	                messUpdateTextArea("　 ID: "+id+"  |  Name: "+name+"  |  E-mail: "+email+"  |  Phone: "+phone);
	            } catch (SQLException ex) {
	            	ex.printStackTrace();
	                messUpdateTextArea("データベースエラー: " + ex.getMessage());
	            }
	            // 更新のみ行うリストの再表示は行わない。
	        });

	        if (cnt % 2==0) {
	            filedPane.setBackground(new Color(240,240,240));
			}else {
			    filedPane.setBackground(new Color(230,230,230));
			}
	        filedPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
	        
            // 顧客ごとに動的にJTextFieldを追加//各行のフィールドにidを設置している
            JTextField idField = createTextField(String.valueOf(customer.id),3,false,false);
            JTextField nameField = createTextField(customer.name, 12,false,false);
            nameField.setName("nameT");
            JTextField emailField = createTextField(customer.email, 18,false,false);
            emailField.setName("emailT");
            JTextField phoneField = createTextField(customer.phone, 13,false,false);
            phoneField.setName("phoneT");
            filedPane.add(idField);
            filedPane.add(nameField);
            filedPane.add(emailField);
            filedPane.add(phoneField);
	        filedPane.add(updateButton);

	        filedMatome.add(filedPane);
	        cnt++;
	    }
	    filedMatome.revalidate();
	    filedMatome.repaint();
	}
	public void aftersearchCustomerList(ArrayList<Customer> customers) {
		// filedMatomeをクリア
	    filedMatome.removeAll();
	    // フィールドエリア フィールドを設置してlistを流し込むまでを担当
		filedMatome.setLayout(new BoxLayout(filedMatome, BoxLayout.Y_AXIS));  // フィールドを縦に並べるレイアウトに設定
		int cnt=0;
	    for (Customer customer : customers) {
	        // 1行顧客ごとに動的にJTextFieldを追加
	    JPanel filedPane = new JPanel();
	    filedPane.setLayout(new FlowLayout(FlowLayout.CENTER));  // 横並びにするためのレイアウトに設定
	
	    // 顧客ごとに動的にJTextFieldを追加
	        JTextField idT = new JTextField(String.valueOf(customer.id), 3);
	        idT.setEditable(false);  // 編集不可に設定
	        idT.setFocusable(false);   // 選択不可
	        JTextField nameT = new JTextField(customer.name, 12);
	        nameT.setName("nameT"); // 名前をセット
	        JTextField emailT = new JTextField(customer.email, 18);
	        emailT.setName("emailT");
	        JTextField phoneT = new JTextField(customer.phone, 13);
	        phoneT.setName("phoneT");
	        nameT.putClientProperty("customer_id", customer.id);
	        emailT.putClientProperty("customer_id", customer.id);
	        phoneT.putClientProperty("customer_id", customer.id);
	   
	        JButton updateButton = new JButton("更新");
	        updateButton.putClientProperty("customer_id", customer.id);
	        
	        JButton deleteButton = new JButton("削除");
	        deleteButton.putClientProperty("customer_id", customer.id);
	        //削除ボタンリスナー
	        //更新ボタンの処理 各行（レコード）のIDを持ち、当該レコードのみ更新
	        //同じレコードにあるボタンでのみ更新
	        updateButton.addActionListener(e -> {
	            JButton button = (JButton) e.getSource();
	            Container  parent =  button.getParent(); // buttonが配置されているJPanelなどのコンテナを取得
	    
	            //IDを取得
	            Object idObj = button.getClientProperty("customer_id");
	            int id = -1;  // idを-1で初期化しておく
	            if (idObj instanceof Integer) {
	                id = (Integer) idObj;
	            } else {
	                System.out.println("▶︎ IDの取得に失敗");
	                return;
	            }
	
	            // テキストフィールドを名前で取得 (getChildCount()とgetComponent()をループで回して名前で判断する)
	            String name = "";
	            String email = "";
	            String phone = "";
	
	            for (int i = 0; i < parent.getComponentCount(); i++) {
	                Component c = parent.getComponent(i);
	                if (c instanceof JTextField) {
	                    JTextField textField = (JTextField) c;
	                    String fieldName = textField.getName();  // getName()を一度取得
	
	                    // nullチェックと名前による判断
	                    if (fieldName != null) {
	                        if (fieldName.equals("nameT")) {
	                            name = textField.getText();
	                        } else if (fieldName.equals("emailT")) {
	                            email = textField.getText();
	                        } else if (fieldName.equals("phoneT")) {
	                            phone = textField.getText();
	                        }
	                    }
	                }
	            }
	            
	            // データベースで検索値をセット
	            // SQL UPDATE文を実行
	            String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
	            try (Connection conn = DriverManager.getConnection(CustomerManagerGUI6.getUrl(), CustomerManagerGUI6.getUser(), CustomerManagerGUI6.getPass());
	            	 PreparedStatement pstmt = conn.prepareStatement(sql)) {
	                pstmt.setString(1, name);
	                pstmt.setString(2, email);
	                pstmt.setString(3, phone);
	                pstmt.setInt(4, id);
	                pstmt.executeUpdate();
	                messUpdateTextArea("▶︎ 顧客情報が更新されました。");
	                messUpdateTextArea("　 ID: "+id+"  |  Name: "+name+"  |  E-mail: "+email+"  |  Phone: "+phone);
	            } catch (SQLException ex) {
	            	ex.printStackTrace();
	                messUpdateTextArea("データベースエラー: " + ex.getMessage());
	            }
	            // 更新のみ行うリストの再表示は行わない。
	        });
	        
	        //削除ボタンリスナー
	        deleteButton.addActionListener(e -> {
	        	///削除ダイアログ
	            int result = JOptionPane.showConfirmDialog(
	                    null,
	                    "本当に削除してよろしいですか？",
	                    "確認",
	                    JOptionPane.YES_NO_OPTION,
	                    JOptionPane.WARNING_MESSAGE
	                );
	
	                // 「いいえ」の場合、処理を終了
	                if (result != JOptionPane.YES_OPTION) {
	                    return;
	                }
	            JButton button = (JButton) e.getSource();
	            Container  parent =  button.getParent(); // buttonが配置されているJPanelなどのコンテナを取得
	    
	            //IDを取得
	            Object idObj = button.getClientProperty("customer_id");
	            int id = -1;  // idを-1で初期化しておく
	            if (idObj instanceof Integer) {
	                id = (Integer) idObj;
	            } else {
	                System.out.println("▶︎ IDの取得に失敗");
	                return;
	            }
	
	            // テキストフィールドを名前で取得 (getChildCount()とgetComponent()をループで回して名前で判断する)
	            String name = "";
	            String email = "";
	            String phone = "";
	
	            for (int i = 0; i < parent.getComponentCount(); i++) {
	                Component c = parent.getComponent(i);
	                if (c instanceof JTextField) {
	                    JTextField textField = (JTextField) c;
	                    String fieldName = textField.getName();  // getName()を一度取得
	
	                    // nullチェックと名前による判断
	                    if (fieldName != null) {
	                        if (fieldName.equals("nameT")) {
	                            name = textField.getText();
	                        } else if (fieldName.equals("emailT")) {
	                            email = textField.getText();
	                        } else if (fieldName.equals("phoneT")) {
	                            phone = textField.getText();
	                        }
	                    }
	                }
	            }
	            
	            // データベースでIDを元にDELETE処理
	            // SQL DELETE文を実行
	            String sql = "DELETE FROM customers WHERE id = ?";
	            try (Connection conn = DriverManager.getConnection(CustomerManagerGUI6.getUrl(), CustomerManagerGUI6.getUser(), CustomerManagerGUI6.getPass());
	            	 PreparedStatement pstmt = conn.prepareStatement(sql)) {
	                pstmt.setInt(1, id);
	                pstmt.executeUpdate();
	                messUpdateTextArea("▶︎ 顧客情報を削除しました。");
	                messUpdateTextArea("　 ID: "+id+"  |  Name: "+name+"  |  E-mail: "+email+"  |  Phone: "+phone);
	            } catch (SQLException ex) {
	            	ex.printStackTrace();
	                messUpdateTextArea("データベースエラー: " + ex.getMessage());
	            }
	            // 更新のみ行うリストの再表示は行わない。
	        	filedMatome.removeAll();
	        	//○今の結果一覧を取得したい　×全表示して
	        	aftersearchCustomerList(CustomerManagerGUI6.searchCustomer(serchName,serchEmail,serchPhone,mess));
	        	//削除ボタンを設置する
	        	messUpdateTextArea("▶︎ 更新または削除の処理が選択できます。");
	        });
	        //
	
	        if (cnt % 2==0) {
	            filedPane.setBackground(new Color(240,240,240));
			}else {
				filedPane.setBackground(new Color(230,230,230));
			}
	        filedPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
            // 顧客ごとに動的にJTextFieldを追加//各行のフィールドにidを設置している
            JTextField idField = createTextField(String.valueOf(customer.id),3,false,false);
            JTextField nameField = createTextField(customer.name, 12,false,false);
            nameField.setName("nameT");
            JTextField emailField = createTextField(customer.email, 18,false,false);
            emailField.setName("emailT");
            JTextField phoneField = createTextField(customer.phone, 13,false,false);
            phoneField.setName("phoneT");
            filedPane.add(idField);
            filedPane.add(nameField);
            filedPane.add(emailField);
            filedPane.add(phoneField);
	        
	        filedPane.add(updateButton);
	        filedPane.add(deleteButton);
	
	        filedMatome.add(filedPane);
	        cnt++;
	    }
	    filedMatome.revalidate();
	    filedMatome.repaint();
	}
	public void messUpdateTextArea(String newText) {
        mess.append(newText+"\n");  // ここでテキストが更新される
        mess.setCaretPosition(mess.getDocument().getLength()); 
    }
	public void deleteCustomerList(ArrayList<Customer> customers) {
		// filedMatomeをクリア
		// フィールドエリア フィールドを設置してlistを流し込むまでを担当
	    filedMatome.removeAll();
		filedMatome.setLayout(new BoxLayout(filedMatome, BoxLayout.Y_AXIS));  // フィールドを縦に並べるレイアウトに設定
	    // 1行顧客ごとに動的にJTextFieldを追加
	    // 顧客ごとに動的にJTextFieldを追加//各行のフィールドにidを設置している
		int cnt=0;
	    for (Customer customer : customers) {
	        JPanel filedPane = new JPanel(new FlowLayout(FlowLayout.CENTER));// 横並びにするためのレイアウトに設定
	        JButton delButton = new JButton("削除");
	        delButton.putClientProperty("customer_id", customer.id);
	        delButton.addActionListener(e -> {
        	///削除ダイアログ
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "本当に削除してよろしいですか？",
                    "確認",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                // 「いいえ」の場合、処理を終了
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            JButton button = (JButton) e.getSource();
            Container parent =  button.getParent(); // buttonが配置されているJPanelなどのコンテナを取得

          //IDを取得
            Object idObj = button.getClientProperty("customer_id");
            int id = -1;  // idを-1で初期化しておく
            if (idObj instanceof Integer) {
                id = (Integer) idObj;
            } else {
                System.out.println("▶︎ IDの取得に失敗");
                return;
            }
            // テキストフィールドを名前で取得 (getChildCount()とgetComponent()をループで回して名前で判断する)
            String name = "";
            String email = "";
            String phone = "";

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (c instanceof JTextField) {
                    JTextField textField = (JTextField) c;
                    String fieldName = textField.getName();  // getName()を一度取得

                    // nullチェックと名前による判断
                    if (fieldName != null) {
                        if (fieldName.equals("nameT")) {
                            name = textField.getText();
                        } else if (fieldName.equals("emailT")) {
                            email = textField.getText();
                        } else if (fieldName.equals("phoneT")) {
                            phone = textField.getText();
                        }
                    }
                }
            }
            
            // データベースでIDを元にDELETE処理
            // SQL DELETE文を実行
            String sql = "DELETE FROM customers WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(CustomerManagerGUI6.getUrl(), CustomerManagerGUI6.getUser(), CustomerManagerGUI6.getPass());
            	 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                messUpdateTextArea("▶︎ 顧客情報を削除しました。");
                messUpdateTextArea("　 ID: "+id+"  |  Name: "+name+"  |  E-mail: "+email+"  |  Phone: "+phone);
            } catch (SQLException ex) {
            	ex.printStackTrace();
                messUpdateTextArea("データベースエラー: " + ex.getMessage());
            }
            // 更新のみ行うリストの再表示は行わない。
        	filedMatome.removeAll();
        	//全表示して
        	ArrayList<Customer> deleteCustomers = CustomerManagerGUI6.getCustomers();
        	//削除ボタンを設置する
        	messUpdateTextArea("▶︎ 削除する行の削除ボタンを押してください");
        	deleteCustomerList(deleteCustomers);//再表示
        });
            if (cnt % 2==0) {
                filedPane.setBackground(new Color(240,240,240));
			}else {
			    filedPane.setBackground(new Color(230,230,230));
			}
            filedPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
            // 顧客ごとに動的にJTextFieldを追加//各行のフィールドにidを設置している
            JTextField idField = createTextField(String.valueOf(customer.id),3,true,true);
            JTextField nameField = createTextField(customer.name, 12,true,true);
            nameField.setName("nameT");
            JTextField emailField = createTextField(customer.email, 18,true,true);
            emailField.setName("emailT");
            JTextField phoneField = createTextField(customer.phone, 13,true,true);
            phoneField.setName("phoneT");
            filedPane.add(idField);
            filedPane.add(nameField);
            filedPane.add(emailField);
            filedPane.add(phoneField);
            filedPane.add(delButton);
            filedMatome.add(filedPane);
            cnt++;
	    }
	    filedMatome.revalidate();
	    filedMatome.repaint();
	}
	//JTextFieldの共通メソッド
	private JTextField createTextField(String text, int columns, boolean isReadOnly, boolean setName) {
	 JTextField textField = new JTextField(text, columns);
	 textField.setBorder(null);
	 textField.setBackground(null);
	 textField.setEditable(!isReadOnly);
	 textField.setFocusable(!isReadOnly);
	 if (setName) textField.setName(text);
	 return textField;
	}
	public void updateCustomerList(ArrayList<Customer> customers) {
		// filedMatomeをクリア
        filedMatome.removeAll();
        // フィールドエリア フィールドを設置してlistを流し込むまでを担当
		filedMatome.setLayout(new BoxLayout(filedMatome, BoxLayout.Y_AXIS));  // フィールドを縦に並べるレイアウトに設定
		int cnt=0;
        for (Customer customer : customers) {
            // 1行顧客ごとに動的にJTextFieldを追加
        	JPanel filedPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        	// 顧客ごとに動的にJTextFieldを追加
           
            if (cnt % 2==0) {
                filedPane.setBackground(new Color(240,240,240));
			}else {
			    filedPane.setBackground(new Color(230,230,230));
			}
           
            filedPane.add(createTextField(String.valueOf(customer.id), 3,false));
            filedPane.add(createTextField(customer.name, 12,false));
            filedPane.add(createTextField(customer.email, 18,false));
            filedPane.add(createTextField(customer.phone, 13,false));
            filedPane.add(createTextField(String.valueOf(customer.timeStampR), 14,false));

            filedMatome.add(filedPane);
            cnt++;
        }
        filedMatome.revalidate();
        filedMatome.repaint();
	}
	// JTextFieldの共通設定を行うメソッド
	private JTextField createTextField(String text, int columns, boolean isReadOnly) {
	    JTextField textField = new JTextField(text, columns);
	    textField.setBorder(null);
	    textField.setBackground(null);
	    if (isReadOnly) {
	        textField.setEditable(false);
	    }
	    return textField;
	}
}