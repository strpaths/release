import java.io.File;

import javax.swing.ImageIcon;


public class Resources {
	private static String firstRootPath = new File("plugins","files").toString();
	private static String rootPath = new File(firstRootPath,"static").toString();
	
	private static String humanIconPath = new File(rootPath, "human.jpg").toString();
	private static String mouseIconPath = new File(rootPath, "mouse.jpg").toString();
	private static String ratIconPath = new File(rootPath, "rat.jpg").toString();
	private static String pagerankIconPath = new File(rootPath, "pagerank-icon.jpg").toString();

	public final static ImageIcon humanIcon = new ImageIcon(humanIconPath);
	public final static ImageIcon mouseIcon = new ImageIcon(mouseIconPath);
	public final static ImageIcon ratIcon = new ImageIcon(ratIconPath);
	public final static ImageIcon pagerankIcon = new ImageIcon(pagerankIconPath);
	
}
