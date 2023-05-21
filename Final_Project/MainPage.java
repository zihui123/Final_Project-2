import java.awt.*;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


import java.awt.event.*;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import java.sql.*;
import java.util.ArrayList;
//資料叫courses
public class MainPage extends JFrame{
	private JLabel searchLabel;
	private JTextField searchField;
	private JScrollPane scrollPane;
	private Connection conn;
	private JButton join, recruit, myStatus, searchButton;
	private ArrayList<Integer> selectedIDs;
	private JTable table_1;
	
	public MainPage() {
		createLabel();
		createButton();
		createTextField();
		createPanel();
		showAll();//先秀出全部的課程
		
		//如果使用者有想特別找的再打search
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String search = searchField.getText();
				
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject","root","n2431836");
					String query = "SELECT * FROM `courses` WHERE name LIKE ? OR id LIKE ? OR teacher LIKE ?";
					PreparedStatement stat = conn.prepareStatement(query);
					stat.setString(1, "%" + search + "%");
			        stat.setString(2, "%" + search + "%");
			        stat.setString(3, "%" + search + "%");
			        ResultSet rs = stat.executeQuery();

                    DefaultTableModel model = new DefaultTableModel() {
                    	
                        @Override//指定每一欄的類型
                        public Class<?> getColumnClass(int columnIndex) {
                            if (columnIndex == 0) {
                                return Boolean.class; // 第一欄設為布林類型
                            } else {
                                return super.getColumnClass(columnIndex);
                            }
                        }

                        @Override//指定哪些欄位是可以編輯的
                        public boolean isCellEditable(int row, int column) {
                            return column == 0; // 只有第一欄是可編輯的
                        }
                    };

                    model.addColumn("Select"); // 新增布林欄位

                    ResultSetMetaData rsmd = (ResultSetMetaData)rs.getMetaData();
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
                            rows[i] = rs.getObject(i);//將每一行添加到row
                        }
                        model.addRow(rows);
                    }

                    table_1.setModel(model);
                    
                 // 在每一行前面加 JCheckBox
                    TableColumnModel columnModel = table_1.getColumnModel();//取得負責管理表格的欄位設置
                    TableColumn checkBoxColumn = columnModel.getColumn(0);//取得欄位0 就是整欄的checkBox
                    checkBoxColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));//設置欄位編輯器是預設的，並初始化
                    checkBoxColumn.setCellRenderer(table_1.getDefaultRenderer(Boolean.class));

                    stat.close();
                    conn.close();
		        } catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		join.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int columnCount = table_1.getRowCount();
				selectedIDs = new ArrayList<>();
				for(int i = 0; i<columnCount; i++) {
					Boolean selected = (Boolean) table_1.getValueAt(i, 0); //第i行第0列 就是checkBox
					if(selected) {
						int courseID = (int) table_1.getValueAt(i, 1);//第i行第1列 即course_id
						selectedIDs.add(courseID);
					}
				}
				
				if(selectedIDs.isEmpty()) {
					JOptionPane.showMessageDialog(null,"Select some courses!","Error",JOptionPane.ERROR_MESSAGE);
				}else {
					try {
						JoinPage join = new JoinPage(selectedIDs,conn);
						join.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						join.setVisible(true);
						join.setSize(600,500);
					}catch(SQLException e1) {
						e1.printStackTrace();
					}
					
				}
			}
		});
		
		recruit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int columnCount = table_1.getRowCount();
				selectedIDs = new ArrayList<>();
				for(int i = 0; i<columnCount; i++) {
					Boolean selected = (Boolean) table_1.getValueAt(i, 0); //第i行第0列 就是checkBox
					if(selected) {
						int courseID = (int) table_1.getValueAt(i, 1);//第i行第1列 即course_id
						selectedIDs.add(courseID);
					}
				}
				if(selectedIDs.isEmpty()) {
					JOptionPane.showMessageDialog(null,"Select some courses!","Error",JOptionPane.ERROR_MESSAGE);

				}else {
					RecruitPage recruit = new RecruitPage(selectedIDs,conn);
					recruit.setVisible(true);
					recruit.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					recruit.setSize(600,500);
				}
			}
		});
		myStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				My_status my_status = new My_status();
				my_status.setVisible(true);
				my_status.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				my_status.setSize(600,500);
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
	}
	public void createTextField() {

		searchField = new JTextField(30);
		searchField.setText("Search by course's name, ID, or teacher."); // 設定提示文字
		searchField.setForeground(Color.GRAY); // 設定文字顏色為灰色
//		
//		searchField.addFocusListener(new FocusListener() {
//		    
//		    public void focusGained(FocusEvent e) {
//		        // 當searchField被點擊時，清除提示文字
//		    	if (searchField.getText().equals("Search by course's name, ID, or teacher.")) {
//		    		searchField.setText("");
//		    		searchField.setForeground(Color.BLACK); // 將文字顏色設定回黑色
//		        }
//		    }
//		    public void focusLost(FocusEvent e) {
//		        // 當焦點失去時，如果文字為空，顯示提示文字
//		        if (searchField.getText().isEmpty()) {
//		        	searchField.setForeground(Color.GRAY);
//		        	searchField.setText("Search by course's name, ID, or teacher.");
//		        }
//		    }
//		});
	}
	public void showAll() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject","root","n2431836");
			String query = "SELECT * FROM `courses` ";
			PreparedStatement stat = conn.prepareStatement(query);
	        ResultSet rs = stat.executeQuery();

            DefaultTableModel model = new DefaultTableModel() {
            	
                @Override//指定每一欄的類型
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class; // 第一欄設為布林類型
                    } else {
                        return super.getColumnClass(columnIndex);
                    }
                }

                @Override//指定哪些欄位是可以編輯的
                public boolean isCellEditable(int row, int column) {
                    return column == 0; // 只有第一欄是可編輯的
                }
            };

            model.addColumn("Select"); // 新增布林欄位

            ResultSetMetaData rsmd = (ResultSetMetaData)rs.getMetaData();
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
                    rows[i] = rs.getObject(i);//將每一行添加到row
                }
                model.addRow(rows);
            }

            table_1.setModel(model);
            
         // 在每一行前面加 JCheckBox
            TableColumnModel columnModel = table_1.getColumnModel();//取得負責管理表格的欄位設置
            TableColumn checkBoxColumn = columnModel.getColumn(0);//取得欄位0 就是整欄的checkBox
            checkBoxColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));//設置欄位編輯器是預設的，並初始化
            checkBoxColumn.setCellRenderer(table_1.getDefaultRenderer(Boolean.class));

            stat.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	
	public void createPanel() {
		JPanel upPanel = new JPanel();
		upPanel.add(searchLabel);
		upPanel.add(searchField);
		upPanel.add(searchButton);
		getContentPane().add(upPanel,BorderLayout.NORTH);
		
		table_1 = new JTable();
	    scrollPane = new JScrollPane(table_1);
	    scrollPane.setPreferredSize(new Dimension(500, 200));
	    getContentPane().add(scrollPane, BorderLayout.CENTER);

	    JPanel downPanel = new JPanel();
	    downPanel.add(join);
	    downPanel.add(recruit);
	    downPanel.add(myStatus);
	    getContentPane().add(downPanel, BorderLayout.SOUTH);
	}
	
}