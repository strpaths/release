import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.RoundedBalloonStyle;
import net.java.balloontip.utils.ToolTipUtils;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.swing.AbstractCySwingApp;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;

class BFSInfos {
	public HashMap<Integer, Integer> heights;
	public HashMap<Integer, Integer> visitTimes;

}

public class MyStrongestPathPlugin extends AbstractCySwingApp {

	String styleName = "myVisualStyle";
	private final String[] databases = { "bind", "CORUM", "dip", "grid",
			"HPRD", "InnateDB", "MatrixDB", "mint", "MPPI", "ophid", "intact",
			"string" };
	private final String[] databaseLabels = { "BIND", "CORUM", "DIP",
			"BioGRID", "HPRD", "InnateDB", "MatrixDB", "MINT", "MPPI", "Ophid",
			"IntAct", "STRING" };
	private final HashSet<String> excludeDatabases = new HashSet<String>();
	{
		excludeDatabases.add("ophid");
		excludeDatabases.add("HPRD");
	}

	private String networkViewIdentifier = "StrongestPathNetworkView";
	private int networkViewNum = 0;

	protected String DATAspecies;
	public StrongestPath strongestPath;
	protected String DATAdatabaseName = "binary-human-bind";
	protected int DATA1column;
	protected int DATA2column;
	private JFrame frame;
	public JPanel databaseModePanel;
	public JPanel pluginMainPanel;
	public JPanel uploadUserDBPanel;
	public JTextField annotFileAddressE;
	public JTextField userDBFileAddress;
	public ActionListener annotActionListenerE;
	public ActionListener userDBActionListener;
	public JPanel databaseChoosePanel;
	public JPanel databaseChoosePanelSP;
	public JCheckBox[] databaseSelectionSP = new JCheckBox[databases.length];
	public JCheckBox[] databaseSelectionE = new JCheckBox[databases.length];
	public JButton selectAllE;
	public JButton deselectAllE;
	public JButton selectAllSP;
	public JButton deselectAllSP;

	// public VisualStyleFactory visualStyleFactoryServiceRef;
	public VisualStyle visualStyle;

	// Old 2.x
	// VisualStyleBuilder graphStyle = new VisualStyleBuilder(styleName, false);

	public MyStrongestPathPlugin(CySwingAppAdapter adapter) {

		super(adapter);
		adapter.getCySwingApplication().addAction(
				new MyPluginMenuAction(adapter));
	}

	public class MyPluginMenuAction extends AbstractCyAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851939337950955364L;
		final public static String HUMAN = Nomenclature.HUMANSPEICEID;
		final public static String MOUSE = Nomenclature.MOUSESPEICEID;
		final public static String RAT = Nomenclature.RATSPEICEID;
		private JTabbedPane tabbedPane;
		private JPanel topPanel;
		private JPanel growthDataPanel;
		private JPanel strongestPathDataPanel;
		private JComponent[] panelList;
		protected int strongestPathHeight = 100;// 590;
		protected int startingPanelHeight = 250;
		protected int growthPanelHeight = 200;

		protected int selectDBWidth = 350;
		protected int selectDBHeight = 300;
		protected int userDBPanelWidth = 700;
		protected int userDBPanelHeight = 500;
		protected int pluginWidth = 600;
		protected int pluginHeight = 700;
		protected int tabbedPaneWidth = 600 - 60;
		protected int tabbedPaneHeight = 700 - 80;

		// ******* DATA *******

		protected HashMap<String, StrongestPath> subNetworks = new HashMap<String, StrongestPath>();
		// protected HashMap<String, StrongestPath> subNetworks = new
		// HashMap<String, StrongestPath>();

		protected String DATAsrcfilePath;
		protected String DATAfilePath;
		protected String DATAsrctextField;
		protected String DATAdstfilePath;
		protected String DATAdsttextField;

		protected int DATApowerFactor;
		protected int DATAdampingFactor;
		protected int DATAnumNodes;
		protected int DATAmode;

		private JRadioButton humanRadioButton;
		private JRadioButton mouseRadioButton;

		private JTextField srcfileAddress;
		private JTextField srcTextField;
		private JTextField dstfileAddress;
		private JTextField dstTextField;

		private JTextField numberOfNewNodesText;
		private JTextField srcfileAddressE;
		private JTextField srcTextFieldE;
		private ActionListener srcActionListenerE;
		private boolean srcFromFileE = false;

		private GridBagConstraints gbc = new GridBagConstraints();

		private boolean userDatabase = false;
		private ActionListener srcActionListener;
		private ActionListener dstActionListener;
		private boolean srcFromFile = false;
		private boolean dstFromFile = false;
		protected double DATAthreshold;
		protected int DATAalphaSlider;
		protected int DATApowerSlider;
		protected String DATAnumberOfNodes;
		private int step;
		private ArrayList<String> DATAdatabaseNames;
		private boolean networkFromFile = false;
		private String networkFile;
		private Nomenclature nomen;
		private int numberOfNewNodes;
		private boolean annotFromFile;
		private String annotFile;
		private boolean selectedAllE = false;
		private boolean selectedAllSP = false;
		private CyAppAdapter adapter;
		final CyApplicationManager manager;

		public MyPluginMenuAction(CyAppAdapter adapter) {
			super("Strongest Path", adapter.getCyApplicationManager(),
					"network", adapter.getCyNetworkViewManager());
			// super("Strongest Path");
			this.adapter = adapter;
			setPreferredMenu("Apps");
			manager = adapter.getCyApplicationManager();
			visualStyle = this.adapter.getVisualStyleFactory()
					.createVisualStyle(styleName);

		}

		protected void bringToTheFront()
		{		
			java.awt.EventQueue.invokeLater(new Runnable() {
			    @Override
			    public void run() {
			        frame.toFront();
			        frame.repaint();
			    }
			});
		}
		
		
		protected void addNodeIDColumn(CyNetwork network) {
			CyTable nodeTable = network.getDefaultNodeTable();
			CyTable edgeTable = network.getDefaultEdgeTable();
			if (nodeTable.getColumn("nodeID") == null) {
				nodeTable.createColumn("nodeID", String.class, false);
			}
			/*if (nodeTable.getColumn("Path Confidence") == null) {
				nodeTable.createColumn("Path Confidence", String.class, false);
			}*/
			if (edgeTable.getColumn("Database") == null) {
				edgeTable.createColumn("Database", String.class, false);
			}
		}

		private void doFinalize() {
			for (String dbn : subNetworks.keySet())
				subNetworks.put(dbn, null);
			for (String dbn : subNetworks.keySet())
			subNetworks.put(dbn, null);
//			subNetworks = null;
//			nomen = null;
			System.gc();
		}

		protected void finalize() throws Throwable {
			doFinalize();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Turn off metal's use of bold fonts
					UIManager.put("swing.boldMetal", Boolean.FALSE);
					initPlugin();
					showFrame();
					
					
				}
			});
		}

		public void initPlugin() {

			
			DependencyHandler dh = new DependencyHandler();
			dh.resolveAllDependencies();
			
			
			topPanel = new JPanel(false);

			initializePluginMainPanel();
			initializeUploadUserDatabase();
			initializeSelectDatabaseModePanel();

			
			panelList = new JComponent[] { databaseModePanel,
					uploadUserDBPanel, pluginMainPanel };
			topPanel.setPreferredSize(new Dimension(selectDBWidth,
					selectDBHeight));
			databaseModePanel.setPreferredSize(new Dimension(
					selectDBWidth - 40, selectDBHeight - 30));
//			uploadUserDBPanel.setPreferredSize(new Dimension(
//					userDBPanelWidth - 40, userDBPanelHeight - 30));
			
			uploadUserDBPanel.setPreferredSize(new Dimension(
					500, 300));
			pluginMainPanel.setPreferredSize(new Dimension(pluginWidth - 40,
					pluginHeight - 30));

			databaseModePanel.setVisible(true);
			uploadUserDBPanel.setVisible(false);
			pluginMainPanel.setVisible(false);

			topPanel.add(databaseModePanel);
			topPanel.add(uploadUserDBPanel);
			topPanel.add(pluginMainPanel);

		}

		private CyNode getNodeWithValue(final CyNetwork network,
				final String colname, final Object value) {
			CyTable table = network.getDefaultNodeTable();
			final Collection<CyRow> matchingRows = table.getMatchingRows(
					colname, value);
			final Set<CyNode> nodes = new HashSet<CyNode>();
			final String primaryKeyColname = table.getPrimaryKey().getName();
			if (matchingRows.size() != 0) {
				for (final CyRow row : matchingRows) {
					final Long nodeId = row.get(primaryKeyColname, Long.class);
					if (nodeId == null)
						continue;
					final CyNode node = network.getNode(nodeId);
					if (node == null)
						continue;
					nodes.add(node);
				}
				return nodes.iterator().next();
			} else
				return null;
		}

		private void initializeSelectDatabaseModePanel() {
			databaseModePanel = new JPanel(false);
			setBorder(databaseModePanel, " Database input ");
			databaseModePanel.setLayout(new GridLayout(2, 1));

			final JRadioButton userDatabaseButton = new JRadioButton(
					"User Provided Database");
			final JRadioButton internalDatabaseButton = new JRadioButton(
					"Internal Database");
			ButtonGroup group = new ButtonGroup();
			group.add(internalDatabaseButton);
			group.add(userDatabaseButton);
			internalDatabaseButton.setSelected(true);
			group.setSelected(internalDatabaseButton.getModel(), true);

			JPanel internalDBPanel = new JPanel(false);
			internalDBPanel.setLayout(new GridLayout(2, 1));
			final JPanel species = createSpeciesPanel();
			internalDBPanel.add(internalDatabaseButton);
			internalDBPanel.add(species);
			species.setLocation(species.getLocation().x + 50,
					species.getLocation().y);
			internalDBPanel.setVisible(true);

			JPanel userDBPanel = new JPanel(false);
			userDBPanel.setLayout(new GridLayout(2, 1));
			JPanel tempPanel = new JPanel(false);
			JButton next = new JButton(" Next ");
			userDBPanel.add(userDatabaseButton);
			tempPanel.add(next);
			// userDBPanel.add(next);
			userDBPanel.add(tempPanel);
			userDBPanel.setVisible(true);

			databaseModePanel.add(internalDBPanel);
			databaseModePanel.add(userDBPanel);

			// ************************************** Action Listeners
			next.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					panelList[0].setVisible(false);
					if (userDatabaseButton.isSelected()) {
						userDatabase = true;
						Thread t = new Thread() {
							public void run() {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										panelList[1].setVisible(true);
										setInternalNetworkPanalEnabled(false);
										topPanel.setPreferredSize(new Dimension(
												500, 300));
										frame.pack();
									}
								});
							}
						};
						t.start();
						topPanel.setSize(500,300);
						topPanel.setPreferredSize(new Dimension(
								500, 300));
//						frame.setSize(new Dimension(500, 300));
//						frame.setPreferredSize(new Dimension(500, 300));
						frame.pack();
						
					} else {
						// **********
						userDatabase = false;
						final JPanel p1 = new JPanel(new GridBagLayout());
						p1.add(new JLabel(
								"Loading the databases. Please wait ..."),
								new GridBagConstraints());
						topPanel.add(p1);
						topPanel.setPreferredSize(new Dimension(300, 100));
						frame.pack();

						Thread t = new Thread() {
							public void run() {

								frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								loadAllDatabasesAndNomen();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										frame.setCursor(new Cursor(
												Cursor.DEFAULT_CURSOR));
										p1.setVisible(false);
										panelList[2].setVisible(true);
										setInternalNetworkPanalEnabled(true);
										topPanel.setPreferredSize(new Dimension(
												pluginWidth, pluginHeight));

										frame.pack();
									}
								});
							}
						};
						t.start();
					}

				}

			});
			internalDatabaseButton.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					// TODO Auto-generated method stub
					if (internalDatabaseButton.isSelected())
						setEnabaledSpeciesPanel(true);
					else
						setEnabaledSpeciesPanel(false);
				}
			});

		}

		private void initializeUploadUserDatabase() {
			uploadUserDBPanel = new JPanel(false);
			uploadUserDBPanel.setLayout(new GridLayout(3, 1));

			JPanel annotationPanel = createDBandAnnotInput(true);
			JPanel userDBPanel = createDBandAnnotInput(false);
			JPanel nextPanel = new JPanel(new GridBagLayout());
			JButton next = new JButton(" Next ");
			nextPanel.add(next, gbc);

			uploadUserDBPanel.setPreferredSize(new Dimension(500, 300));
			uploadUserDBPanel.add(annotationPanel);
			uploadUserDBPanel.add(userDBPanel);
			uploadUserDBPanel.add(nextPanel);
			// **************** Action listeners
			next.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (annotFileAddressE.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your annotation file ");
						annotActionListenerE.actionPerformed(arg0);
					} else if (userDBFileAddress.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your database ");
						userDBActionListener.actionPerformed(arg0);
					} else {
						// ********
						panelList[1].setVisible(false);

						final JPanel p1 = new JPanel(new GridBagLayout());
						p1.add(new JLabel(
								"Loading the databases. Please wait ..."),
								new GridBagConstraints());
						topPanel.add(p1);
						topPanel.setPreferredSize(new Dimension(300, 100));
						frame.pack();

						Thread t = new Thread() {
							public void run() {

								frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								try {

									loadUserDatabasesAndNomen();
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											frame.setCursor(new Cursor(
													Cursor.DEFAULT_CURSOR));
											p1.setVisible(false);
											panelList[2].setVisible(true);
											setInternalNetworkPanalEnabled(false);
											topPanel.setPreferredSize(new Dimension(
													pluginWidth, pluginHeight));

											frame.pack();
										}
									});
								} catch (Exception e) {
									frame.setCursor(new Cursor(
											Cursor.DEFAULT_CURSOR));
									p1.setVisible(false);
									panelList[1].setVisible(true);
									topPanel.setPreferredSize(new Dimension(
											userDBPanelWidth, userDBPanelHeight));
									frame.pack();
									JOptionPane.showMessageDialog(null,
											e.getMessage());
									e.printStackTrace();
								} finally {
									frame.setCursor(new Cursor(
											Cursor.DEFAULT_CURSOR));
								}
							}
						};
						t.start();

					}

				}
			});

		}

		private void initializePluginMainPanel() {
			pluginMainPanel = new JPanel(false);
			tabbedPane = new JTabbedPane();
			tabbedPane.setBounds(1, 1, tabbedPaneWidth, tabbedPaneHeight);
			JPanel growthTabPanel = new JPanel();
			JPanel strongestPathTabPanel = new JPanel();
			tabbedPane.setVisible(true);

			createGrowthDataPanel();
			createStrongestPathDataPanel();

			JScrollPane growthDataScrollPane = new JScrollPane(growthDataPanel);
			growthDataScrollPane.setVisible(true);
			growthDataScrollPane.setPreferredSize(new Dimension(
					tabbedPaneWidth, tabbedPaneHeight));
			JPanel strongestPathDataScrollPane = new JPanel();
			strongestPathDataScrollPane.add(strongestPathDataPanel);
			strongestPathDataScrollPane.setPreferredSize(new Dimension(
					tabbedPaneWidth, tabbedPaneHeight));

			growthTabPanel.add(growthDataScrollPane);
			strongestPathTabPanel.add(strongestPathDataScrollPane);

			tabbedPane.addTab("Strongest Path", strongestPathTabPanel);
			tabbedPane.addTab("Expand", growthTabPanel);

			growthDataPanel.setVisible(true);
			strongestPathDataPanel.setVisible(true);
			growthDataPanel.setBounds(1, 1, tabbedPaneWidth, tabbedPaneHeight);
			strongestPathDataPanel.setBounds(1, 1, tabbedPaneWidth,
					tabbedPaneHeight);
			pluginMainPanel.add(tabbedPane);
		}

		private void setInternalNetworkPanalEnabled(boolean enable) {
			selectAllE.setEnabled(enable);
			deselectAllE.setEnabled(enable);
			selectAllSP.setEnabled(enable);
			deselectAllSP.setEnabled(enable);
			for (int i = 0; i < databaseSelectionE.length; i++) {
				if (DATAspecies.equals(MOUSE)
						&& excludeDatabases.contains(databases[i]))
					databaseSelectionE[i].setEnabled(false);
				else
					databaseSelectionE[i].setEnabled(enable);

			}
			for (int i = 0; i < databaseSelectionSP.length; i++) {
				if (DATAspecies.equals(MOUSE)
						&& excludeDatabases.contains(databases[i]))
					databaseSelectionSP[i].setEnabled(false);
				else
					databaseSelectionSP[i].setEnabled(enable);
			}
		}

		private void createGrowthDataPanel() {
			growthDataPanel = new JPanel(false);

			growthDataPanel.setLayout(new GridLayout(4, 1));

			JPanel sourceNodesPanel = createNodesInputDataPanelNew(true, true);
			growthDataPanel.add(sourceNodesPanel);

			// ************** starting database panel
			databaseChoosePanel = new JPanel(false);
			databaseChoosePanel.setLayout(new GridBagLayout());
			JPanel databaseChoosePanelWrapper = new JPanel(false);
			databaseChoosePanelWrapper.setLayout(new GridLayout(
					(databases.length + 2) / 4, 4));
			setBorder(databaseChoosePanel, " Select the databases: ");
			for (int i = 0; i < databases.length; i++) {
				databaseSelectionE[i] = new JCheckBox(databaseLabels[i]);
				databaseChoosePanelWrapper.add(databaseSelectionE[i]);
			}

			JPanel selectButtons = new JPanel(false);
			selectAllE = new JButton(" Select All ");
			deselectAllE = new JButton(" Select None ");
			selectButtons.setLayout(new GridLayout(2, 1));
			selectButtons.add(selectAllE, gbc);
			selectButtons.add(deselectAllE, gbc);

			databaseChoosePanel.add(selectButtons, gbc);
			databaseChoosePanel.add(databaseChoosePanelWrapper, gbc);

			growthDataPanel.add(databaseChoosePanel);

			// *************** expand panel
			JPanel expandPanel = new JPanel(false);
			expandPanel.setLayout(new GridBagLayout());

			setBorder(expandPanel, " Display network ");
			final JButton showNetworkButton = new JButton(" Show network ");
			expandPanel.add(showNetworkButton, gbc);
			growthDataPanel.add(expandPanel);

			// *************** starting navigation panel
			JPanel numberOfNewNodesPanel = new JPanel(false);
			numberOfNewNodesPanel.setLayout(new GridBagLayout());
			setBorder(numberOfNewNodesPanel, " Expand network ");
			JLabel numberOfNewNodesLabel = new JLabel(
					"Number of genes/proteins: ");
			numberOfNewNodesText = new JTextField(5);
			numberOfNewNodesText.setText("10");
			gbc = new GridBagConstraints();
			numberOfNewNodesPanel.add(numberOfNewNodesLabel, gbc);
			numberOfNewNodesPanel.add(numberOfNewNodesText, gbc);
			final JButton expand = new JButton(" Expand by confidences ");
			expand.setEnabled(false);
			numberOfNewNodesPanel.add(expand, gbc);
			growthDataPanel.add(numberOfNewNodesPanel);

			// ******************** action listeners

			expand.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						if (numberOfNewNodesText.getText().equals("")) {
							JOptionPane
									.showMessageDialog(null,
											" Please input a posetive integer for number of new nodes ");
						} else {
							numberOfNewNodes = Integer
									.parseInt(numberOfNewNodesText.getText());
							doExpand();
						}
					} catch (Exception e) {
						JOptionPane
								.showMessageDialog(null,
										" Please input a posetive integer for number of new nodes ");
					}
				}
			});

			showNetworkButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					DATAdatabaseNames = new ArrayList<String>();
					for (int i = 0; i < databases.length; i++) {
						if (databaseSelectionE[i].isSelected())
							DATAdatabaseNames.add(databases[i]);
					}
					if (DATAdatabaseNames.size() == 0 && !networkFromFile) {
						JOptionPane.showMessageDialog(null,
								" Please select at least one database ");
					} else if (srcFromFileE
							&& srcfileAddressE.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						srcActionListenerE.actionPerformed(arg0);
					} else if (!srcFromFileE
							&& srcTextFieldE.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						srcTextFieldE.requestFocus();
					}else {

						DATAsrcfilePath = srcfileAddressE.getText();
						DATAsrctextField = srcTextFieldE.getText();
//						DATAdstfilePath = dstfileAddress.getText();
//						DATAdsttextField = dstTextField.getText();
						if (humanRadioButton.isSelected()) {
							DATAspecies = HUMAN;
						}
						if (mouseRadioButton.isSelected()) {
							DATAspecies = MOUSE;
						}
						doDisplayNetwork();
						expand.setEnabled(true);
					}

				}
			});

			selectAllE.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					for (int i = 0; i < databases.length; i++) {
						if (DATAspecies.equals(MOUSE)
								&& excludeDatabases.contains(databases[i]))
							databaseSelectionE[i].setSelected(false);
						else
							databaseSelectionE[i].setSelected(true);
					}
					selectedAllSP = true;
				}
			});
			deselectAllE.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					for (int i = 0; i < databases.length; i++) {
						databaseSelectionE[i].setSelected(false);
					}
					selectedAllSP = false;
				}
			});

		}

		private void setEnabaledSpeciesPanel(boolean enable) {
			humanRadioButton.setEnabled(enable);
			mouseRadioButton.setEnabled(enable);
		}

		private JPanel createSpeciesPanel() {
			JPanel speciesPanel = new JPanel(false);
			speciesPanel.setLayout(new GridBagLayout());

			ButtonGroup speciesGroup = new ButtonGroup();
			JLabel humanIcon = new JLabel(Resources.humanIcon);
			JLabel mouseIcon = new JLabel(Resources.mouseIcon);

			JPanel humanPanel = new JPanel();
			humanPanel.add(humanIcon);
			JPanel mousePanel = new JPanel();
			mousePanel.add(mouseIcon);
			humanRadioButton = new JRadioButton("human");
			mouseRadioButton = new JRadioButton("mouse");

			speciesGroup.add(humanRadioButton);
			speciesGroup.add(mouseRadioButton);

			humanPanel.add(humanRadioButton);
			mousePanel.add(mouseRadioButton);

			speciesGroup.setSelected(mouseRadioButton.getModel(), true);
			speciesPanel.add(humanPanel, gbc);
			speciesPanel.add(mousePanel, gbc);

			return speciesPanel;
		}

		private void createStrongestPathDataPanel() {
			strongestPathDataPanel = new JPanel(false);

			strongestPathDataPanel.setLayout(new GridLayout(5, 1));

			JPanel sourceNodesPanel = createNodesInputDataPanel(true);
			JPanel destinationNodesPanel = createNodesInputDataPanel(false);

			strongestPathDataPanel.add(sourceNodesPanel);
			strongestPathDataPanel.add(destinationNodesPanel);

			// ************** starting database panel
			databaseChoosePanelSP = new JPanel(false);
			databaseChoosePanelSP.setLayout(new GridBagLayout());
			JPanel databaseChooseWrapperPanel = new JPanel(false);
			databaseChooseWrapperPanel.setLayout(new GridLayout((int) Math
					.ceil((databases.length + 1) / 4), 4));
			setBorder(databaseChoosePanelSP, " Select the databases: ");
			for (int i = 0; i < databases.length; i++) {
				databaseSelectionSP[i] = new JCheckBox(databaseLabels[i]);
				databaseChooseWrapperPanel.add(databaseSelectionSP[i]);
			}
			// databaseSelectionSP[databases.length] = new
			// JCheckBox("SELECT ALL");
			// databaseChoosePanelSP.add(databaseSelectionSP[databases.length]);
			JPanel selectButtons = new JPanel(false);
			selectAllSP = new JButton(" Select All ");
			deselectAllSP = new JButton(" Select None ");
			selectButtons.setLayout(new GridLayout(2, 1));
			selectButtons.add(selectAllSP, gbc);
			selectButtons.add(deselectAllSP, gbc);
			databaseChoosePanelSP.add(selectButtons, gbc);
			databaseChoosePanelSP.add(databaseChooseWrapperPanel, gbc);
			strongestPathDataPanel.add(databaseChoosePanelSP);

			// *************** Threshold panel
			JPanel thresholdPanel = new JPanel(false);
			setBorder(thresholdPanel, " Select Maximum Distance Threshold: ");
			thresholdPanel.setLayout((LayoutManager) new BoxLayout(
					thresholdPanel, BoxLayout.Y_AXIS));
			final JSlider thresholdSlider = new JSlider(0, 1000, 0);
			thresholdSlider.setMajorTickSpacing(50);
			thresholdSlider.setPaintTicks(true);
			thresholdSlider.setPaintTrack(true);
			thresholdSlider
					.setModel(new DefaultBoundedRangeModel(0, 0, 0, 1000));
			Hashtable<Integer, JLabel> ht = new Hashtable<Integer, JLabel>();
			for (int i = 0; i < 1000; i++) {
				if (i % 100 == 0) {
					if (Math.pow(i / 1000.0, 3) > 0.3)
						ht.put(i,
								new JLabel(String.format("%.1f",
										Math.pow(i / 1000.0, 3))));
					else if (Math.pow(i / 1000.0, 3) > 0.03)
						ht.put(i,
								new JLabel(String.format("%.2f",
										Math.pow(i / 1000.0, 3))));
					else
						ht.put(i,
								new JLabel(String.format("%.3f",
										Math.pow(i / 1000.0, 3))));

				}
			}
			ht.put(999, new JLabel(String.format("%.1f", 1000 / 1000.0)));
			thresholdSlider.setLabelTable(ht);
			thresholdSlider.setPaintLabels(true);

			thresholdSlider.setAlignmentY(Component.CENTER_ALIGNMENT);
			thresholdPanel.add(thresholdSlider);
			strongestPathDataPanel.add(thresholdPanel);

			// *************** starting navigation panel
			JPanel navigationPanel = new JPanel(false);
			final JButton next = new JButton(" View results ");
			navigationPanel.add(next);
			strongestPathDataPanel.add(navigationPanel);

			// ******************** action listeners
			next.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					DATAdatabaseNames = new ArrayList<String>();
					for (int i = 0; i < databases.length; i++) {
						if (databaseSelectionSP[i].isSelected())
							DATAdatabaseNames.add(databases[i]);

					}

					if (DATAdatabaseNames.size() == 0 && !networkFromFile) {
						JOptionPane.showMessageDialog(null,
								" Please select at least one database ");
					} else if (srcFromFile
							&& srcfileAddress.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						srcActionListener.actionPerformed(arg0);
					} else if (!srcFromFile
							&& srcTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						srcTextField.requestFocus();
					} else if (dstFromFile
							&& dstfileAddress.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						dstActionListener.actionPerformed(arg0);
					} else if (!dstFromFile
							&& dstTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						dstTextField.requestFocus();
					} else {

						panelList[1].setVisible(false);
						panelList[2].setVisible(true);

						DATAsrcfilePath = srcfileAddress.getText();
						DATAsrctextField = srcTextField.getText();
						DATAdstfilePath = dstfileAddress.getText();
						DATAdsttextField = dstTextField.getText();
						if (humanRadioButton.isSelected()) {
							DATAspecies = HUMAN;
						}
						if (mouseRadioButton.isSelected()) {
							DATAspecies = MOUSE;
						}
						DATAthreshold = Math.pow(
								thresholdSlider.getValue() / 1000.0, 3);

						// ***** Do the Job ******
						doKStrongestPath();
					}
				}
			});

			selectAllSP.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					for (int i = 0; i < databases.length; i++) {
						if (DATAspecies.equals(MOUSE)
								&& excludeDatabases.contains(databases[i]))
							databaseSelectionSP[i].setSelected(false);
						else
							databaseSelectionSP[i].setSelected(true);
					}
					selectedAllSP = true;
				}
			});
			deselectAllSP.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					for (int i = 0; i < databases.length; i++) {
						databaseSelectionSP[i].setSelected(false);
					}
					selectedAllSP = false;
				}
			});

		}

		private JPanel createDBandAnnotInput(final boolean annotOrDB) {

			JPanel panel = new JPanel(false);
			String title = annotOrDB ? " Select Annotation File: "
					: " Select Database File: ";
			setBorder(panel, title);

			final JTextField fileAddress = new JTextField(20);
			
			final JButton browseButton = new JButton("Browse");
			panel.add(fileAddress);
			panel.add(browseButton);

			if (annotOrDB) {
				annotFileAddressE = fileAddress;
			} else {
				userDBFileAddress = fileAddress;
			}

			final JFileChooser fc = new JFileChooser();

			// **** action or change listeners

			fileAddress.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					if (annotOrDB) {
						annotFromFile = true;
						annotFile = fileAddress.getText();
					} else {
						networkFromFile = true;
						networkFile = fileAddress.getText();
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub

				}
			});
			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						fileAddress.setText(file.getAbsolutePath());
						if (annotOrDB) {
							annotFromFile = true;
							annotFile = file.getAbsolutePath();
						} else {
							networkFromFile = true;
							networkFile = file.getAbsolutePath();
						}
					}
				}
			});
			if (annotOrDB)
				annotActionListenerE = browseButton.getActionListeners()[0];
			else
				userDBActionListener = browseButton.getActionListeners()[0];

			return panel;
		}

		private JPanel createNodesInputDataPanelNew(final boolean src,
				final boolean expand) {

			JPanel panel = new JPanel(false);
			String title = expand ? "Input genes/proteins"
					: src ? " Source genes/proteins: "
							: " Target genes/proteins: ";
			setBorder(panel, title);

			final JRadioButton browseMode = new JRadioButton("Read from file");
//			browseMode
//					.setToolTipText(" There should be one name per line in the input file ");
			final JRadioButton textMode = new JRadioButton(
					"List of names                        ");
//			textMode.setToolTipText(" Genes/Proteins should be separated by comma ");
			ButtonGroup group = new ButtonGroup();
			group.add(browseMode);
			group.add(textMode);
			textMode.setSelected(true);
			group.setSelected(textMode.getModel(), true);

			final JPanel browseOuterPanel = new JPanel(false);

			JPanel browsePanel = new JPanel(false);
			final JTextField fileAddress = new JTextField(20);
//			fileAddress
//					.setToolTipText(" There should be one name per line in the input file ");
			final JButton browseButton = new JButton("Browse");
			browsePanel.add(browseButton);
			browsePanel.add(fileAddress);
			browseOuterPanel.add(browseMode);
			browseOuterPanel.add(browsePanel);

			BalloonTipStyle btStyle = new RoundedBalloonStyle(5, 5, new Color(255, 255, 204), new Color(204, 204, 0));
			btStyle.setHorizontalOffset(20);
			btStyle.setVerticalOffset(10);
			BalloonTip bt;
			if (src)
			{
				bt = new BalloonTip(browseButton, 
						"<html>There should be one name per line in the input file. <br><br><i>e.g.<i> <br>pou5f1 <br>nanog <br>sall4</html>",
						btStyle, false);
			}	
			else
			{
				bt = new BalloonTip(browseButton, 
						"<html>There should be one name per line in the input file. <br><br><i>e.g.<i> <br>cdx2 <br>eomes <br>tead4</html>",
						btStyle,
						false);
			}
			ToolTipUtils.balloonToToolTip(bt, 1, 7000);

			final JPanel textOuterPanel = new JPanel(false);
			textOuterPanel.setLayout(new FlowLayout());
			JPanel textPanel = new JPanel(false);
			final JTextField textfield = new JTextField(20);
			BalloonTip bt2;
			if (src)
			{
				bt2 = new BalloonTip(textfield, 
						"<html>Genes/Proteins sholud be separated by comma. <br><i>e.g.<i> pou5f1, nanog, sall4</html>",
						btStyle, false);
			}	
			else
			{
				bt2 = new BalloonTip(textfield, 
						"<html>Genes/Proteins sholud be separated by comma. <br><i>e.g.<i> cdx2, eomes, tead4</html>",
						btStyle,
						false);
			}
			ToolTipUtils.balloonToToolTip(bt2, 1, 7000);
			textPanel.add(textfield);
			textOuterPanel.add(textMode);
			textOuterPanel.add(textPanel);

			panel.setLayout(new GridLayout(2, 1));
			panel.add(textOuterPanel);
			panel.add(browseOuterPanel);

			browseOuterPanel.setEnabled(false);
			browseButton.setEnabled(false);
			fileAddress.setEnabled(false);

			if (src) {
				srcTextFieldE = textfield;
				srcfileAddressE = fileAddress;
			} else {
				dstTextField = textfield;
				dstfileAddress = fileAddress;
			}

			final JFileChooser fc = new JFileChooser();

			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						fileAddress.setText(file.getAbsolutePath());
					}
				}
			});
			if (src)
				srcActionListenerE = browseButton.getActionListeners()[0];
			else
				dstActionListener = browseButton.getActionListeners()[0];
			ChangeListener myChangeListener = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (browseMode.isSelected()) {
						textOuterPanel.setEnabled(false);
						textfield.setEnabled(false);
						if (src)
							srcFromFileE = true;
						else
							dstFromFile = true;

						browseOuterPanel.setEnabled(true);
						browseButton.setEnabled(true);
						fileAddress.setEnabled(true);

					} else {
						textOuterPanel.setEnabled(true);
						textfield.setEnabled(true);
						if (src)
							srcFromFileE = false;
						else
							dstFromFile = false;
						browseOuterPanel.setEnabled(false);
						browseButton.setEnabled(false);
						fileAddress.setEnabled(false);
					}
				}
			};
			browseMode.addChangeListener(myChangeListener);
			textMode.addChangeListener(myChangeListener);

			return panel;
		}

		private JPanel createNodesInputDataPanel(final boolean src) {

			JPanel panel = new JPanel(false);
			String title = src ? " Source genes/proteins: "
					: " Target genes/proteins: ";
			setBorder(panel, title);

			final JRadioButton browseMode = new JRadioButton("Read from file");
			browseMode
					.setToolTipText(" There should be one name per line in the input file ");
			final JRadioButton textMode = new JRadioButton(
					"List of names                        ");
			textMode.setToolTipText(" Genes/Proteins should be separated by comma ");

			ButtonGroup group = new ButtonGroup();
			group.add(browseMode);
			group.add(textMode);
			textMode.setSelected(true);
			group.setSelected(textMode.getModel(), true);

			final JPanel browseOuterPanel = new JPanel(false);

			JPanel browsePanel = new JPanel(false);
			final JTextField fileAddress = new JTextField(20);
			
			
			final JButton browseButton = new JButton("Browse");
			browsePanel.add(browseButton);
			browsePanel.add(fileAddress);
			browseOuterPanel.add(browseMode);
			browseOuterPanel.add(browsePanel);

			BalloonTipStyle btStyle = new RoundedBalloonStyle(5, 5, new Color(255, 255, 204), new Color(204, 204, 0));
			btStyle.setHorizontalOffset(20);
			btStyle.setVerticalOffset(10);
			BalloonTip bt;
			if (src)
			{
				bt = new BalloonTip(browseButton, 
						"<html>There should be one name per line in the input file. <br><br><i>e.g.<i> <br>pou5f1 <br>nanog <br>sall4</html>",
						btStyle, false);
			}	
			else
			{
				bt = new BalloonTip(browseButton, 
						"<html>There should be one name per line in the input file. <br><br><i>e.g.<i> <br>cdx2 <br>eomes <br>tead4</html>",
						btStyle,
						false);
			}
			ToolTipUtils.balloonToToolTip(bt, 1, 7000);

			final JPanel textOuterPanel = new JPanel(false);
			textOuterPanel.setLayout(new FlowLayout());
			JPanel textPanel = new JPanel(false);
			final JTextField textfield = new JTextField(20);
			BalloonTip bt2;
			if (src)
			{
				bt2 = new BalloonTip(textfield, 
						"<html>Genes/Proteins sholud be separated by comma. <br><i>e.g.<i> pou5f1, nanog, sall4</html>",
						btStyle, false);
			}	
			else
			{
				bt2 = new BalloonTip(textfield, 
						"<html>Genes/Proteins sholud be separated by comma. <br><i>e.g.<i> cdx2, eomes, tead4</html>",
						btStyle,
						false);
			}
			ToolTipUtils.balloonToToolTip(bt2, 1, 7000);
			/*
			if (src)
				textfield.setText("nanog, pou5f1, sall4");
			else
				textfield.setText("cdx2, eomes, tead4");
			 */
			textPanel.add(textfield);
			textOuterPanel.add(textMode);
			textOuterPanel.add(textPanel);

			panel.setLayout(new GridLayout(2, 1));
			panel.add(textOuterPanel);
			panel.add(browseOuterPanel);

			browseOuterPanel.setEnabled(false);
			browseButton.setEnabled(false);
			fileAddress.setEnabled(false);

			if (src) {
				srcTextField = textfield;
				srcfileAddress = fileAddress;
			} else {
				dstTextField = textfield;
				dstfileAddress = fileAddress;
			}

			final JFileChooser fc = new JFileChooser();

			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						fileAddress.setText(file.getAbsolutePath());
					}
				}
			});
			if (src)
				srcActionListener = browseButton.getActionListeners()[0];
			else
				dstActionListener = browseButton.getActionListeners()[0];
			ChangeListener myChangeListener = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (browseMode.isSelected()) {
						textOuterPanel.setEnabled(false);
						textfield.setEnabled(false);
						if (src)
							srcFromFile = true;
						else
							dstFromFile = true;

						browseOuterPanel.setEnabled(true);
						browseButton.setEnabled(true);
						fileAddress.setEnabled(true);

					} else {
						textOuterPanel.setEnabled(true);
						textfield.setEnabled(true);
						if (src)
							srcFromFile = false;
						else
							dstFromFile = false;
						browseOuterPanel.setEnabled(false);
						browseButton.setEnabled(false);
						fileAddress.setEnabled(false);
					}
				}
			};
			browseMode.addChangeListener(myChangeListener);
			textMode.addChangeListener(myChangeListener);

			return panel;
		}

		private void loadUserDatabasesAndNomen() throws Exception {
			int species = 0;
			if (humanRadioButton.isSelected()) {
				DATAspecies = HUMAN;
				species = 0;
			}
			if (mouseRadioButton.isSelected()) {
				DATAspecies = MOUSE;
				species = 1;
			}
			nomen = new Nomenclature(DATAspecies, annotFile, true);
			convertFile(DATAspecies, nomen);// file
			// binary-species-User-PPI.txt
			// should be created from
			// nomenClature

			String databaseName = "User";
			
			subNetworks = new HashMap<String, StrongestPath>();
			try {
				subNetworks.put(databaseName, new StrongestPath(species, nomen,
						"binary-" + DATAspecies + "-" + databaseName + "-PPI.txt"));
				subNetworks.put(databaseName, new StrongestPath(species, nomen,
						"binary-" + DATAspecies + "-" + databaseName + "-PPI.txt"));				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				// TODO: handle exception
			}

		}

		private void loadAllDatabasesAndNomen() {
			int species = 0;
			if (humanRadioButton.isSelected()) {
				DATAspecies = HUMAN;
				species = 0;
			}
			if (mouseRadioButton.isSelected()) {
				DATAspecies = MOUSE;
				species = 1;
			}
			try {
				nomen = new Nomenclature(DATAspecies, null, false);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}
			subNetworks = new HashMap<String, StrongestPath>();
			// subNetworks = new HashMap<String, StrongestPath>();
			
			subNetworks.remove("User");
			for (String databaseName : databases) {
				if (DATAspecies.equals(MOUSE)
						&& excludeDatabases.contains(databaseName))
					continue;
				try {
					subNetworks.put(databaseName, new StrongestPath(species,
							nomen, "binary-" + DATAspecies + "-" + databaseName
									+ "-PPI.txt"));

				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					e.printStackTrace();
				}
			}
			

		}

		private void doExpand() {
			try {
				expandAndShowNetwork(nomen, DATAdatabaseNames, numberOfNewNodes);
				bringToTheFront();
			} catch (Exception e) {
				String message = "[doExpand: Step " + step + "] Error: ";
				JOptionPane.showMessageDialog(null,
						message + "(" + e.getMessage() + ")");
				e.printStackTrace();

			}
		}

		private void doDisplayNetwork() {

			int species = 0;
			if (DATAspecies.equals(HUMAN))
				species = 0;
			else if (DATAspecies.equals(MOUSE))
				species = 1;

			step = 0;

			try {

				// ***** Converts the database according to the nomenClature
				// *****//
				if (networkFromFile) {

					DATAdatabaseNames.add("User");
				}
				// *** to here *** //
				step = 1;
				String[] sources = getGenes(DATAsrcfilePath, DATAsrctextField,
						srcFromFileE);

				step = 2;
				showNetwork(species, nomen, sources, DATAdatabaseNames);
				bringToTheFront();

			} catch (Exception e) {
				String message = "[doDisplayNetwork: Step " + step
						+ "] Error: ";
				JOptionPane.showMessageDialog(null,
						message + "(" + e.getMessage() + ")");
				e.printStackTrace();

			}

		}

		private void showNetwork(int species, Nomenclature nomen,
				String[] sources, ArrayList<String> dATAdatabaseNames)
				throws Exception {


			for (String databaseName : dATAdatabaseNames) {
				StrongestPath strongestPath = null;
				if(!userDatabase && databaseName.equals("User"))
					continue;
				strongestPath = subNetworks.get(databaseName);
				step = 3;
				
				strongestPath.setSources(sources);
				step = 4;
				Vector<Pair> edges = strongestPath.getSubNetwork(databaseName);
				step = 6;
				visaulizeNetwork(edges, sources, nomen, databaseName);

			}
		}

		private void expandAndShowNetwork(Nomenclature nomen,
				ArrayList<String> dATAdatabaseNames, int numberOfNewNodes)
				throws Exception {

			CyNetwork network = adapter.getCyApplicationManager()
					.getCurrentNetwork();
			String databaseName = network.getRow(network).get(CyNetwork.NAME,
					String.class);
			StrongestPath strongestPath = subNetworks.get(databaseName);
			step = 3;
			Vector<Pair> edges = null;
			try {
				edges = strongestPath.expandAndGetSubNetwork(databaseName,
						numberOfNewNodes);
			} catch (NullPointerException e) {
				throw new Exception("Please select the correct network");
			}
			step = 4;
			if (edges.size() != 0)
				visaulizeNetwork(edges, strongestPath.getSubGraph(), nomen,
						databaseName, false);
			else
				throw new Exception("These genes have no neighbors in "
						+ databaseName + "!");
		}

		private void doKStrongestPath() {
			int species = 0;
			if (DATAspecies.equals(HUMAN))
				species = 0;
			else if (DATAspecies.equals(MOUSE))
				species = 1;

			step = 0;

			try {

				// ***** Converts the database according to the nomenClature
				// *****//
				if (networkFromFile) {
					DATAdatabaseNames.add("User");

				}

				// *** to here *** //
				step = 1;
				String[] sources = getGenes(DATAsrcfilePath, DATAsrctextField,
						srcFromFile);
				step = 2;
				String[] destinations = getGenes(DATAdstfilePath,
						DATAdsttextField, dstFromFile);
				for (int i = 0; i < sources.length; i++)
					for (int j = 0; j < destinations.length; j++) {
						if (sources[i].equals(destinations[j]))
							throw new Exception(
									"'" + sources[i]
											+ "' is in both sources and destination genes.");
					}
			
				doStrongestPathOnEachDatabase(species, nomen, sources,
						destinations, DATAdatabaseNames);
				bringToTheFront();
			} catch (Exception e) {
				String message = "[doKStrongestPath: Step " + step
						+ "] Error: ";
				JOptionPane.showMessageDialog(null,
						message + "(" + e.getMessage() + ")");
				e.printStackTrace();

			}

		}

		public void convertFile(String species, Nomenclature nomen)
				throws Exception {
			String root = new File(getRoot(), "files").toString();
			File file = new File(new File(root, species).toString(), "/binary-"
					+ species + "-User-PPI.txt");
			File file2 = new File(new File(root, species).toString(),
					"/binary-" + species + "-User-PPI-Inverted.txt");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(networkFile)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					file.getAbsoluteFile()));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(
					file2.getAbsoluteFile()));
			String line;
			String[] listGeneID;
			LineNumberReader lnr = new LineNumberReader(new FileReader(
					new File(networkFile)));
			lnr.skip(Long.MAX_VALUE);
			int lines = lnr.getLineNumber() - 1;
			if (lines < 0)
				throw new Exception("Database file is corrupted!");
			int[][] fileArray;
			fileArray = new int[lines][3];
			int i = 0;
			br.readLine(); // skip the header
			while ((line = br.readLine()) != null) {
				try {

					listGeneID = line.split("\t");
					fileArray[i][0] = nomen.NametoID(listGeneID[0]);
					fileArray[i][1] = nomen.NametoID(listGeneID[1]);
					fileArray[i][2] = Integer.parseInt(listGeneID[2]);
					i++;
				} catch (Exception e) {
					throw new Exception(
							"Network file is not appropriate @line " + i + " ("
									+ e.getMessage() + ")");

				}
			}

			java.util.Arrays.sort(fileArray, new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2) {
					return ((Integer) o2[0]).compareTo(o1[0]);
				}
			});
			i--;
			while (i >= 0) {
				bw.write(fileArray[i][0] + "\t" + fileArray[i][1] + "\t"
						+ fileArray[i][2] + "\n");
				i--;
			}
			for (int j = 0; j < lines; j++) {
				swap(fileArray, j, 0, 1);

			}
			i = lines;
			java.util.Arrays.sort(fileArray, new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2) {
					return ((Integer) o2[0]).compareTo(o1[0]);
				}
			});
			i--;
			while (i >= 0) {
				bw2.write(fileArray[i][0] + "\t" + fileArray[i][1] + "\t"
						+ fileArray[i][2] + "\n");
				i--;
			}
			bw.close();
			bw2.close();
			br.close();
			lnr.close();

		}

		public void swap(int[][] arr, int line, int pos1, int pos2) {
			int temp = arr[line][pos1];
			arr[line][pos1] = arr[line][pos2];
			arr[line][pos2] = temp;
		}

		private void doStrongestPathOnEachDatabase(int species,
				Nomenclature nomen, String[] sources, String[] destinations,
				ArrayList<String> dATAdatabaseNames) throws Exception {

			Map<String, Double> confidences = new HashMap<String, Double>();
			StrongestPath strongestPath = null;
			for (String databaseName : dATAdatabaseNames) {
				if(!userDatabase && databaseName.equals("User"))
					continue;
				strongestPath = subNetworks.get(databaseName);
				step = 3;
				strongestPath.setSources(sources);
				step = 4;
				strongestPath.setDestinations(destinations);
				step = 5;
				Vector<Pair> edges = strongestPath
						.getStrongestPathsGraph(DATAthreshold);
				step = 6;
				Vector<Vector<Pair>> vecTemp = new Vector<Vector<Pair>>();
				vecTemp.add(edges);
				HashSet<Integer> nodeset = new BiConnected(1).edgeToVertex(
						vecTemp).get(0);
				nodeset.remove(0);

				nodeset.remove(strongestPath.destGraph.proteinsCount);
				confidences = strongestPath.getConfidences(nodeset);
				step = 7;
				BFSInfos heights = getHeightsByBFS(edges, nodeset, sources,
						destinations, nomen, strongestPath);
				step = 8;
				visualizeEdges(sources, destinations, edges, heights,
						confidences, true, nomen, databaseName, strongestPath);

				step = 9;
			}

		}

		private BFSInfos getHeightsByBFS(Vector<Pair> edges,
				HashSet<Integer> nodeset, String[] sources,
				String[] destinations, Nomenclature nomen, StrongestPath sp)
				throws Exception {
			HashMap<Integer, Integer> heights = new HashMap<Integer, Integer>();
			HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>();
			int src = 0;
			int dst = sp.destGraph.proteinsCount;

			for (Pair p : edges) {
				if (p.l == src || p.l == dst || p.r == src || p.r == dst)
					continue;
				if (!graph.containsKey(p.r))
					graph.put(p.r, new HashSet<Integer>());
				if (!graph.containsKey(p.l))
					graph.put(p.l, new HashSet<Integer>());

				HashSet<Integer> neighbors, neighbors2;
				neighbors = graph.get(p.r);
				neighbors.add(p.l);
				graph.put(p.r, neighbors);
				neighbors2 = graph.get(p.l);
				neighbors2.add(p.r);
				graph.put(p.l, neighbors2);
			}

			ArrayList<Integer> queue = new ArrayList<Integer>();
			HashSet<Integer> marked = new HashSet<Integer>();
			int start, end;
			int current;
			int visitTime[] = new int[100000];

			HashMap<Integer, Integer> visitTimes = new HashMap<Integer, Integer>();
			int temp = sources.length - 1;
			for (String srcGene : sources) {
				current = nomen.NametoID(srcGene);
				heights.put(current, 0);
				marked.add(current);
				if (graph.containsKey(current)) {
					queue.add(current);
					visitTimes.put(current, visitTime[0]++);
				} else
					visitTimes.put(current, temp--);
			}
			end = queue.size();
			int current_height;
			start = 0;
			int max = 0;

			while (end != start) {
				current = queue.get(start);
				current_height = heights.get(current);
				start++;
				for (Integer child : graph.get(current)) {
					if (!marked.contains(child)) {
						heights.put(child, current_height + 1);
						visitTimes.put(child, visitTime[current_height + 1]++);
						if (max < current_height + 1)
							max = current_height + 1;
						queue.add(child);
						marked.add(child);
						end++;
					}
				}
			}
			int count = 0;
			for (Entry<Integer, Integer> e : heights.entrySet()) {
				if (e.getValue() == max)
					count++;
			}
			if (max == 0 || count > destinations.length)
				max++;
			else
				visitTime[max]--;
			for (String dstGene : destinations) {
				heights.put(nomen.NametoID(dstGene), max);
				visitTimes.put(nomen.NametoID(dstGene), visitTime[max]++);
			}

			BFSInfos bfs = new BFSInfos();
			bfs.heights = heights;
			bfs.visitTimes = visitTimes;

			return bfs;
		}

		private void visaulizeNetwork(Vector<Pair> edges,
				HashMap<Integer, Integer> subGraph, Nomenclature nomen,
				String title, boolean createNetwork)
				throws NumberFormatException, Exception {
			String networkTitle = "Strongest path network view";
			CyNetwork network = getNetwork(createNetwork, title);
			CyNetworkView networkView = getNetworkView(createNetwork, network);
			adapter.getCyNetworkManager().addNetwork(network);
			adapter.getCyNetworkViewManager().addNetworkView(networkView);

			Integer max = Collections.max(subGraph.values());

			/* find nodes ids' */

			ArrayList<Entry<Integer, Integer>> allNodes = new ArrayList<Entry<Integer, Integer>>(
					subGraph.entrySet());
			Collections.sort(allNodes,
					new Comparator<Entry<Integer, Integer>>() {
						@Override
						public int compare(Entry<Integer, Integer> arg0,
								Entry<Integer, Integer> arg1) {
							if (arg0.getValue() > arg1.getValue())
								return 1;
							else if (arg0.getValue() < arg1.getValue())
								return -1;
							else
								return 0;
						}

					});
			Integer[] nodes = new Integer[allNodes.size()];
			for (int i = 0; i < allNodes.size(); i++) {
				nodes[i] = allNodes.get(i).getKey();

			}
			String[] nodeIds = new String[nodes.length];
			for (int i = 0; i < nodes.length; i++) {
				nodeIds[i] = nodes[i].toString();
			}

			/* add nodes to network */
			ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
			ArrayList<String> newStrings = new ArrayList<String>();
			for (String s : nodeIds) {

				// Added the nodes to the network with name equal to node ID
				CyNode node;
				node = getNodeWithValue(network, "nodeID", s);
				if (node == null) {
					node = network.addNode();
					network.getRow(node).set("nodeID", s);
					newNodes.add(node);
					newStrings.add(s);
				}

				/* Change color and shape of the node in the network */

			}
			adapter.getCyEventHelper().flushPayloadEvents();

			for (int i = 0; i < allNodes.size(); i++) {
				int g = (255 / (max)) * (max - allNodes.get(i).getValue());

				nodeStyleWithShape(
						getNodeWithValue(network, "nodeID", allNodes.get(i)
								.getKey().toString()), nomen.Convert(
								nomen.IDtoName(allNodes.get(i).getKey()),
								"Official_Gene_Symbol"),
						NodeShapeVisualProperty.ELLIPSE, new Color(255, g, g));
				
				/*nodeStyleWithShape(newNodes.get(i), nomen.Convert(
						nomen.IDtoName(Integer.parseInt(s)),
						"Official_Gene_Symbol"),
						NodeShapeVisualProperty.ELLIPSE, null);*/
			}

			/* add edges to the network */
			CyEdge edge;
			CyNode node1, node2;

			for (Pair p : edges) {
				/*
				 * OLD 2.x node1 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.left).toString(), true);
				 * node2 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.right).toString(),
				 * true);
				 */
				// New 3.x
				node1 = getNodeWithValue(network, "nodeID",
						nomen.NametoID(p.left).toString());
				node2 = getNodeWithValue(network, "nodeID",
						nomen.NametoID(p.right).toString());

				/*
				 * Old 2.x cyNodeAttrs = Cytoscape.getNodeAttributes();
				 * 
				 * edge = Cytoscape.getCyEdge(node1, node2,
				 * Semantics.INTERACTION, "pp", true); cyEdgeAttrs =
				 * Cytoscape.getEdgeAttributes();
				 * cyEdgeAttrs.setAttribute(edge.getSUID(), "Database",
				 * p.dataBaseName); if (!network.containsEdge(edge))
				 * network.addEdge(edge);
				 */

				// New
				if (!network.containsEdge(node1, node2)) {
					edge = network.addEdge(node1, node2, true);
					network.getRow(edge).set("Database", p.dataBaseName);
				}

			}

			/*************************************/

			// Create the visual style
			buildNetwork(network, networkView, networkTitle + title, false);
			applyTableLayout(network, nodeIds, nomen);

		}

		private void visaulizeNetwork(Vector<Pair> edges, String[] nodes,
				Nomenclature nomen, String title) throws Exception {
			// String networkTitle = "Strongest path network view: ";
			/*
			 * Old 2.x CyNetwork network = Cytoscape.createNetwork(title);
			 */
			// New 3.x
			CyNetwork network = getNetwork(true, title);
			CyNetworkView networkView = getNetworkView(true, network);
			adapter.getCyNetworkManager().addNetwork(network);
			adapter.getCyNetworkViewManager().addNetworkView(networkView);
			// CyNetwork network =
			// adapter.getCyNetworkFactory().createNetwork();
			// network.getRow(network).set(CyNetwork.NAME, title);
			// addNodeIDColumn(network);
			// Old 2.x
			// graphStyle.setNodeSizeLocked(false);

			/* find nodes ids' */
			String[] srcIds = new String[nodes.length];
			Integer tempId;
			for (int i = 0; i < srcIds.length; i++) {
				tempId = nomen.NametoID(nodes[i].trim());
				if (tempId == -1)
					throw new Exception("There is no gene with this id: "
							+ nodes[i]);
				srcIds[i] = tempId.toString();
			}

			/* add nodes to network */

			ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
			ArrayList<String> newStrings = new ArrayList<String>();
			for (String s : srcIds) {
				/*
				 * Old 2.x node = Cytoscape.getCyNode(s, true);
				 * network.addNode(node);
				 */
				// New 3.x
				CyNode node;
				node = network.addNode();
				network.getRow(node).set("nodeID", s);
				/* change shape and color of the nodes */
				step = 40;
				newStrings.add(s);
				newNodes.add(node);
			}
			adapter.getCyEventHelper().flushPayloadEvents();
			String s;

			for (int i = 0; i < newNodes.size(); i++) {
				s = newStrings.get(i);
				if (!s.equals("source") && !s.equals("destination")) {
					nodeStyleWithShape(newNodes.get(i), nomen.Convert(
							nomen.IDtoName(Integer.parseInt(s)),
							"Official_Gene_Symbol"),
							NodeShapeVisualProperty.ELLIPSE, null);
				}

			}

			/* add edges to the network */
			CyEdge edge;
			/*
			 * Old 2.x CyAttributes cyNodeAttrs; CyAttributes cyEdgeAttrs;
			 */
			CyNode node1, node2;

			for (Pair p : edges) {
				/*
				 * Old 2.x node1 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.left).toString(), true);
				 * node2 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.right).toString(),
				 * true);
				 */
				// New 3.x
				node1 = getNodeWithValue(network, "nodeID",
						nomen.NametoID(p.left).toString());
				node2 = getNodeWithValue(network, "nodeID",
						nomen.NametoID(p.right).toString());

				/*
				 * Old 2.x cyNodeAttrs = Cytoscape.getNodeAttributes(); edge =
				 * Cytoscape.getCyEdge(node1, node2, Semantics.INTERACTION,
				 * "pp", true); cyEdgeAttrs = Cytoscape.getEdgeAttributes();
				 * cyEdgeAttrs.setAttribute(edge.getSUID(), "Database",
				 * p.dataBaseName); if (!network.containsEdge(edge))
				 * network.addEdge(edge);
				 */
				// New 3.x
				if (!network.containsEdge(node1, node2)) {
					edge = network.addEdge(node1, node2, true);
					network.getRow(edge).set("Database", p.dataBaseName);
				}

			}

			/*************************************/

			// Create the visual style
			buildNetwork(network, networkView, title, false);
			applyTableLayout(network, srcIds, nomen);

		}

		private void visualizeEdges(String[] srcIds, String[] dstIds,
				Vector<Pair> edges, BFSInfos heights,
				Map<String, Double> confidences, boolean createNetwork,
				Nomenclature nomen, String title, StrongestPath strongestPath)
				throws Exception {

			String networkTitle = "Strongest path network: ";

			CyNetwork network = getNetwork(createNetwork, title);
			CyNetworkView networkView = getNetworkView(createNetwork, network);

			adapter.getCyNetworkManager().addNetwork(network);
			adapter.getCyNetworkViewManager().addNetworkView(networkView);
			/*******/
			/* change style of nodes */

			// graphStyle.setNodeSizeLocked(false);

			// set some visual property for two nodes
			Integer tempId;
			String[] srcIds2 = new String[srcIds.length];
			for (int i = 0; i < srcIds2.length; i++) {
				tempId = nomen.NametoID(srcIds[i].trim());
				if (tempId == -1)
					throw new Exception("There is no gene with this id: "
							+ srcIds[i]);
				srcIds2[i] = tempId.toString();
			}
			String[] dstIds2 = new String[dstIds.length];
			for (int i = 0; i < dstIds2.length; i++) {
				tempId = nomen.NametoID(dstIds[i].trim());
				if (tempId == -1)
					throw new Exception("There is no gene with this name: "
							+ dstIds[i]);
				dstIds2[i] = tempId.toString();
			}

			/**********/

			if (createNetwork) {
				step = 22;
				ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
				ArrayList<String> newStrings = new ArrayList<String>();
				for (String s : srcIds2) {
					CyNode node;
					/*
					 * Old 2.x node = Cytoscape.getCyNode(s, true);
					 * network.addNode(node);
					 */
					// New 3.x
					node = network.addNode();
					network.getRow(node).set("nodeID", s);
					newNodes.add(node);
					newStrings.add(s);

				}
				adapter.getCyEventHelper().flushPayloadEvents();
				String s;
				for (int i = 0; i < newNodes.size(); i++) {
					s = newStrings.get(i);
					if (!s.equals("source") && !s.equals("destination")) {
						nodeStyleWithShape(newNodes.get(i), nomen.Convert(
								nomen.IDtoName(Integer.parseInt(s)),
								"Official_Gene_Symbol"),
								NodeShapeVisualProperty.ELLIPSE, null);
					}

				}

				newNodes.clear();
				newStrings.clear();
				for (String s1 : dstIds2) {
					CyNode node;
					/*
					 * Old 2.x node = Cytoscape.getCyNode(s, true);
					 * network.addNode(node);
					 */
					// New 3.x
					node = network.addNode();
					network.getRow(node).set("nodeID", s1);
					newNodes.add(node);
					newStrings.add(s1);
				}
				adapter.getCyEventHelper().flushPayloadEvents();
				String s1;
				for (int i = 0; i < newNodes.size(); i++) {
					s1 = newStrings.get(i);
					if (!s1.equals("source") && !s1.equals("destination")) {
						nodeStyleWithShape(newNodes.get(i), nomen.Convert(
								nomen.IDtoName(Integer.parseInt(s1)),
								"Official_Gene_Symbol"),
								NodeShapeVisualProperty.ELLIPSE, null);
					}

				}
			}

			CyEdge edge;
			/*
			 * Old 2.x CyAttributes cyNodeAttrs; CyAttributes cyEdgeAttrs;
			 */

			int src = 0;
			int dst = strongestPath.destGraph.proteinsCount;

			/********** DRAW EDGES ***************/
			step = 25;
			int nodeID1, nodeID2;
			ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
			ArrayList<Integer> newNodesIDs = new ArrayList<Integer>();
			for (Pair p : edges) {
				if (p.l == src || p.l == dst || p.r == src || p.r == dst)
					continue;
				/*
				 * Old 2.x node1 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.left).toString(), true);
				 * node2 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.right).toString(),
				 * true);
				 */
				CyNode node1, node2;
				nodeID1 = nomen.NametoID(p.left);
				nodeID2 = nomen.NametoID(p.right);
				step = 26;
				Integer.toString(nodeID1);

				node1 = getNodeWithValue(network, "nodeID",
						Integer.toString(nodeID1));
				node2 = getNodeWithValue(network, "nodeID",
						Integer.toString(nodeID2));

				// if (!network.containsNode(node1)) {
				if (node1 == null) {
					/*
					 * Old 2.x network.addNode(node1);
					 */
					// TODO add node1 to the network
					step = 27;
					node1 = network.addNode();
					network.getRow(node1).set("nodeID",
							Integer.toString(nodeID1));

					step = 271;
					newNodes.add(node1);
					newNodesIDs.add(nodeID1);

					/*
					 * adapter.getCyEventHelper().flushPayloadEvents(); step =
					 * 272; nodeStyle3(node1, nomen, nodeID1); step = 273;
					 */
				}
				// if (!network.containsNode(node2)) {
				if (node2 == null) {
					/*
					 * Old 2.x network.addNode(node2);
					 */

					// TODO add node2 to the network
					node2 = network.addNode();
					network.getRow(node2).set("nodeID",
							Integer.toString(nodeID2));
					newNodes.add(node2);
					newNodesIDs.add(nodeID2);
					// adapter.getCyEventHelper().flushPayloadEvents();
					// step = 28;
					// nodeStyle3(node2, nomen, nodeID2);
				}

				/*** here ***/

				/*
				 * Old 2.x cyNodeAttrs = Cytoscape.getNodeAttributes();
				 * cyNodeAttrs.setAttribute(node1.getSUID(), "PathConfidence",
				 * 1.0 / Math.pow(2, confidences.get(p.left)));
				 * cyNodeAttrs.setAttribute(node2.getSUID(), "PathConfidence",
				 * 1.0 / Math.pow(2, confidences.get(p.right)));
				 */
				// New 3.x
				step = 29;
				/*
				network.getRow(node1).set(
						"Path Confidence",
						Double.toString(1.0 / Math.pow(2,
								confidences.get(p.left))));
				network.getRow(node2).set(
						"Path Confidence",
						Double.toString(1.0 / Math.pow(2,
								confidences.get(p.right))));
*/
				/*
				 * Old 2.x edge = Cytoscape.getCyEdge(node1, node2,
				 * Semantics.INTERACTION, "pp", true); cyEdgeAttrs =
				 * Cytoscape.getEdgeAttributes(); if (title.equals(""))
				 * cyEdgeAttrs.setAttribute(edge.getSUID(), "Database",
				 * p.dataBaseName); if (!network.containsEdge(edge))
				 * network.addEdge(edge);
				 */
				// New 3.x
				if (!network.containsEdge(node1, node2)) {
					edge = network.addEdge(node1, node2, true);
					network.getRow(edge).set("Database", p.dataBaseName);
				}

			}
			adapter.getCyEventHelper().flushPayloadEvents();
			for (int i = 0; i < newNodes.size(); i++) {
				nodeStyle3(newNodes.get(i), nomen, newNodesIDs.get(i));
			}
			/*************************************/
			// Create the visual style
			buildNetwork(network, networkView, networkTitle + title, true);

			applyColor(network, networkView, heights, nomen);
			applyBFSLayout(network, networkView, heights, nomen);
			// JOptionPane.showMessageDialog(null, "BFS");

		}

		private CyNetworkView getNetworkView(boolean createNetwork,
				CyNetwork network) {
			CyNetworkView networkView;
			if (createNetwork)
				networkView = adapter.getCyNetworkViewFactory()
						.createNetworkView(network);
			else
				networkView = adapter.getCyApplicationManager()
						.getCurrentNetworkView();
			return networkView;
		}

		private CyNetwork getNetwork(boolean createNetwork, String title) {
			CyNetwork network;
			if (createNetwork) {
				network = adapter.getCyNetworkFactory().createNetwork();
				network.getRow(network).set(CyNetwork.NAME, title);

			} else
				network = adapter.getCyApplicationManager().getCurrentNetwork();
			addNodeIDColumn(network);
			return network;
		}

		private void applyColor(CyNetwork network, CyNetworkView networkView,
				BFSInfos bfsInfos, Nomenclature nomen) {
			HashMap<Integer, Integer> heights = bfsInfos.heights;
			int max = 0;
			for (Entry<Integer, Integer> e : heights.entrySet())
				if (e.getValue() > max)
					max = e.getValue();

			CyNode node;
			step = 26;
			for (Entry<Integer, Integer> e : heights.entrySet()) {
				/*
				 * Old 2.x CyNode node1 =
				 * Cytoscape.getCyNode(e.getKey().toString(), true);
				 */
				node = getNodeWithValue(network, "nodeID", e.getKey()
						.toString());
				String red = Integer.toHexString((255 / max)
						* (max - e.getValue()));
				if (red.length() == 1)
					red = "0" + red;
				networkView.getNodeView(node).setVisualProperty(
						BasicVisualLexicon.NODE_FILL_COLOR,
						Color.decode("#ff" + red + red));
			}
			// Old 2.x
			// graphStyle.buildStyle();
		}

		private void applyBFSLayout(CyNetwork network,
				CyNetworkView networkView, BFSInfos bfsInfos, Nomenclature nomen) {
			CyNode node;
			HashMap<Integer, Integer> heights = bfsInfos.heights;
			HashMap<Integer, Integer> visitTimes = bfsInfos.visitTimes;

			for (Integer nodeID : heights.keySet()) {
				/*
				 * Old 2.x node = Cytoscape.getCyNode(nodeID.toString(), false);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setYPosition(heights.get(nodeID) * 80);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setXPosition(visitTimes.get(nodeID) * 80);
				 */
				// JOptionPane.showMessageDialog(null, "x: "+
				// heights.get(nodeID)+ ", y: "+visitTimes.get(nodeID));
				node = getNodeWithValue(network, "nodeID", nodeID.toString());
				networkView.getNodeView(node).setVisualProperty(
						BasicVisualLexicon.NODE_Y_LOCATION,
						(double) heights.get(nodeID) * 80);
				networkView.getNodeView(node).setVisualProperty(
						BasicVisualLexicon.NODE_X_LOCATION,
						(double) visitTimes.get(nodeID) * 80);
			}
			// Old 2.x
			// Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
			// networkView.updateView();
			networkView.updateView();
			adapter.getCyEventHelper().flushPayloadEvents();

		}

		private void applyTableLayout(CyNetwork network, String[] srcIds,
				Nomenclature nomen) {
			CyNode node;
			int r = 0, c = 0;
			int maxRow = (int) Math.ceil(Math.sqrt(srcIds.length));

			for (String nodeID : srcIds) {

				if (c == maxRow) {
					c = 0;
					r++;
				}
				/*
				 * Old 2.x node = Cytoscape.getCyNode(nodeID, false);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setYPosition(r * 80);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setXPosition(c * 80);
				 */
				/* New 3.x */
				node = getNodeWithValue(network, "nodeID", nodeID.toString());
				manager.getCurrentNetworkView()
						.getNodeView(node)
						.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
								(double) r * 80);
				manager.getCurrentNetworkView()
						.getNodeView(node)
						.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
								(double) c * 80);

				c++;
			}
			// Old 2.x
			// Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
			manager.getCurrentNetworkView().updateView();
			adapter.getCyEventHelper().flushPayloadEvents();
		}

		// private void nodeStyleWithShape(String nodeIdentifier, String label,
		private void nodeStyleWithShape(CyNode node, String label,
				NodeShape shape, Color color) {
			// New 3.x
			if (color == null) {
				color = new Color(0, 250, 250);
			}

			step = 41;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_LABEL, label);
			step = 42;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 50.0);
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 50.0);
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR,
							color);
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, shape);

			/*
			 * Old 2.x graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_LABEL, label);
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_WIDTH, "50");
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_HEIGHT, "50");
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_FILL_COLOR, "#00FAFA");
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_SHAPE, shape);
			 */
		}

		private String[] getGenes(String dATAdstfilePath,
				String dATAdsttextField, boolean dstFromFile)
				throws FileNotFoundException {
			String s;
			ArrayList<String> genes = new ArrayList<String>();
			if (dstFromFile) {
				Scanner sc = new Scanner(new File(dATAdstfilePath));
				while (sc.hasNext()) {
					s = sc.nextLine().trim();
					if (!"".equals(s))
						genes.add(s);
				}
				return genes.toArray(new String[genes.size()]);

			} else {
				String[] splited = dATAdsttextField.split(",");
				for (int i = 0; i < splited.length; i++) {
					splited[i] = splited[i].trim();
					if (!"".equals(splited[i]))
						genes.add(splited[i]);
				}
				return genes.toArray(new String[genes.size()]);
			}

		}

		private void setBorder(JPanel panel, String text) {
			TitledBorder title = new TitledBorder(
					BorderFactory.createTitledBorder(text));
			title.setTitlePosition(TitledBorder.LEFT);
			panel.setBorder(title);
		}

		public void showFrame() {
			frame = new JFrame();
			// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// frame.setResizable(false);
			/*
			 * frame.addWindowListener(new WindowAdapter() {
			 * 
			 * @Override public void windowClosing(WindowEvent e) {
			 * super.windowClosing(e); doFinalize();
			 * JOptionPane.showMessageDialog(null, "closed"); } });
			 */
			frame.setLocation(100, 50);
			// Add content to the window.
			frame.add(topPanel, BorderLayout.CENTER);

			// Display the window.
			frame.pack();
			frame.setVisible(true);
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			        try {
			        	doFinalize();
						
					} catch (Exception e) {
						// TODO: handle exception
						JOptionPane.showMessageDialog(null, "Didn't manage to finalize the app.");
					}
			        
			    }
			});
		}

		private void nodeStyle3(CyNode node, Nomenclature nomen, int nodeId)
				throws NumberFormatException, Exception {


			String officialSymbol = nomen.Convert(nomen.IDtoName(nodeId),
					"Official_Gene_Symbol");

			Color color = new Color(0, 250, 250);
			step = 281;
			if (manager.getCurrentNetworkView() == null)
				JOptionPane.showMessageDialog(null, "network view is null");
			if (manager.getCurrentNetworkView().getNodeView(node) == null)
				JOptionPane.showMessageDialog(null, officialSymbol
						+ " node view is null " + node.toString());
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_LABEL,
							officialSymbol);
			step = 282;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 50.0);
			step = 283;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 50.0);
			step = 284;
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR,
							color);
			step = 285;
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_SHAPE,
							NodeShapeVisualProperty.ELLIPSE);
		}

	}

	/*
	 * Old 2.x private void buildNetwork(CyNetwork network, String networkTitle,
	 * boolean disposeFrame) { graphStyle.buildStyle();
	 * 
	 * 
	 * 
	 * CyNetworkView view = Cytoscape.createNetworkView(network, networkTitle);
	 * 
	 * networkViewIdentifier = view.getSUID();
	 * view.applyLayout((CyLayoutAlgorithm) CyLayouts.getAllLayouts()
	 * .toArray()[0]); try { Cytoscape.getDesktop().getNetworkViewManager()
	 * .getInternalFrame(view).setMaximum(true); } catch
	 * (IllegalArgumentException e) { e.printStackTrace(); } catch
	 * (PropertyVetoException e) { e.printStackTrace(); } // if (disposeFrame)
	 * // frame.dispose();
	 * 
	 * }
	 */
	private void buildNetwork(CyNetwork network, CyNetworkView networkView,
			String networkTitle, boolean disposeFrame) {

	
		/*
		 * //TODO Convert next few lines to the new 3.x 
		 * CyLayoutAlgorithm layoutAlgorithm =
		 * adapter .getCyLayoutAlgorithmManager().getAllLayouts().iterator()
		 * .next(); TaskIterator itr =
		 * layoutAlgorithm.createTaskIterator(networkView,
		 * layoutAlgorithm.createLayoutContext(),
		 * CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		 * adapter.getTaskManager().execute(itr);
		 */
	}
	public static String getHigherFolder(String path)
	{
		int lastSepIndex = path.indexOf(File.separator, path.indexOf("Cytoscape"));
		String path1 = path.substring(0, lastSepIndex+1);
		String result;
		try {
			result = java.net.URLDecoder.decode(path1, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			result = path1.replace("%20", " ");
			e.printStackTrace();
		}
		return result;
	}
	public static String getRoot()
	{
		
		return getHigherFolder(new File(MyStrongestPathPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent());
	}
}
