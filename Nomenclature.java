import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Nomenclature {

	public final static String HUMANSPEICEID = "human";
	public final static String MOUSESPEICEID = "mouse";
	public final static String RATSPEICEID = "rat";
	public static int PROTEINSCOUNTRAT = 16902;
	public static int PROTEINSCOUNTMOUSE = 17919;
	public static int PROTEINSCOUNTHUMAN = 18600;

	public String species;

	String[] attributes = { "Refseq_peptide", "Refseq_mrna", "Ensembl_gene_id",
			"Affymetrix_id", "Ensembl_Transcript_ID", "Entrez_Gene_ID",
			"Unigene", "Official_Gene_Symbol", "Agilent_ID", "Illumina_ID",
			"Ensembl_peptide", "Uniprot_id" };
	
	boolean userNomen = false;
	Integer maxAttributes = attributes.length;
	String fileName;
	HashMap<String, Integer> map = new HashMap<String, Integer>();
	HashMap<Integer, String>[] annotationtoIDMap = new HashMap[maxAttributes + 1];

	public Nomenclature(String species, String fileAddress, boolean userNomen) throws Exception {
		this.userNomen = userNomen;
		for (int i = 0; i < annotationtoIDMap.length; i++) {
			annotationtoIDMap[i] = new HashMap<Integer, String>();
			annotationtoIDMap[i].clear();
		}
		this.species = species;

		if (fileAddress != null && !fileAddress.equals(""))
			fileName = fileAddress;
		else
			fileName = DataBaseManager.getDataBasePath(species, "Annotations-"
					+ species + ".txt");

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName)));

		String line;
		String[] listGeneID;
		String[] GeneID;

		int lineNumber = 0;
		int counter = 0;
		
		try {

			while ((line = br.readLine()) != null) {
				listGeneID = line.split("\t");
				for (int i = 1; i < annotationtoIDMap.length; i++) {
					if (listGeneID.length >= i) {
						annotationtoIDMap[i].put(lineNumber,
								listGeneID[i-1].toLowerCase());
					}
				}

				for (String list : listGeneID) {

					GeneID = list.split(",");
					for (String ID : GeneID) {
						map.put(ID.toLowerCase(), lineNumber);

					}
				}
				lineNumber++;
			}
		} catch (Exception e) {
			throw new Exception("Annotation file is corrupted!");
		}

		br.close();
	}

	Integer AttributetoID(String attr) {
		if(userNomen)
			return 1;
		for (int i = 1; i < attributes.length; i++)
			if (attributes[i].toLowerCase().equals(attr.toLowerCase()))
				return i;
		return 1;
	}

	Integer NametoID(String name) throws Exception {
		if(name.equals("") || !map.containsKey(name.toLowerCase()))
			throw new Exception("There is no gene with the name '"+ name +"' in annotations file.");
		return map.get(name.toLowerCase());

	}

	String IDtoName(Integer ID) {
		for (int i = 1; i < attributes.length; i++)
			if (annotationtoIDMap[i].get(ID) != null)
				return annotationtoIDMap[1].get(ID).toString();
		return null;

	}

	String Convert(String name, String destType) throws Exception {
		Integer destIndex = userNomen ? 1 : 8; 
		//AttributetoID(destType); // We can use this function to show the results with any attribute we want, 
								   // but for now we just use first column for user nomen and 8th column for our nomen.
		Integer line = NametoID(name);
		String st1;
		if (destIndex != -1) {
			st1 = annotationtoIDMap[destIndex].get(line);
			if (st1 != null)
//				return annotationtoIDMap[destIndex].get(line).toString();
				return st1.toString();
		}

		return "";
	}

}
