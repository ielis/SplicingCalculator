package org.monarchinitiative.splicing.analyze_selected_exons;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.splicing.calculate.SplicingInformationContentAnnotator;
import org.monarchinitiative.splicing.io.GenomeSequenceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class AnalyzeSelectedExonsApplicationRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeSelectedExonsApplicationRunner.class);

    private final GenomeSequenceAccessor sequenceAccessor;

    private final SplicingInformationContentAnnotator splicingInformationContentAnnotator;

    private final JannovarData jannovarData;

    public AnalyzeSelectedExonsApplicationRunner(GenomeSequenceAccessor sequenceAccessor,
                                                 SplicingInformationContentAnnotator splicingInformationContentAnnotator,
                                                 JannovarData jannovarData) {
        this.sequenceAccessor = sequenceAccessor;
        this.splicingInformationContentAnnotator = splicingInformationContentAnnotator;
        this.jannovarData = jannovarData;
    }

    private static String reverseComplement(String seq) {
        char[] oldSeq = seq.toCharArray();
        char[] newSeq = new char[oldSeq.length];
        int idx = oldSeq.length - 1;
        for (int i = 0; i < oldSeq.length; i++) {
            if (oldSeq[i] == 'A') {
                newSeq[idx - i] = 'T';
            } else if (oldSeq[i] == 'a') {
                newSeq[idx - i] = 't';
            } else if (oldSeq[i] == 'T') {
                newSeq[idx - i] = 'A';
            } else if (oldSeq[i] == 't') {
                newSeq[idx - i] = 'a';
            } else if (oldSeq[i] == 'C') {
                newSeq[idx - i] = 'G';
            } else if (oldSeq[i] == 'c') {
                newSeq[idx - i] = 'g';
            } else if (oldSeq[i] == 'G') {
                newSeq[idx - i] = 'C';
            } else if (oldSeq[i] == 'g') {
                newSeq[idx - i] = 'c';
            } else if (oldSeq[i] == 'N') {
                newSeq[idx - i] = 'N';
            } else if (oldSeq[i] == 'n') {
                newSeq[idx - i] = 'n';
            } else throw new IllegalArgumentException(String.format("Illegal nucleotide %s in sequence %s",
                    oldSeq[i], seq));
        }
        return new String(newSeq);
    }

    /**
     * Run this code using <code>java -jar target/SplicingCalculator-1.0.2.jar --spring.config.location=src/main/resources/application.properties
     * analyze_selected_exons --input=/home/ielis/dwn/fromGTF.SE.tsv --output=analysis_of_given_exons.tsv</code>
     * @param args application args
     * @throws Exception if troubles
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            final List<String> nonOptionArgs = args.getNonOptionArgs();
            if (!nonOptionArgs.contains("analyze_selected_exons")) { // this runner is run only if cmdline contains this token
                return;
            }

            // we need paths to the input and output files
            Path inputPath;
            if (!args.containsOption("input")) {
                LOGGER.warn("Please provide path to input file");
                return;
            }
            inputPath = Paths.get(args.getOptionValues("input").get(0));
            Path outputPath;
            if (!args.containsOption("output")) {
                LOGGER.warn("Please provide path to output file");
                return;
            }
            outputPath = Paths.get(args.getOptionValues("output").get(0));

            LOGGER.info("Reading data from '{}'", inputPath);
            LOGGER.info("Writing results to '{}'", outputPath);

            try (BufferedReader reader = Files.newBufferedReader(inputPath);
                 BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                ReferenceDictionary rd = jannovarData.getRefDict();

                String header = reader.readLine();
                header = header + "\t" + "donorIC" + "\t" + "acceptorIC";
                writer.write(header);
                writer.newLine();

                String line;
                while ((line = reader.readLine()) != null) {
                    final String[] tokens = line.split("\t");
                    String contig = tokens[3];
                    Strand strand = tokens[4].equals("+") ? Strand.FWD : Strand.REV;
                    int begin = Integer.parseInt(tokens[5]); // 0-based (excluded) coordinate on FWD string
                    int end = Integer.parseInt(tokens[6]); // 0-based (included) coordinate on FWD string

                    GenomeInterval exon = new GenomeInterval(rd, Strand.FWD, rd.getContigNameToID().get(contig), begin, end).withStrand(strand);
                    GenomeInterval donor = new GenomeInterval(exon.getGenomeEndPos().shifted(-3), 9);
                    String donorSeq;
                    if (strand.isForward()) {
                        donorSeq = sequenceAccessor.fetchSequence(contig,
                                donor.getBeginPos(),
                                donor.getEndPos());
                    } else {
                        donorSeq = sequenceAccessor.fetchSequence(contig,
                                donor.getGenomeEndPos().withStrand(Strand.FWD).getPos() + 1,
                                donor.getGenomeBeginPos().withStrand(Strand.FWD).getPos() + 1);
                        donorSeq = reverseComplement(donorSeq);
                    }
                    final double spliceDonorScore = splicingInformationContentAnnotator.getSpliceDonorScore(donorSeq);


                    GenomeInterval acceptor = new GenomeInterval(exon.getGenomeBeginPos().shifted(-25), 27);
                    String acceptorSeq;
                    if (strand.isForward()) {
                        acceptorSeq = sequenceAccessor.fetchSequence(contig,
                                acceptor.getBeginPos(),
                                acceptor.getEndPos());
                    } else {
                        acceptorSeq = sequenceAccessor.fetchSequence(contig,
                                acceptor.getGenomeEndPos().withStrand(Strand.FWD).getPos() + 1,
                                acceptor.getGenomeBeginPos().withStrand(Strand.FWD).getPos() + 1);
                        acceptorSeq = reverseComplement(acceptorSeq);
                    }
                    final double spliceAcceptorScore = splicingInformationContentAnnotator.getSpliceAcceptorScore(acceptorSeq);

                    writer.write(line + "\t" + spliceDonorScore + "\t" + spliceAcceptorScore);
                    writer.newLine();
                }

            }
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }
}
