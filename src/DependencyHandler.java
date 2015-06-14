import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;



//class FileInfo {
//	public final String filename;
//	public final String relativePath;
//	public final String MD5;
//
//	public FileInfo(String relativePath, String MD5, String filename) {
//		this.filename = filename;
//		this.relativePath = relativePath;
//		this.MD5 = MD5;
//	}
//}

class FileInfo {
	public final String filename;
	public final String relativePath;
	public final String MD5;

	public FileInfo(String relativePath, String MD5, String filename) {
		this.filename = filename;
		this.relativePath = relativePath;
		this.MD5 = MD5;
	}
}

public class DependencyHandler {

	public ArrayList<FileInfo> dependencies = new ArrayList<FileInfo>();
	public DependencyHandler()
	{
//		JOptionPane.showMessageDialog(null, "hi");
	}



	public void addDependency(String relativePath, String filename, String MD5) {
		dependencies.add(new FileInfo(relativePath, MD5, filename));
	}

	public ArrayList<FileInfo> checkDependencies(String absolutePath) {
		ArrayList<FileInfo> notExisted = new ArrayList<FileInfo>();
		for (FileInfo fi : dependencies) {

//			File f = new File(absolutePath, new File(fi.relativePath,
//					fi.filename).getPath());
			File f = new File(absolutePath,fi.filename);
//			JOptionPane.showMessageDialog(null, f.toPath());
			
			if (absolutePath.indexOf(fi.relativePath) != -1 && !(f.exists() && !f.isDirectory()))
				notExisted.add(fi);
			/* Check MD5
			if(absolutePath.indexOf(fi.relativePath) != -1 && f.exists() && !f.isDirectory())
			{
				MessageDigest md;
				try {
					md = MessageDigest.getInstance("MD5");
					InputStream is = Files.newInputStream(f.toPath());
					DigestInputStream dis = new DigestInputStream(is, md);
					  // Read stream to EOF as normal...
					
					byte[] digest = md.digest();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			*/
		}
		return notExisted;
	}
	
	private void downloadDependency(String url, String absolutePath, FileInfo fi) throws IOException
	{
		URL website = new URL(url + fi.filename);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(new File(fi.relativePath, fi.filename).getPath());
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

	/*
	private void downloadDependency(String url, String absolutePath, FileInfo fi)
			throws Exception {

		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet httpget = new HttpGet(url + fi.filename);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			BufferedInputStream bis = new BufferedInputStream(
					entity.getContent());
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(
							//new File(absolutePath, new File(fi.relativePath, fi.filename).getPath())));
							new File(fi.relativePath, fi.filename).getPath()));
			int inByte;
			while ((inByte = bis.read()) != -1)
				bos.write(inByte);
			bis.close();
			bos.close();
		} else {
			throw new Exception("HTTP request failed!");
		}

	}
*/
	
	public int resolveDependencies(String[] urls, String[] absolutePaths)
			throws Exception {
//		JOptionPane.showMessageDialog(null, "check dependencies: "+absolutePath+File.separator);
//		if (!Files.isDirectory(new File(absolutePath).toPath(), LinkOption.NOFOLLOW_LINKS))
//			JOptionPane.showMessageDialog(null, "Not existed: "+new File(absolutePath).toPath());
		String url, absolutePath;
		ArrayList<FileInfo>[] allDependencies = new ArrayList[absolutePaths.length];
		int missings = 0;
		for (int i=0; i < urls.length; i++)
		{	
			url = urls[i];
			absolutePath = absolutePaths[i];
			if(!Files.exists(new File(absolutePath).toPath(), LinkOption.NOFOLLOW_LINKS))
			{
//				File f = new File(absolutePath);//new File(rootPath + sep + "static" + File.separator);
				File f = new File(absolutePath);//new File(rootPath + sep + "static" + File.separator);
				
				f.mkdirs();
			}
			allDependencies[i] = checkDependencies(absolutePath);
			int s = allDependencies[i].size();
			missings += s;
//			if (s == 0)
//				return;
		}
		if (missings == 0)
			return missings;
		
		for (int i=0; i < urls.length; i++)
		{	
			absolutePath = absolutePaths[i];
			if(!Files.exists(new File(absolutePath).toPath(), LinkOption.NOFOLLOW_LINKS))
			{
				JOptionPane.showConfirmDialog(null, "Error! Couldn't create directory: "+absolutePath);
				return missings;
			}
		}
		
		int dialogResult = JOptionPane.showConfirmDialog(null, missings + " file"
				+ (missings == 1 ? " is" : "s are")
				+ " missing. Would you like to download "
				+ (missings == 1 ? "it?" : "them?"), "Warning",
				JOptionPane.YES_NO_OPTION);

		if (dialogResult == JOptionPane.YES_OPTION) {
		int counter = 1;
		for (int i = 0; i < urls.length; i++)
		{
			
			for (FileInfo fi : allDependencies[i]) {
				
				/*
				JOptionPane msgBox = createMessageBox();
				JDialog dialog;
				dialog = showMessage(counter, notExistedDependencies.size(), fi.filename, msgBox);
				dialog.setVisible(true);
				*/
				JDialog dialog = showMessage1(counter, missings, fi.filename);
				
				try
				{
					downloadDependency(urls[i], absolutePaths[i], fi);
				}catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: "+e.getMessage());
				}
				counter++;
				dialog.dispose();
			}
		}
		}
		return missings;
	}
	
	public void resolveDependencies(String url, String absolutePath)
			throws Exception {
//		JOptionPane.showMessageDialog(null, "check dependencies: "+absolutePath+File.separator);
//		if (!Files.isDirectory(new File(absolutePath).toPath(), LinkOption.NOFOLLOW_LINKS))
//			JOptionPane.showMessageDialog(null, "Not existed: "+new File(absolutePath).toPath());
		if(!Files.exists(new File(absolutePath).toPath(), LinkOption.NOFOLLOW_LINKS))
		{
			File f = new File(absolutePath);//new File(rootPath + sep + "static" + File.separator);
			f.mkdirs();
		}
		ArrayList<FileInfo> notExistedDependencies = checkDependencies(absolutePath);
		int s = notExistedDependencies.size();
		if (s == 0)
			return;
		int dialogResult = JOptionPane.showConfirmDialog(null, s + " file"
				+ (s == 1 ? " is" : "s are")
				+ " missing. Would you like to download "
				+ (s == 1 ? "it?" : "them?"), "Warning",
				JOptionPane.YES_NO_OPTION);

		if (dialogResult == JOptionPane.YES_OPTION) {
			
			int counter = 1;
			
			for (FileInfo fi : notExistedDependencies) {
				
				/*
				JOptionPane msgBox = createMessageBox();
				JDialog dialog;
				dialog = showMessage(counter, notExistedDependencies.size(), fi.filename, msgBox);
				dialog.setVisible(true);
				*/
				JDialog dialog = showMessage1(counter, notExistedDependencies.size(), fi.filename);
				
				try
				{
					downloadDependency(url, absolutePath, fi);
				}catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: "+e.getMessage());
				}
				counter++;
				dialog.dispose();
			}
		}
		
	}

	private JOptionPane createMessageBox() {
		JOptionPane optionPane = new JOptionPane("",
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null, new Object[] {}, null);
		return optionPane;
	}

	private JDialog showMessage(int counter, int total, String filename, JOptionPane optionPane) {
		optionPane.setMessage("Downloading file \"" + filename + "\" ... ("+ counter + "/" + total+ ")");
		final JDialog dialog = new JDialog();
		dialog.setTitle("Downloading...("+counter + "/" + total+")");
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.pack();
		dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)
				/ 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit()
				.getScreenSize().height) / 2 - dialog.getHeight() / 2);

		
		return dialog;
	}
	
	private JDialog showMessage1(int counter, int total, String filename)
	{
		String mesg = "Downloading file \"" + filename + "\" ... ("+ counter + "/" + total+ ")";
		JDialog dialog = new JDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class,null), "");
		
		JPanel panel = new JPanel(new GridLayout(2, 1));
		
		JLabel msg1 = new JLabel("This may take several minutes. Please wait ...");
		JPanel panel1 = new JPanel();
		panel1.setBorder(new EmptyBorder(0, 0, 10, 0));
		panel1.add(msg1);
		JLabel msg = new JLabel(mesg);
		JPanel panel2 = new JPanel();
		panel2.add(msg);
		panel1.setVisible(true);
		panel2.setVisible(true);
		panel.add(panel1);
		panel.add(panel2);
		panel.setVisible(true);
		panel.setBorder(new EmptyBorder(30, 40, 30, 40));
		panel.setSize(panel.getWidth()+50, panel.getHeight()+50);
		dialog.setTitle("Downloading...("+counter + "/" + total +")");
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)
				/ 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit()
				.getScreenSize().height) / 2 - dialog.getHeight() / 2);dialog.setVisible(true);
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.pack();
		dialog.show();
		return dialog;
	}
	
	private JDialog showMessage(int counter, int total, String filename) {
		JDialog dialog = new JDialog();
		JPanel panel = new JPanel(new GridLayout(2, 1));
		
		JLabel msg1 = new JLabel("This may take several minutes. Please wait ...");
		JPanel panel1 = new JPanel();
		panel1.setBorder(new EmptyBorder(0, 0, 10, 0));
		panel1.add(msg1);
		JLabel msg = new JLabel();
		JPanel panel2 = new JPanel();
		panel2.add(msg);
		panel1.setVisible(true);
		panel2.setVisible(true);
		panel.add(panel1);
		panel.add(panel2);
		panel.setVisible(true);
		panel.setBorder(new EmptyBorder(30, 40, 30, 40));
		panel.setSize(panel.getWidth()+50, panel.getHeight()+50);
		dialog.setTitle("Downloading...("+counter + "/" + total +")");
		dialog.getContentPane().add(panel);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)
				/ 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit()
				.getScreenSize().height) / 2 - dialog.getHeight() / 2);dialog.setVisible(true);
		dialog.setModal(true);
		dialog.setResizable(false);
//		dialog.setVisible(true);
		dialog.pack();
		dialog.show();
		
		
//		dialog.repaint();

		return dialog;
	}
	
	
	
	public void resolveAllDependencies()
	{

		// Human
		// under:
		// http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files/human/
		// Example: dh.addDependency("test/test2".replace("/", File.separator),
		// "binary-human-CORUM-Nodes.txt", "md5-todo");
		String sep = File.separator;
		String rootPath = Resources.getRoot() + sep + "files";
		String[] top = { "human", "mouse" };
		String[] datasets = { "ophid", "HPRD", "CORUM", "InnateDB", "MPPI", "MatrixDB", 
				"bind", "dip", "grid", "intact", "mint", "string", };
//		String[] postfixes = { "converted", "Degs", "Nodes", "PPI-Inverted","PPI" };
		String[] postfixes = { "PPI-Inverted","PPI" };

		String relativePath;
		for (int i = 0; i < 2; i++) {
			relativePath = rootPath + sep + top[i];
			for (int j = 0; j < datasets.length; j++) {
//				addDependency(relativePath, "binary-" + top[i] + "-"
//						+ datasets[j] + ".txt", "MD5");
				if (i == 1 && j < 2 ) // For mouse, HPRD and ophid don't need the files with postfixes.
					continue;
				for (int k = 0; k < postfixes.length; k++) {
					addDependency(relativePath, "binary-" + top[i] + "-"
							+ datasets[j] + "-" + postfixes[k] + ".txt", "MD5");
				}
				
			}

//			addDependency(relativePath, "binary-" + top[i] + "-MPACT.txt",
//					"MD5");
//			addDependency(relativePath, "binary-" + top[i] + "-mpiimex.txt",
//					"MD5");
//			addDependency(relativePath, "binary-" + top[i] + "-mpilit.txt",
//					"MD5");
			addDependency(relativePath, "Annotations-" + top[i] + ".txt",
					"MD5");
//			if (i == 1)
//				addDependency(relativePath, "binary-allnet-" + top[i]
//						+ ".txt", "MD5");
		}

		addDependency(rootPath + sep + "static", "human.jpg", "MD5");
		addDependency(rootPath + sep + "static", "mouse.jpg", "MD5");

		// Mouse
		// under:
		// http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files/mouse/

		// Static
		// under:
		// http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files/static/

		String baseUrl = "http://www.cs.colostate.edu/~asharifi/StrongestPath/plugins/files";
		//String baseUrl = "http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files";
		
//		try {
//			File f = new File(rootPath + sep + "static" + File.separator);
//			f.mkdirs();
//			f = new File(rootPath + sep + "mouse" + File.separator);
//			f.mkdirs();
//			f = new File(rootPath + sep + "human" + File.separator);
//			f.mkdirs();
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			JOptionPane.showMessageDialog(null, e.getMessage());
//		}
		try {
			String[] urls = {
					baseUrl+"/human/", 
					baseUrl+"/mouse/", 
					baseUrl+"/static/"
					};
			String[] absolutePaths = {
					rootPath+sep+"human",
					rootPath+sep+"mouse",
					rootPath+sep+"static"
					};
//			resolveDependencies(baseUrl + "/human/", rootPath+sep+"human");
//			resolveDependencies(baseUrl + "/mouse/", rootPath+sep+"mouse");
//			resolveDependencies(baseUrl + "/static/", rootPath+sep+"static");
			int missings = resolveDependencies(urls, absolutePaths);
			
			if(missings != 0)
				JOptionPane.showMessageDialog(null, "All necessary files have been successfully downloaded!");

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error: "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error: "+e.getMessage());
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		DependencyHandler dh = new DependencyHandler();

		// Human
		// under:
		// http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files/human/
		// Example: dh.addDependency("test/test2".replace("/", File.separator),
		// "binary-human-CORUM-Nodes.txt", "md5-todo");
		String sep = File.separator;
		String rootPath = Resources.getRoot() + sep + "files";
		
		String[] top = { "human", "mouse" };
		String[] datasets = { "CORUM", "HPRD", "InnateDB", "MPPI", "MatrixDB",
				"bind", "dip", "grid", "intact", "mint", "string", "ophid" };
//		String[] postfixes = { "converted", "Degs", "Nodes", "PPI-Inverted",
//		"PPI" };
		String[] postfixes = { "PPI-Inverted", "PPI" };

		String relativePath;
		for (int i = 0; i < 2; i++) {
			relativePath = rootPath + sep + top[i];
			for (int j = 0; j < datasets.length - i; j++) {
				for (int k = 0; k < postfixes.length; k++) {
					dh.addDependency(relativePath, "binary-" + top[i] + "-"
							+ datasets[j] + "-" + postfixes[k] + ".txt", "MD5");
				}
//				dh.addDependency(relativePath, "binary-" + top[i] + "-"
//						+ datasets[j] + ".txt", "MD5");
			}

//			dh.addDependency(relativePath, "binary-" + top[i] + "-MPACT.txt",
//					"MD5");
//			dh.addDependency(relativePath, "binary-" + top[i] + "-mpiimex.txt",
//					"MD5");
//			dh.addDependency(relativePath, "binary-" + top[i] + "-mpilit.txt",
//					"MD5");
			dh.addDependency(relativePath, "Annotations-" + top[i] + ".txt",
					"MD5");
//			if (i == 1)
//				dh.addDependency(relativePath, "binary-allnet-" + top[i]
//						+ ".txt", "MD5");
		}

		dh.addDependency(rootPath + sep + "static", "human.jpg", "MD5");
		dh.addDependency(rootPath + sep + "static", "mouse.jpg", "MD5");

		// Mouse
		// under:
		// http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files/mouse/

		// Static
		// under:
		// http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files/static/

		String baseUrl = "http://lbb.ut.ac.ir/Download/LBBsoft/StrongestPath/plugins/files";

		try {
			File f = new File(rootPath + sep + "static" + File.separator);
			f.mkdirs();
			f = new File(rootPath + sep + "mouse" + File.separator);
			f.mkdirs();
			f = new File(rootPath + sep + "human" + File.separator);
			f.mkdirs();

		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String[] urls = {
					baseUrl+"/human/", 
					baseUrl+"/mouse/", 
					baseUrl+"/static/"
					};
			String[] absolutePaths = {
					rootPath+sep+"human",
					rootPath+sep+"mouse",
					rootPath+sep+"static 1"
					};
//			resolveDependencies(baseUrl + "/human/", rootPath+sep+"human");
//			resolveDependencies(baseUrl + "/mouse/", rootPath+sep+"mouse");
//			resolveDependencies(baseUrl + "/static/", rootPath+sep+"static");
			dh.resolveDependencies(urls, absolutePaths);
//			dh.resolveDependencies(baseUrl + "/human/", rootPath+sep+"human");
//			dh.resolveDependencies(baseUrl + "/mouse/", rootPath+sep+"mouse");
//			dh.resolveDependencies(baseUrl + "/static/", rootPath+sep+"static");
			
//			JOptionPane.showMessageDialog(null, "Done!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
