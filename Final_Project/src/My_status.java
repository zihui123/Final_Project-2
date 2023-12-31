import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.awt.Color;

public class My_status extends JFrame {

	private User user = new User();
	private JTable join_table, recruit_table;
	private JLabel join_status, recruit_status;
	private JScrollPane scrollPane_1, scrollPane_2;
	private JButton back, submit;
	private MainPage main = new MainPage();
	private JPanel bPanel;

	public My_status(User users) {

		this.user = users;
		setTitle("My Status");
		createLayout();
		createJoinTable();
		createRecruitTable();

		back.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				main.setDefaultCloseOperation(EXIT_ON_CLOSE);
				main.setVisible(true);
				main.setSize(600, 500);
				main.setAccount(user);
				My_status.this.dispose();
				
			}
		});

		submit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				JDialog.setDefaultLookAndFeelDecorated(true);
				int response = JOptionPane.showConfirmDialog(null,
						"just to make sure you won't regrate to recruit them.", "Confirm",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (response == JOptionPane.NO_OPTION) {// 按否

					System.out.println("No button is clicked");

				} else if (response == JOptionPane.YES_OPTION) {// 按確認

					try (Connection conn = DriverManager.getConnection(user.url, user.usernameLogin, user.password)) {

						String leaderName = "";
						int courseIDs = 0;
						String stuName = "";
						int stuID = 0;

						int columnCount = recruit_table.getRowCount();
						for (int i = 0; i < columnCount; i++) {

							Boolean selected = (Boolean) recruit_table.getValueAt(i, 0); // 第i行第0列 就是checkBox

							if (selected) {

								int cID = (int) recruit_table.getValueAt(i, 1);
								courseIDs = cID;
								stuName = (String) recruit_table.getValueAt(i, 4);
								String a = (String) recruit_table.getValueAt(i, 6);
								stuID = Integer.parseInt(a);
								int groupID = (int) recruit_table.getValueAt(i, 2);// 第i行第1列 即groupID

								leaderName = (String) recruit_table.getValueAt(i, 3);
								String text = "學號 " + stuID + " 已被課程 " + courseIDs + " 課程組長 " + leaderName + "加入";

								String query = "SELECT Expected FROM GroupList WHERE GroupID = " + groupID;
								ResultSet s = user.stat.executeQuery(query);

								int origin_num = 0, new_num = 0;

								while (s.next()) {

									origin_num = Integer.parseInt(s.getString("Expected"));

								}

								new_num = origin_num - 1;
								query = "Update GroupList SET Expected = " + new_num + " WHERE GroupID = " + groupID;

								user.stat.execute(query);

								// *Sending email */
								String gmail = stuID + "@g.nccu.edu.tw";
								JavaMail mail = new JavaMail(gmail, "Member-Finding System", text);
								mail.SendMail();

								query = String.format("DELETE FROM Total_Register_List WHERE `StuID` = '%s'" ,stuID);
								user.stat.execute(query);
								System.out.println(String.format("成功從registers 刪掉%s",stuName));

								// 並且加進GroupList
								query = String.format("SELECT `Member` FROM `GroupList` WHERE `GroupID` = '%s'",
										groupID);
								ResultSet result = user.stat.executeQuery(query);

								if (result.next()) {

									String studentName = result.getString("Member");
									StringBuilder strbuild = new StringBuilder();
									strbuild.append(studentName);
									strbuild.append("," + stuName);
									query = "UPDATE GroupList SET Member = '" + strbuild.toString()
											+ "' WHERE GroupID = '" + groupID + "'";
									user.stat.execute(query);
									System.out.print("現在全部" + strbuild);
									System.out.println(String.format("有無成功從groupList加入%s", stuName));

									//*Back to main page
									main.setAccount(user);
									main.setDefaultCloseOperation(EXIT_ON_CLOSE);
									main.setVisible(true);
									main.setSize(600, 500);
									checkGroup(groupID);

								}
							}
						}


					} catch (SQLException e1) {

						e1.printStackTrace();

					}

					JOptionPane.showMessageDialog(null, "You have successfully added the members!",
							"Success", JOptionPane.INFORMATION_MESSAGE);

				} else if (response == JOptionPane.CLOSED_OPTION) {

					System.out.println("JOptionPane closed");

				}
			}
		});
	}

	public void createLayout() {

		back = new JButton("back");
		submit = new JButton("submit");

		join_table = new JTable();
		recruit_table = new JTable();
		join_status = new JLabel("join_status");
		recruit_status = new JLabel("recruit_status");

		scrollPane_1 = new JScrollPane(join_table);
		scrollPane_2 = new JScrollPane(recruit_table);
		scrollPane_2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane_2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
		getContentPane().setLayout(boxLayout);

		bPanel = new JPanel();
		bPanel.setBackground(new Color(210, 212, 207));
		bPanel.add(back);
		bPanel.add(submit);

		getContentPane().add(join_status);
		getContentPane().add(scrollPane_1);
		getContentPane().add(recruit_status);
		getContentPane().add(scrollPane_2);
		getContentPane().add(bPanel);

	}

	public void createJoinTable() {

		try (Connection conn = DriverManager.getConnection(user.url, user.usernameLogin, user.password)) {

			String stuID = user.userAccount;
			String query = String.format("SELECT * FROM `Total_Register_List` WHERE `StuID`= '%s'", stuID);

			PreparedStatement stat = conn.prepareStatement(query);
			ResultSet rs = stat.executeQuery();

			DefaultTableModel model = new DefaultTableModel() {
				// 指定每一欄的類型
				public Class<?> getColumnClass(int columnIndex) {

					return super.getColumnClass(columnIndex);

				}
			};

			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			// 加入欄位名稱
			for (int i = 1; i <= cols; i++) {

				model.addColumn(rsmd.getColumnName(i));

			}

			// 加入資料 要先創建每一行
			while (rs.next()) {

				Object[] rows = new Object[cols];

				for (int i = 1; i <= cols; i++) {

					rows[i - 1] = rs.getObject(i);

				}
				model.addRow(rows);

				// 檢查資料
				for (int i = 0; i < model.getRowCount(); i++) {

					for (int j = 0; j < model.getColumnCount(); j++) {

						Object value = model.getValueAt(i, j);
						System.out.println(value);

					}
				}
			}
			join_table.setModel(model);

			stat.close();
			conn.close();

		} catch (SQLException e1) {

			e1.printStackTrace();

		}
	}

	public void createRecruitTable() {

		try (Connection conn = DriverManager.getConnection(user.url, user.usernameLogin, user.password)) {

			String query = String.format("SELECT CourseID, GroupID, GroupName, StudentName, Department, StuID  FROM Total_Register_List WHERE `LeaderName` = '%s'", user.userName);

			PreparedStatement stat = conn.prepareStatement(query);
			ResultSet rs = stat.executeQuery(query);
			DefaultTableModel model = new DefaultTableModel() {
				// 指定每一欄的類型
				public Class<?> getColumnClass(int columnIndex) {

					if (columnIndex == 0) {

						return Boolean.class; // 第一欄設為布林類型
						
					} else {

						return super.getColumnClass(columnIndex);

					}
				}

				// 指定哪些欄位是可以編輯的
				public boolean isCellEditable(int row, int column) {

					return column == 0; // 只有第一欄是可編輯的

				}
			};
			model.addColumn("Select"); // 新增布林欄位
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			int cols = rsmd.getColumnCount();

			// 加入欄位名稱
			for (int i = 1; i <= cols; i++) {

				model.addColumn(rsmd.getColumnName(i));

			}

			// 加入資料 要先創建每一行
			while (rs.next()) {

				Object[] rows = new Object[cols + 1]; // 創建原database資料的欄位數加一
				rows[0] = false; // 預設未選中

				for (int i = 1; i <= cols; i++) {

					rows[i] = rs.getObject(i);// 將每一行添加到row

				}
				model.addRow(rows);
			}
			recruit_table.setModel(model);

			// 在每一行前面加 JCheckBox
			TableColumnModel columnModel = recruit_table.getColumnModel();// 取得負責管理表格的欄位設置
			TableColumn checkBoxColumn = columnModel.getColumn(0);// 取得欄位0 就是整欄的checkBox
			recruit_table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

			checkBoxColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));// 設置欄位編輯器是預設的，並初始化
			checkBoxColumn.setCellRenderer(recruit_table.getDefaultRenderer(Boolean.class));

			stat.close();
			conn.close();

		} catch (SQLException e1) {

			e1.printStackTrace();

		}
	}

	public void checkGroup(int groupID) {

		try {
			
			String query = String.format("DELETE FROM GroupList WHERE Expected = 0");
			user.stat.execute(query);
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}

	}
}
