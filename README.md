# Splicing calculator
Calculate splice donor and acceptor scores for all exons of the genome.

Pre-built TSV tables for *hg38* can be downloaded from [here](https://s3-eu-west-1.amazonaws.com/danisd/SplicingCalculatorData.zip).

## How to compile

Splicing calculator is a *Maven* project:

```bash
cd SplicingCalculator
mvn clean package
```

`JAR` file and distribution `ZIP` will be create in `target/` folder after successful compilation.

## How to run
In order to run, Splicing calculator needs following resources:

- `jannovar.cache.file` - path to Jannovar transcript database. The database must be created by Jannovar version `v0.26` and the reference genome should match one used in `ref.genome.fasta.file`

- `ref.genome.fasta.file` - path to indexed FASTA file containing all chromosomes of the genome build

- `main.output.file` - path to file where the results will be written. The results will be g-zipped if the path ends with `.gz`

Paths to resource files can be set in the `application.properties` file, that is located in the app's classpath.

### Prepare resources

**Prepare reference genome**
Splicing calculator requires single indexed FASTA file containing all chromosomes of the reference genome. Follow these steps to prepare FASTA file for *hg38*.

```bash
# download hg38 from UCSC
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg38/bigZips/hg38.chromFa.tar.gz
# unpack
tar xvzf hg38.chromFa.tar.gz
# concatenate into a single FASTA file
for k in $(ls chroms/*.fa); do cat $k >> hg38.fa; done
# index with samtools
samtools faidx hg38.fa
```

**Download Jannovar transcript database (cache)**
You have to install Jannovar and follow instructions in the [Jannovar manual](https://doc-openbio.readthedocs.io/projects/jannovar/en/v0.26/download.html).

------

After resources are prepared and updated in `application` properties, the app is **run** by:

```bash
java -jar target/SplicingCalculator-1.0.1.jar --spring.config.location=target/classes/application.properties calculate
```
Alternatively, you can provide the paths to resources directly

```bash
java -jar target/SplicingCalculator-1.0.1.jar --jannovar.cache.path=/path/to/cache.ser --ref.genome.fasta.file=/path/to/hg38.fa --main.output.file=hg38_out.tsv
```

## Use with tabix

The TSV file can be used with tabix, if required:

```bash
cat hg38_out.tsv | grep -v "^#" | sort -k1,1 -k2n,2 -k3n,3 | bgzip -c > hg38_out.tsv.gz && tabix -p bed hg38_out.tsv.gz
```


