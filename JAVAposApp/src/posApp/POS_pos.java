package posApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class POS_pos extends JPanel implements ActionListener {

	ItemDAO dao = ItemDAO.getInstance();

	JButton btnDB;
	JLabel lblItem;
	JComboBox cmbBox;
	JLabel lblStock;
	JTextField txtStock;
	JLabel lblTotal;
	JTextField txtTotal;
	JButton btnAdd;
	JButton btnPay;
	JButton btnCancel;
	JTable jTableItem;

	DefaultTableModel tableModel = new DefaultTableModel();
	DefaultComboBoxModel comboModel = new DefaultComboBoxModel();

	public POS_pos() {

		tableModel.addColumn("id");
		tableModel.addColumn("상품명");
		tableModel.addColumn("구매개수");
		tableModel.addColumn("물품가격");
		jTableItem = new JTable(tableModel);
		JScrollPane jtable = new JScrollPane(jTableItem);

		setLayout(null);

		btnDB = new JButton("제품 불러오기");
		btnDB.setBounds(20, 20, 140, 40);

		lblItem = new JLabel("상품");
		lblItem.setBounds(20, 90, 100, 30);

		cmbBox = new JComboBox();
		cmbBox.setBounds(70, 90, 200, 30);

		lblStock = new JLabel("수량");
		lblStock.setBounds(20, 140, 100, 30);

		txtStock = new JTextField();
		txtStock.setBounds(70, 140, 200, 30);

		lblTotal = new JLabel("총가격");
		lblTotal.setBounds(20, 250, 100, 40);

		txtTotal = new JTextField();
		txtTotal.setBounds(70, 250, 200, 40);

		btnAdd = new JButton("추가");
		btnAdd.setBounds(170, 190, 100, 40);

		btnPay = new JButton("결재");
		btnPay.setBounds(300, 250, 100, 40);

		btnCancel = new JButton("취소");
		btnCancel.setBounds(410, 250, 100, 40);

		jTableItem = new JTable(tableModel);
		jTableItem.setBounds(300, 20, 210, 210);

		// JPanel에 추가
		add(btnDB);
		add(lblItem);
		add(cmbBox);
		add(lblStock);
		add(txtStock);
		add(lblTotal);
		add(txtTotal);
		add(btnAdd);
		add(btnPay);
		add(btnCancel);
		add(jTableItem);

		btnDB.addActionListener(this);
		btnAdd.addActionListener(this);
		btnPay.addActionListener(this);
		btnCancel.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String buttonText = e.getActionCommand();

		String name = "";
		String stock = "";
		String price = "";

		if (buttonText == "제품 불러오기") {

			cmbBox.removeAllItems();

			Vector<String> itemlist = dao.getItem();
			for (int i = 0; i < itemlist.size(); i++) {
				cmbBox.addItem(itemlist.get(i));
			}

		} else if (buttonText == "추가") {

			name = cmbBox.getSelectedItem().toString();
			stock = txtStock.getText();
			int index = tableModel.getRowCount() + 1;
			int total = 0;

			try {
				price = "" + (Integer.parseInt(stock) * Integer.parseInt(dao.getPrice(name)));

				Vector<String> in = new Vector<String>();
				in.add("" + index);
				in.add(name);
				in.add(stock);
				in.add(price);

				tableModel.addRow(in);
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					total += Integer.parseInt((String) tableModel.getValueAt(i, 3));
					txtTotal.setText("" + total);
				}

			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (buttonText == "결재") {
			try {
				int res;
				res = JOptionPane.showConfirmDialog(null, "결재하시겠습니까?");
				if (res == 0) {
					JOptionPane.showConfirmDialog(null, "총금액은 " + txtTotal.getText() + "입니다");
					String money = JOptionPane.showInputDialog("");
					if (Integer.parseInt(money) >= Integer.parseInt(txtTotal.getText())) {
						JOptionPane.showConfirmDialog(null,"지불하신 금액은 " + money + "이고\n상품의 합계는 " + txtTotal.getText() + "이며,\n거스름돈은 "
										+ (Integer.parseInt(money) - Integer.parseInt(txtTotal.getText())) + "입니다");
						stockUpdate(tableModel);
						clean();
					} else {
						JOptionPane.showConfirmDialog(null, "금액이 적습니다");
					}
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else {
			int res;
			res = JOptionPane.showConfirmDialog(null, "주문을 취소하시겠습니까?");
			if (res == 0) {
				clean();
			}
		}
	}

	public void clean() {
		int rows = tableModel.getRowCount();
		for (int i = rows - 1; i >= 0; i--) {
			tableModel.removeRow(i);
		}
		txtStock.setText("");
		txtTotal.setText("");
	}

	public void stockUpdate(DefaultTableModel model) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "update item set item_stock=?-? where item_name=?";

		try {
			conn = DBConnect.connect();
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < model.getRowCount(); i++) {
				pstmt.setString(1, dao.getStock(model.getValueAt(i, 1).toString()));
				pstmt.setString(2, model.getValueAt(i, 2).toString());
				pstmt.setString(3, model.getValueAt(i, 1).toString());
				pstmt.executeUpdate();
			}

		} catch (Exception e) {
			System.out.println("DB 연결 또는  SQL 에러!");
		} finally {
			pstmt.close();
			conn.close();
		}

	}
}
