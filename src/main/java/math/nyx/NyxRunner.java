package math.nyx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import math.nyx.report.FractalCodecReport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Main class used to control encode, decode and generate reports via the CLI.
 *
 * @author jwhite
 */
public class NyxRunner {
	private static Logger logger = LogManager.getLogger("Nyx");

	public static final Integer[] SCALE_VALUES = new Integer[] { 1 };

	@Autowired
	private Nyx nyx;

	@Argument(usage="input files")
    private List<String> fileNames = new ArrayList<String>(0);

	@Option(name="-f", usage="force encode")
	private Boolean forceEncode = false;

	@Option(name="-o", usage="path to output folder")
    private String pathToOutputFolder;

	@Option(name="-s", usage="additional scales used to decode the signal")
    private String additionalScales;

	private List<File> sourceFiles = new ArrayList<File>(0);

	private File outputFolder = Paths.get("").toFile().getAbsoluteFile();

	private Set<Integer> scales = new HashSet<Integer>(Arrays.asList(SCALE_VALUES));

	public List<FractalCodecReport> doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);

            if ( fileNames.isEmpty() )
                throw new CmdLineException(parser, "No argument is given");
            
            for ( String fileName : fileNames ) {
            	File file = new File(fileName);
            	if (!file.canRead())
            		throw new CmdLineException(parser, "Cannot read the input file: " + file);
            	sourceFiles.add(file);
            }

            if (!StringUtils.isEmpty(additionalScales)) {
            	for (String scaleAsString : additionalScales.split(",")) {
            		int scale = new Integer(scaleAsString);
                	if (scale < 2)
                		throw new CmdLineException(parser, "Additional scales must be greater than 1");
                	scales.add(scale);
            	}
            }

            if (!StringUtils.isEmpty(pathToOutputFolder))
            	outputFolder = new File(pathToOutputFolder).getAbsoluteFile();

            if (!outputFolder.exists() && !outputFolder.mkdir())
            	throw new CmdLineException(parser, "Could not create the output folder: " + outputFolder);

            if (!outputFolder.canWrite())
            	throw new CmdLineException(parser, "Cannot write to the output folder: " + outputFolder);

        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java Nyx [options...] files...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java Nyx -f -s 1,2,4 -o ../target/ lena.png img2.jpg");
            return new ArrayList<FractalCodecReport>(0);
        }

        return generateAndSaveCodecReports(fileNames);
    }

	public List<FractalCodecReport> generateAndSaveCodecReports(List<String> fileNames) throws IOException {
        List<FractalCodecReport> reports = new ArrayList<FractalCodecReport>();
        for( String fileName : fileNames ) {
        	File sourceFile = new File(fileName);
        	logger.info("Generating encode/decode report for {}.", sourceFile);
        	FractalCodecReport report = nyx.encodeDecode(sourceFile, scales, outputFolder, forceEncode);
        	reports.add(report);

        	File reportFile = new File(outputFolder, String.format("%s-report.tex", sourceFile.getName()));
        	logger.info("Saving report to {}.", reportFile);
        	report.save(reportFile);
        }

        return reports;
	}

	public static void main(String[] args) throws IOException {
		NyxRunner nyx = new NyxRunner();
	
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
		beanFactory.autowireBean(nyx);

		nyx.doMain(args);
		
		context.close();
	}
}

