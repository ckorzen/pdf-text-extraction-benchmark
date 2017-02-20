package edu.isi.bmkeg.lapdf.classification.ruleBased;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DecisionTableFactory;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

import edu.isi.bmkeg.lapdf.classification.Classifier;
import edu.isi.bmkeg.lapdf.extraction.exceptions.ClassificationException;
import edu.isi.bmkeg.lapdf.features.ChunkFeatures;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.factory.AbstractModelFactory;
import edu.isi.bmkeg.utils.Converters;
/**
 * Rule based classification of blocks using drools. 
 * @author cartic
 *
 */
public class RuleBasedChunkClassifier implements Classifier<ChunkBlock> {
	
	private static Logger logger = Logger.getLogger(RuleBasedChunkClassifier.class);

	private StatelessKnowledgeSession kSession;
	
	private AbstractModelFactory modelFactory;
	
	private KnowledgeBase kbase;
	
	private void reportCompiledRules(String droolsFileName, 
			DecisionTableConfiguration dtableconfiguration) throws IOException  {
		
		String rules = DecisionTableFactory.loadFromInputStream(ResourceFactory.newFileResource(droolsFileName).getInputStream(), dtableconfiguration);
		logger.info( "GENERATED RULE FILE FROM SPREADSHEET:\n" + rules);
		
	}
	
	public RuleBasedChunkClassifier(String droolsFileName,
			AbstractModelFactory modelFactory) throws IOException, ClassificationException  {

		// Workaround for JBRULES-3163
		Properties properties = new Properties();
		properties.setProperty( "drools.dialect.java.compiler.lnglevel", "1.6" );
		PackageBuilderConfiguration cfg = new PackageBuilderConfiguration( properties );
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder( cfg );
		kbase = KnowledgeBaseFactory.newKnowledgeBase();
		
		File expandedFile = null;
		if( droolsFileName.contains(".jar!") || droolsFileName.contains(".zip!") ) {
			File fileInArchive = new File(droolsFileName);
			expandedFile = Converters.retrieveFileFromArchive(fileInArchive);
			droolsFileName = expandedFile.getPath();
		}
			
		if(droolsFileName.endsWith(".csv")) {
			
			DecisionTableConfiguration dtableconfiguration =
				KnowledgeBuilderFactory.newDecisionTableConfiguration();
			
			dtableconfiguration.setInputType( DecisionTableInputType.CSV );
			
			Resource xlsRes = ResourceFactory.newFileResource( droolsFileName );
			
			kbuilder.add( xlsRes, ResourceType.DTABLE, dtableconfiguration);
			
			reportCompiledRules(droolsFileName,dtableconfiguration);
		
		} else if(droolsFileName.endsWith(".xls")) {
		
			DecisionTableConfiguration dtableconfiguration =
				KnowledgeBuilderFactory.newDecisionTableConfiguration();
			
			dtableconfiguration.setInputType( DecisionTableInputType.XLS );
			
			Resource xlsRes = ResourceFactory.newFileResource( droolsFileName );
			
			kbuilder.add( xlsRes, ResourceType.DTABLE, dtableconfiguration);
			
			reportCompiledRules(droolsFileName,dtableconfiguration);
		
		} else if( droolsFileName.endsWith(".drl")) {
			
			kbuilder.add(ResourceFactory.newFileResource(droolsFileName),
					ResourceType.DRL);
			
		}

		if (kbuilder.hasErrors()) {

			if( expandedFile != null ) {
				Converters.recursivelyDeleteFiles(expandedFile.getParentFile());
			}

			throw new ClassificationException("Error in DROOLS run"+ kbuilder.getErrors());
			
		}
		
		ArrayList<KnowledgePackage> kpkgs = new ArrayList<KnowledgePackage>(
				kbuilder.getKnowledgePackages());
		
		kbase.addKnowledgePackages(kpkgs);
		
		this.modelFactory = modelFactory;
		
		
	
	}

	@Override
	public void classify(List<ChunkBlock> blockList) {
		
		this.kSession = kbase.newStatelessKnowledgeSession();
		for (ChunkBlock chunk : blockList) {
			kSession.setGlobal("chunk", chunk);
			kSession.execute(new ChunkFeatures(chunk, modelFactory));
		}
		this.kSession = null;

	}

}
