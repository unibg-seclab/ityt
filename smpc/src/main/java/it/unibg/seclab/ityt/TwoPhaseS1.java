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
import dk.alexandra.fresco.lib.common.collections.io.OpenList;
import dk.alexandra.fresco.lib.common.math.polynomial.PolynomialImpl;
import dk.alexandra.fresco.lib.common.math.polynomial.evaluator.PolynomialEvaluator;

import dk.alexandra.fresco.lib.debug.NumericMarker;

import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;

import java.io.IOException;
import java.io.PrintStream;
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

/**
 * First step n-to-n sMPC
 */
public class TwoPhaseS1 implements Application<TwoPhaseS1Result, ProtocolBuilderNumeric> {

    private static Logger log = LoggerFactory.getLogger(TwoPhaseS1.class);

    private int id, n, k;
    private BigInteger seed; // the key in case the owner is executing the protocol

    // instance of the protocol as seen by a party
    public TwoPhaseS1(int id, int n, int k, BigInteger seed) {
	this.id = id;
	this.n = n;
	this.k = k;
	this.seed = seed;
    }

    @Override
    public DRes<TwoPhaseS1Result> buildComputation(ProtocolBuilderNumeric producer) {
	PrintStream outstream = new PrintStream(System.out);
	BigInteger modulus = producer.getBasicNumericContext().getModulus();

	return producer
	    .par(prod -> {
		    prod.seq(new NumericMarker("[*] Step 1.1 - reading inputs and rnd polynomial coefficients...", outstream));
		    TwoPhaseS1Data data = new TwoPhaseS1Data();
		    // select k coefficients (the first k for simplicity)
		    data.setCoefficients(IntStream.rangeClosed(1, this.k) // server dir numbers, 1 is for the owner
					 .mapToObj((id) -> prod.numeric().input(this.seed, id))
					 .collect(Collectors.toList()));
		    // generate n random x values
		    data.setXs(IntStream.rangeClosed(1, this.n-1)
			       .mapToObj(list -> prod.numeric().randomElement())			       
			       .collect(Collectors.toList()));
		    return () -> data;
		}) // the input is ready
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 1.2 - evaluating f(x) ...", outstream));
		    PolynomialImpl polynomial = new PolynomialImpl(data.getCoefficients());
		    data.setYs(data.getXs().stream()
			       .map(x -> prod.seq(new PolynomialEvaluator(x, polynomial)))
			       .collect(Collectors.toList()));
		    return () -> data;
		}) // the ys have been computed
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 1.3 - opening intermediate values to the parties ...", outstream));
		    TwoPhaseS1Result result = new TwoPhaseS1Result();
		    result.setSecretCoefficients(data.getCoefficients().stream()
						 .map(coeff -> prod.numeric().open(coeff, 1)).collect(Collectors.toList()));
		    result.setSecretXs(IntStream.rangeClosed(2, this.n)
				       .mapToObj((id) -> prod.numeric().open(data.getXs().get(id - 2), id))
				       .collect(Collectors.toList()));
		    result.setSecretYs(IntStream.rangeClosed(2, this.n)
				       .mapToObj((id) -> prod.numeric().open(data.getYs().get(id - 2), id))
				       .collect(Collectors.toList()));
		    return () -> result;
		}); // first step result opened to the parties
    }
     
    /**
     * Main method for TwoPhaseS1.
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
	TwoPhaseS1 app;

	app = new TwoPhaseS1(id, n, k, seed);
	SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
	ResourcePoolT resourcePool = cmdUtil.getResourcePool();
	TwoPhaseS1Result result = sce.runApplication(app, resourcePool, cmdUtil.getNetwork());

	// print data to log
	log.info(result.toString());

	cmdUtil.closeNetwork();
	sce.shutdownSCE();
    }

}
