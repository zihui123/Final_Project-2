import java.sql.*;

public class User {

	//*Setting Data Base */
	public String server = "jdbc:mysql://140.119.19.73:3315/";
	public String database = "111306017"; // change to your own database
	public String url = server + database + "?useSSL=false";
	public String usernameLogin = "111306017"; // change to your own user name
	public String password = "9ftmc"; // change to your own password　　
	public int numberInRow; //How many students are in the enrolled list
	public Connection conn;
	public Statement stat;
	public ResultSet result;

	//*User's object info */
	public String userAccount;
	public String userName;
	public String userDep;
	
	//*User's applied group */
	public String courseName;
	public int courseID;
	public int groupID;
	public String message;
	public String leaderName;

	public User() {

		try {
			
			this.conn = DriverManager.getConnection(url, usernameLogin, password);
			this.stat = conn.createStatement();
			this.result = stat.getResultSet();

		} catch (Exception c) {

			System.out.println(c.getMessage());
			
		}
	}

	public void add(String studentName, String department, String account, String pw) throws AccountError {

		//*Throw exception if password format is wrong */
		if (account.length() == 0)throw new AccountError("AccountError:Account can't be empty");
		
		//*Try whether system connect to DB or not */
		try {

			String query;
			boolean success;

			query = "SELECT * FROM `Student_Info`";
			success = stat.execute(query);


			String gmail = account + "@g.nccu.edu.tw";

			if (success) {
                
				query = "INSERT INTO `Student_Info` (Name, Department, ID, Gmail, Password) VALUES" + String.format("('%s', '%s', '%s', '%s', '%s')", studentName, department, account, gmail, pw);
            	success = stat.execute(query);

			}
			

		} catch (Exception e) {
		
			e.printStackTrace();

		}
	}

	public void checkAccountExist(String account) throws AccountError, SQLException {

		try {
			
			String query = "SELECT ID FROM Student_Info";
			result = stat.executeQuery(query);
			boolean checker = false;

			while (result.next()) {
				
				String accountOnAir = result.getString("ID");

				if (account.equals(accountOnAir)) {
					
					checker = true;
					break;
					
				}

			}

			if (checker == false) {
				
				throw new AccountError("Undefined Account");

			}

		} catch (SQLException e) {
			
			System.out.println("------Something wrong when accessing DB-------");

		}

		
	}

	public void checkPassword(String account, String password) throws PasswordError {

		try {

			String query = String.format("SELECT Password FROM Student_Info");
			result = this.stat.executeQuery(query);
			boolean checker = false;

			query = String.format("SELECT Password FROM Student_Info WHERE ID='%s'", account);
			ResultSet r = this.stat.executeQuery(query);

			while (r.next()) {

				String pw = r.getString("Password");

				if (pw.equals(password)) {

					System.out.println("Air: " + pw + " / local: " + password);
					checker = true;
					
				}

			}

			if (checker) {
				
				this.userAccount = account;

				query = String.format("SELECT Name FROM Student_Info WHERE ID='%s'", account);
				r = this.stat.executeQuery(query);

				while (r.next()) {
					
					this.userName = r.getString("Name");

				}

				query = String.format("SELECT Department FROM Student_Info WHERE ID='%s'", account);
				r = this.stat.executeQuery(query);
				
				while (r.next()) {
					
					this.userDep = r.getString("Department");

				}

			}

			if (checker == false) {
				
				throw new PasswordError("Wrong Password");

			}

		} catch (SQLException e) {

			System.out.println("Something Wrong when accessing DB during checking pw");
			e.printStackTrace();

		}
		
	}
}

class AccountError extends Exception {

	public AccountError(String Error) {

		super(Error);

	}

}

class PasswordError extends Exception {

	public PasswordError(String Error) {

		super(Error);

	}

}
