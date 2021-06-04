package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.mimc.MiMCEncryption;
import dk.alexandra.fresco.lib.common.collections.io.OpenList;
import dk.alexandra.fresco.lib.common.math.polynomial.PolynomialImpl;
import dk.alexandra.fresco.lib.common.math.polynomial.evaluator.PolynomialEvaluator;
import dk.alexandra.fresco.lib.debug.NumericMarker;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import java.io.PrintStream;

/**
 * SinglePhase version
 */
public class SinglePhase implements Application<SinglePhaseResult, ProtocolBuilderNumeric> {

    private static Logger log = LoggerFactory.getLogger(SinglePhase.class);

    // required input
    private int id, n, k;
    private BigInteger seed, salt; // the key in case the owner is executing the protocol

    /**
     * Construct a new SinglePhase for the Owner
     */
    public SinglePhase(int id, int n, int k, BigInteger seed) {
	this.id = id;
	this.n = n;
	this.k = k;
	this.seed = seed;
    }

    /**
     * Construct a new SinglePhase for the Shareholder
     */
    public SinglePhase(int id, int n, int k, BigInteger seed, BigInteger salt) {
	this.id = id;
	this.n = n;
	this.k = k;
	this.seed = seed;
	this.salt = salt;
    }
    

    @Override
    public DRes<SinglePhaseResult> buildComputation(ProtocolBuilderNumeric producer) {
	PrintStream outstream = new PrintStream(System.out);
	
	return producer
	    .par(prod -> {
		    prod.seq(new NumericMarker("[*] Step 1.1 - reading inputs and rnd polynomial coefficients...", outstream));
		    SinglePhaseData data = new SinglePhaseData();
		    data.setCoeffs(IntStream.rangeClosed(1, this.k)
					 .mapToObj((id) -> prod.numeric().input(this.seed, id))
					 .collect(Collectors.toList()));
		    // generate n random x values
		    data.setXs(IntStream.rangeClosed(1, this.n-1)
			       .mapToObj(list -> prod.numeric().randomElement())
			       .collect(Collectors.toList()));
		    data.setSalts(IntStream.rangeClosed(2, this.n)
				  .mapToObj((id) -> prod.numeric().input(this.salt, id))
				  .collect(Collectors.toList()));
		    return () -> data;
		}) // the input is ready	    
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 1.2 - evaluating f(x) ...", outstream));
		    PolynomialImpl polynomial = new PolynomialImpl(data.getCoeffs());
		    data.setYs(data.getXs().stream()
			       .map(x -> prod.seq(new PolynomialEvaluator(x, polynomial)))
			       .collect(Collectors.toList()));
		    return () -> data;
		}) // the ys have been computed
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 1.3 - computing commitments using MiMC ...", outstream));
		    // encrypting the shares
		    data.setXCs(IntStream.rangeClosed(2, this.n)
				.mapToObj((id) -> prod.seq(new MiMCEncryption(data.getXs().get(id-2), data.getSalts().get(id-2))))
				 .collect(Collectors.toList()));
		    data.setYCs(IntStream.rangeClosed(2, this.n)
				.mapToObj((id) -> prod.seq(new MiMCEncryption(data.getYs().get(id-2), data.getSalts().get(id-2))))
				 .collect(Collectors.toList()));
		    // encrypting the key
		    data.setCk(prod.seq(new MiMCEncryption(data.getKey(), data.getKey())));
		    return () -> data;
		}) // building the commitments
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 1.4 - opening results to parties ...", outstream));
		    SinglePhaseResult result = new SinglePhaseResult();
		    // for the shareholders
		    result.setXs(IntStream.rangeClosed(2, this.n)
				 .mapToObj((id) -> prod.numeric().open(data.getXs().get(id - 2), id))
				 .collect(Collectors.toList()));
		    result.setYs(IntStream.rangeClosed(2, this.n)
				 .mapToObj((id) -> prod.numeric().open(data.getYs().get(id - 2), id))
				 .collect(Collectors.toList()));
		    // for the owner
		    result.setXCs(IntStream.rangeClosed(2, this.n)
				 .mapToObj((id) -> prod.numeric().open(data.getXCs().get(id - 2), 1))
				 .collect(Collectors.toList()));
		    result.setYCs(IntStream.rangeClosed(2, this.n)
				 .mapToObj((id) -> prod.numeric().open(data.getYCs().get(id - 2), 1))
				 .collect(Collectors.toList()));
		    // for every party
		    result.setK(this.k);
		    result.setN(this.n);
		    result.setCk(prod.numeric().open(data.getCk()));
		    return () -> result;
		}); // opening results to the parties
    }
    
    /**
     * Main method for SinglePhase.
     * @param args Arguments for the application
     * @throws IOException In case of network problems
     */
    public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
	CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();

	cmdUtil.addOption(Option.builder("n").desc("The nof parties."
						   + "Every party has to supply this input").hasArg().build());
	cmdUtil.addOption(Option.builder("k").desc("The degree of the polynomial to generate. "
						   + "Every party has to supply this input").hasArg().build());
	cmdUtil.addOption(Option.builder("seed").desc("The integer key/seed coordinate of this party. "
						      + "Every party has to supply this input").hasArg().build());
	cmdUtil.addOption(Option.builder("salt").desc("A salt used to produce the commitment. "
						      + "Only the Shareholder has to supply this input").hasArg().build());
	

	CommandLine cmd = cmdUtil.parse(args);
	NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
	int id = networkConfiguration.getMyId();
	int parties = networkConfiguration.noOfParties();

	// Every party has to supply the n
	if (!cmd.hasOption("n")) {
	    throw new IllegalArgumentException("Every party has to supply n (the nof parties).");
	}
	// Every party has to supply the k
	if (!cmd.hasOption("k")) {
	    throw new IllegalArgumentException("Every party has to supply the k (the degree).");
	}
	// Every party has to supply the seed
	if (!cmd.hasOption("seed")) {
	    throw new IllegalArgumentException("Every party has to supply the seed (alias the key for the Owner).");
	}

	

	int n = Integer.parseInt(cmd.getOptionValue("n"));	 
	int k = Integer.parseInt(cmd.getOptionValue("k"));
	BigInteger seed = BigInteger.valueOf(Integer.parseInt(cmd.getOptionValue("seed")));
	SinglePhase app;
	
	app = new SinglePhase(id, n, k, seed);	    
	if ( id != 1){
	    // Every party has to supply the seed
	    if (!cmd.hasOption("salt")) {
		throw new IllegalArgumentException("Missing salt from Shareholder!");
	    }
	    BigInteger salt = BigInteger.valueOf(Integer.parseInt(cmd.getOptionValue("salt")));
	    app = new SinglePhase(id, n, k, seed, salt);
	}

	SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
	ResourcePoolT resourcePool = cmdUtil.getResourcePool();
	SinglePhaseResult result = sce.runApplication(app, resourcePool, cmdUtil.getNetwork());

	// print data to log
	log.info(result.toString());

	cmdUtil.closeNetwork();
	sce.shutdownSCE();	
  }

}
