import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.sql.*;
import java.util.ArrayList;

public class MainPage extends JFrame {
	private JLabel searchLabel;
	private JTextField searchField;
	private JScrollPane scrollPane;
	private JButton join, recruit, myStatus, searchButton, showAll;
	private ArrayList<Integer> selectedIDs;
	private JTable table_1;
	private User user = new User();
	private String query;
	private boolean checker = false;

	public MainPage() {
		createLabel();
		createButton();
		createTextField();
		createPanel();
		setTitle("Main Page");
		showAll();// 先秀出全部的課程

		// 如果使用者有想特別找的再打search
		searchButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				String search = searchField.getText();

				try {
					// Class.forName("com.mysql.cj.jdbc.Driver");
					try (Connection conn = DriverManager.getConnection(user.url, user.usernameLogin, user.password)) {

						query = "SELECT * FROM `Course_List` WHERE CourseID LIKE ? OR CourseName LIKE ? OR Teacher LIKE ?";
						// DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject","root","n2431836");
						PreparedStatement stat = conn.prepareStatement(query);// 互動性資料 index從1開始
						stat.setString(1, "%" + search + "%");// 有出現search就會抓回來
						stat.setString(2, "%" + search + "%");// _r% _是single character
						stat.setString(3, "%" + search + "%");
						ResultSet rs = stat.executeQuery(); // execute()回傳boolean, executeUpdate()回傳int 影響資料數

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

						table_1.setModel(model);

						// 在每一行前面加 JCheckBox
						TableColumnModel columnModel = table_1.getColumnModel();// 取得負責管理表格的欄位設置
						TableColumn checkBoxColumn = columnModel.getColumn(0);// 取得欄位0 就是整欄的checkBox
						checkBoxColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));// 設置欄位編輯器是預設的，並初始化
						checkBoxColumn.setCellRenderer(table_1.getDefaultRenderer(Boolean.class));

						stat.close();
						conn.close();

					} catch (Exception se) {

						se.printStackTrace();

					}

				} catch (Exception a) {
					a.printStackTrace();
				}
			}
		});
		join.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				checker = true;
				//setAccount(user);
				int columnCount = table_1.getRowCount();
				selectedIDs = new ArrayList<Integer>();

				for (int i = 0; i < columnCount; i++) {
					
					Boolean selected = (Boolean) table_1.getValueAt(i, 0); // 第i行第0列 就是checkBox
					if (selected) {

						//*Add */
						String courseName = (String) table_1.getValueAt(i, 2);
						user.courseName = courseName;
						
						int courseIDs = (int) table_1.getValueAt(i, 1);// 第i行第1列 即course_id
						user.courseID = courseIDs;
						
						selectedIDs.add(courseIDs);
						
						System.out.println("user courseName: " + user.courseName + " / user courseID: " + user.courseID);

						//*Group ID */
						try {

							String query = String.format("SELECT GroupID FROM GroupList");
							user.result = user.stat.executeQuery(query);
							query = String.format("SELECT GroupID FROM GroupList WHERE CourseID=%d", user.courseID);
							ResultSet r = user.stat.executeQuery(query);

							while (r.next()) {
								
								user.groupID = Integer.parseInt(r.getString("GroupID"));

							}

							System.out.println("user's GroupID = " + user.groupID);
							
						} catch (Exception ae) {

							ae.printStackTrace();

						}


					}
				}

				if (selectedIDs.isEmpty()) {

					JOptionPane.showMessageDialog(null, "Select some courses!", "Error", JOptionPane.ERROR_MESSAGE);
				
				} else {
					
					try {
						
						JoinPage join = new JoinPage(selectedIDs, user);
						join.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						join.setSize(1200, 500);
						join.setVisible(true);

					} catch (SQLException e1) {

						e1.printStackTrace();

					}

				}
			}
		});



		recruit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				checker = true;
				int columnCount = table_1.getRowCount();
				selectedIDs = new ArrayList<>();

				for (int i = 0; i < columnCount; i++) {

					Boolean selected = (Boolean) table_1.getValueAt(i, 0); // 第i行第0列 就是checkBox

					if (selected) {

						//*Add */
						String courseName = (String) table_1.getValueAt(i, 2);
						user.courseName = courseName;
						
						int courseIDs = (int) table_1.getValueAt(i, 1);// 第i行第1列 即course_id
						user.courseID = courseIDs;
						
						selectedIDs.add(courseIDs);
						
						System.out.println("user courseName: " + user.courseName + " / user courseID: " + user.courseID);

					}

					
				}

				if (selectedIDs.isEmpty()) {

					JOptionPane.showMessageDialog(null, "Select some courses!", "Error", JOptionPane.ERROR_MESSAGE);
				
				} else {

					System.out.println("user's name: " + user.userName + " / user's ID: " + user.userAccount);
					RecruitPage recruit = new RecruitPage(user); // *Send user account in */
					recruit.setVisible(true);
					recruit.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					recruit.setSize(600, 500);

				}
			}
		});
		myStatus.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (!checker) { //checker 判斷是不是直接按myStatus



				}
				My_status my_status = new My_status(user);
				my_status.setVisible(true);
				my_status.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				my_status.setSize(900, 500);

			}
		});
		showAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAll();
			}
		});

	}

	public void createLabel() {
		searchLabel = new JLabel("search");
	}

	public void createButton() {

		searchButton = new JButton("Search");
		join = new JButton("Join");
		recruit = new JButton("Recruit");
		myStatus = new JButton("my status");
		showAll = new JButton("show all");

	}

	public void createTextField() {

		searchField = new JTextField(25);
		searchField.setText("Search by course's name, ID, or teacher."); // 設定提示文字
		
	}

	public void showAll() {

		try (Connection conn = DriverManager.getConnection(user.url, user.usernameLogin, user.password)) {

			String query = "SELECT * FROM `Course_List` ";
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

			table_1.setModel(model);

			// 在每一行前面加 JCheckBox
			TableColumnModel columnModel = table_1.getColumnModel();// 取得負責管理表格的欄位設置
			TableColumn checkBoxColumn = columnModel.getColumn(0);// 取得欄位0 就是整欄的checkBox
			checkBoxColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));// 設置欄位編輯器是預設的，並初始化
			checkBoxColumn.setCellRenderer(table_1.getDefaultRenderer(Boolean.class));

			stat.close();
			conn.close();

		} catch (SQLException e1) {

			e1.printStackTrace();

		}
	}

	public void createPanel() {

		JPanel upPanel = new JPanel();
		upPanel.setBackground(new Color(214, 218, 212));
		upPanel.add(searchLabel);
		upPanel.add(searchField);
		upPanel.add(searchButton);
		upPanel.add(showAll);
		getContentPane().add(upPanel, BorderLayout.NORTH);

		table_1 = new JTable();
		scrollPane = new JScrollPane(table_1);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		JPanel downPanel = new JPanel();
		downPanel.setBackground(new Color(214, 218, 212));
		downPanel.setForeground(new Color(76, 76, 72));
		downPanel.add(join);
		downPanel.add(recruit);
		downPanel.add(myStatus);
		getContentPane().add(downPanel, BorderLayout.SOUTH);
		
	}

	public void setAccount(User localUser) {

		this.user = localUser;

	}

}
