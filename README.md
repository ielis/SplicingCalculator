# Splicing calculator
Calculate splice donor and acceptor scores for all exons of the genome.

## How to compile
Splicing calculator is a *Maven* project:

```bash
cd SplicingCalculator
mvn clean package
```

`JAR` file and distribution `ZIP` will be create in `target/` folder after successful compilation.

## How to run
In order to run, Splicing calculator needs following resources which are set in a `*.properties` file:

- `jannovar.cache.file` - path to Jannovar cache. The cache must be created by Jannovar version `v0.26` and the reference genome should match one used in `ref.genome.fasta.file`
- `ref.genome.fasta.file` - path to indexed FASTA file containing all chromosomes of the genome build
- `main.output.file` - path to file where the results will be written. The results will be g-zipped if the path ends with `.gz`

After resources are set, the app is run by:

```bash
java -jar SplicingCalculator-1.0.0.jar --spring.config.location=target/classes/application.properties calculate
```
> Note: you need to use `--spring.config.location` if the `application.properties` is not in the app's classpath.


## Use with tabix
```bash
zcat file.tsv.gz | sort -k1,1 -k2n,2 -k3n,3 | bgzip -c > file.tsv.b.gz && tabix -p bed file.tsv.b.gz
```