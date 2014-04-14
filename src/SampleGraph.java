
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class SampleGraph {
	
	public final String HUMANSPEICEID = Nomenclature.HUMANSPEICEID;
	public final String MOUSESPEICEID = Nomenclature.MOUSESPEICEID;
	public final String RATSPEICEID = Nomenclature.RATSPEICEID;
	public static int PROTEINSCOUNTRAT = 139498;
	public static int PROTEINSCOUNTMOUSE = 87999;
	public static int PROTEINSCOUNTHUMAN = 139498;
	
	public static double D = 0.95;
	public static double logD = Math.log(1/D);
	
	public int proteinsCount;
	public String species;
	private String filename;
	int[] proteins=new int [PROTEINSCOUNTHUMAN+10];
	int[] newproteins=new int [PROTEINSCOUNTHUMAN+10];
	int[][] neighbors=new int [2][3281415+10];

	public SampleGraph(int spiece, String databaseName) throws IOException
	{
		
		switch (spiece)
		{
		case 0:
			this.species = HUMANSPEICEID;
			proteinsCount = PROTEINSCOUNTHUMAN;
			break;
		case 1:
			this.species = MOUSESPEICEID;
			proteinsCount = PROTEINSCOUNTMOUSE;
			break;
		case 2:
			this.species = RATSPEICEID;
			proteinsCount = PROTEINSCOUNTRAT;
		}
		initialize(DataBaseManager.getDataBasePath(species, databaseName));
	}
	
	
	private void initialize(String databasePath) throws IOException
	{
		String strLine;
		String[] line;
		filename = databasePath;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(databasePath)));
		
		int i=0;
		while ((strLine = br.readLine())!=null ) {
			i++;
			line = strLine.split("\t");
			proteins[Integer.parseInt(line[0])]++;
			neighbors[0][i]=Integer.parseInt(line[1]);
			neighbors[1][i]=Integer.parseInt(line[2]);
			
		}
		newproteins[1]=1;
		for (int k=2;k<=proteinsCount;k++)
		{
			newproteins[k]=proteins[k-1]+newproteins[k-1];
		}
	}
	
	
	
}
