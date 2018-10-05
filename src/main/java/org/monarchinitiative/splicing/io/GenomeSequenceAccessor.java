package org.monarchinitiative.splicing.io;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class allows to fetch arbitrary nucleotide sequence from the reference genome. To do so it requires single
 * Fasta file that contains all contigs. Fasta index (*.fai) is required to be present in the same directory. The
 * index can be created using command <code>samtools faidx file.fa</code> from the <code>samtools</code> suite.
 * <p>
 * Chromosome names from UCSC are prefixed <code>'chr'</code>, while chromosomes from ENSEMBL are not. This class is able
 * to fetch sequence from the ENSEMBL build.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class GenomeSequenceAccessor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenomeSequenceAccessor.class);

    private final IndexedFastaSequenceFile fasta;

    /**
     * Prefix 'chr' will be prepended to chromosome String of each query in the {@link #fetchSequence(String, int, int)}
     * method.
     */
    private final boolean usePrefix;

    /**
     * Mitochondrial chromosome will be referred to as 'chrM', even if 'chrMT' is present in query.
     */
    private final boolean useM;

    private final File fastaPath;


    private static boolean figureOutPrefix(SAMSequenceDictionary sequenceDictionary) {
        boolean usePrefix = sequenceDictionary.getSequences().stream().allMatch(sr -> sr.getSequenceName().startsWith("chr"));
        boolean doNotUsePrefix = sequenceDictionary.getSequences().stream().noneMatch(sr -> sr.getSequenceName().startsWith("chr"));
        if (!usePrefix && !doNotUsePrefix) {
            String msg = String.format("Sequence dictionary contains entries both prefixed with 'chr' and not prefixed.\n'%s'",
                    sequenceDictionary.getSequences().stream().map(SAMSequenceRecord::getSequenceName).collect(Collectors.joining(",")));
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        return usePrefix;
    }


    private static boolean figureOutChrM(SAMSequenceDictionary sequenceDictionary) {
        boolean usesM = sequenceDictionary.getSequences().stream().anyMatch(sr -> sr.getSequenceName().equals("chrM") || sr.getSequenceName().equals("M"));
        boolean usesMT = sequenceDictionary.getSequences().stream().anyMatch(sr -> sr.getSequenceName().equals("chrMT") || sr.getSequenceName().equals("MT"));
        if (!usesM && !usesMT) {
            String msg = String.format("The FASTA file does not contain entry for mitochondrial DNA\n'%s'",
                    sequenceDictionary.getSequences().stream().map(SAMSequenceRecord::getSequenceName).collect(Collectors.joining(",")));
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        return usesM;
    }


    /**
     * Create an instance using FASTA file on provided <code>fastaPath</code>.
     *
     * @param fastaPath path to FASTA file. FASTA index is expected to be in the same directory with the same
     *                  basename as the FASTA file + ".fai" suffix
     */
    public GenomeSequenceAccessor(File fastaPath) {
        this(fastaPath, new File(fastaPath.getAbsolutePath() + ".fai"));
    }


    /**
     * Create an instance using provided FASTA file and index.
     *
     * @param fastaPath path to indexed FASTA file
     * @param indexPath path to FASTA index
     */
    public GenomeSequenceAccessor(File fastaPath, File indexPath) {
        this.fastaPath = fastaPath;
        FastaSequenceIndex fastaIndex = new FastaSequenceIndex(indexPath);
        fasta = new IndexedFastaSequenceFile(fastaPath, fastaIndex);
        SAMSequenceDictionary sequenceDictionary = fasta.getSequenceDictionary();
        if (sequenceDictionary == null) {
            File sdict = new File(fastaPath.getParent(), fastaPath.getName().replace(".fa", ".dict"));
            LOGGER.warn("Sequence dictionary {} is not present for fasta {}", sdict.getAbsolutePath(), fastaPath.getAbsolutePath());
            throw new RuntimeException("");
        }
        usePrefix = figureOutPrefix(fasta.getSequenceDictionary());
        useM = figureOutChrM(fasta.getSequenceDictionary());
    }


    /**
     * Get sequence of nucleotides from given position specified by chromosome/contig name, starting position and ending
     * position. Case of nucleotides is not changed.
     * <p>
     * Querying with negative coordinates does not raise an exception, querying with e.g.
     * <code>fetchSequence("chr8", -6, -1)</code> returns ">chr8". However it does have any sense to do it.
     * <p>
     * Chromosomes from <em>ENSEMBL</em> genome build do not use prefix <em>'chr'</em>, while UCSC uses the prefix.
     * This method tries to retrieve the sequence even if <code>chr</code> starts with <em>'chr'</em> and chromosomes are
     * not prefixed (and vice versa).
     * <p>
     * Moreover, Reference dictionaries
     *
     * @param chr   chromosome name
     * @param start start position using 0-based numbering (exclusive)
     * @param end   end chromosomal position using 0-based numbering (inclusive)
     * @return nucleotide sequence or <code>null</code> if coordinates ask for a region beyond the end of the chromosome
     * or if the chromosome is not present in the FASTA file
     */
    public String fetchSequence(String chr, int start, int end) {
        // deal with the 'chr' prefix issue
        String chrom = usePrefix
                ? chr.startsWith("chr") ? chr : "chr" + chr // add prefix, if necessary
                : chr.startsWith("chr") ? chr.substring(3) : chr; // remove prefix, if necessary

        // deal with the chrM vs. chrMT issue
        if (chrom.matches("(chr)?M(T)?")) { // query involves mitochondrial chromosome
            if (useM) {
                // fix if we have MT and we should have M
                if (chrom.contains("MT"))
                    chrom = chrom.replace("MT", "M");
            } else {
                // fix if we have M and we should have MT
                if (!chrom.contains("MT"))
                    chrom = chrom.replace("M", "MT");
            }
        }

        try {
            ReferenceSequence referenceSequence = fasta.getSubsequenceAt(chrom, start + 1, end);
            return new String(referenceSequence.getBases());
        } catch (SAMException e) { // start or end position is beyond the end of contig, chromosome is not present in the FASTA file
            LOGGER.warn("Error fetching sequence for '{}:{}-{}'", chr, start, end);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        LOGGER.debug("Closing fasta file {}", fastaPath.getAbsolutePath());
        this.fasta.close();
    }
}
