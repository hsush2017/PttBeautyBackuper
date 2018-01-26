package choYM.main;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		System.setProperty("file.encoding", "UTF-8");
		
		BackupHandler task = new BackupHandler();
		task.start();
	}

}
