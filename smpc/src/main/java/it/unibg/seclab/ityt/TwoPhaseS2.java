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
 * Second step 1-to-1 sMPC
 */
public class TwoPhaseS2 implements Application<TwoPhaseS2Result, ProtocolBuilderNumeric> {

    private static Logger log = LoggerFactory.getLogger(TwoPhaseS2.class);

    private int id, k; // participant id and polynomial degree
    private List<BigInteger> coeffs; // polynomial coefficients
    private BigInteger x, y, salt, key, Ck, Cs; // share, salt, key, commitment of the key and of the share

    /**
     * Construct a new TwoPhaseS2 for the Owner
     */
    public TwoPhaseS2(int id, List<BigInteger> coeffs) {
	this.id = id;
	this.k = coeffs.size() - 1;
	this.coeffs = coeffs;
	this.x = null;
	this.y = null;
	this.key = coeffs.get(0);
	this.Ck = null;
	this.Cs = null;
	this.salt = null;
    }

    /**
     * Construct a new TwoPhaseS2 for the Shareholder
     */
    public TwoPhaseS2(int id, int k, BigInteger x, BigInteger y, BigInteger salt) {
	this.id = id;
	this.k = k;
	this.coeffs = Collections.nCopies(this.k, (BigInteger) null);
	this.x = x;
	this.y = y;
	this.key = null;
	this.Ck = null;
	this.Cs = null;
	this.salt = salt;
    }

    @Override
    public DRes<TwoPhaseS2Result> buildComputation(ProtocolBuilderNumeric producer) {
	PrintStream outstream = new PrintStream(System.out);

	return producer
	    .par(prod -> {
		    prod.seq(new NumericMarker("[*] Step 2.1 - reading input ...", outstream));
		    TwoPhaseS2Data data = new TwoPhaseS2Data();
		    data.setCoefficients(this.coeffs.stream()
					 .map((n) -> prod.numeric().input(n, 1))
					 .collect(Collectors.toList()));
		    data.setKey(prod.numeric().input(this.key, 1));
		    data.setX(prod.numeric().input(this.x, 2));
		    data.setY(prod.numeric().input(this.y, 2));
		    data.setSalt(prod.numeric().input(this.salt, 2));
		    return () -> data;
		}) // input red
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 2.2 - retrieving f() and evaluating f(x) ...", outstream));
		    PolynomialImpl polynomial = new PolynomialImpl(data.getCoefficients());
		    data.setYHat(prod.seq(new PolynomialEvaluator(data.getX(), polynomial)));
		    return () -> data;
		}) // polynomial evaluated
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 2.3 - computing commitments using MiMC ...", outstream));
		    // the secret is used as the key
		    data.setCx(prod.seq(new MiMCEncryption(data.getX(), data.getSalt())));
		    data.setCy(prod.seq(new MiMCEncryption(data.getY(), data.getSalt())));		    
		    data.setCk(prod.seq(new MiMCEncryption(data.getKey(), data.getKey())));
		    return () -> data;
		}) // building the commitments
	    .par((prod, data) -> {
		    prod.seq(new NumericMarker("[*] Step 2.4 - opening results to parties ...", outstream));
		    TwoPhaseS2Result result = new TwoPhaseS2Result();
		    result.set_cheat(prod.numeric().open(prod.numeric().sub(data.getY(), data.getYHat())));
		    result.set_Ck(prod.numeric().open(data.getCk()));
		    result.set_Cx(prod.numeric().open(data.getCx()));		    
		    result.set_Cy(prod.numeric().open(data.getCy()));
		    return () -> result;
		}); // opening the commitments to the 
    }
    
    /**
     * Main method for TwoPhaseS2.
     * @param args Arguments for the application
     * @throws IOException In case of network problems
     */
    public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
	CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();

	// Owner inputs coefficients of f(x), receives the commitment of the share and of the key
	// Shareholder inputs its share, receives its share commitment plus the commitment of the key
    
	cmdUtil.addOption(Option.builder("coeffs").desc("The polynomial coefficients. "
						   + "Only party 1 has to supply this input").hasArgs().build());
	cmdUtil.addOption(Option.builder("degree").desc("The degree of the polynomial. "
						   + "Only party 2 has to supply this input").hasArg().build());
	cmdUtil.addOption(Option.builder("xval").desc("The x coordinate. "
						   + "Only party 2 has to supply this input").hasArg().build());
	cmdUtil.addOption(Option.builder("yval").desc("The previously evaluated y coordinate. "
						   + "Only party 2 has to supply this input").hasArg().build());
	cmdUtil.addOption(Option.builder("salt").desc("A salt used to produce the commitment. "
						   + "Only party 2 has to supply this input").hasArg().build());

	CommandLine cmd = cmdUtil.parse(args);
	NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
	int id = networkConfiguration.getMyId();

	TwoPhaseS2 app;

	// Party 1 has to supply the coeffs
	if (id == 1) {
	    if (!cmd.hasOption("coeffs")) {
		throw new IllegalArgumentException("Missing input from Party 1.");
	    }
	    if (cmd.hasOption("salt")) {
		throw new IllegalArgumentException("Party 1 doesn't need to provide the salt.");
	    }
	    // reading Party 1 input
	    List<BigInteger> coeffs = Arrays.stream(cmd.getOptionValues("coeffs"))
		.map(n -> new BigInteger(n)).collect(Collectors.toList());
	    
	    app = new TwoPhaseS2(id, coeffs);

	} else {
	    if (!cmd.hasOption("degree") || !cmd.hasOption("xval") || !cmd.hasOption("yval") || !cmd.hasOption("salt")) {
		throw new IllegalArgumentException("Missing input from Party 2.");
	    }
	    // reading Party 2 input
	    int k = Integer.parseInt(cmd.getOptionValue("degree"));
	    BigInteger x = new BigInteger(cmd.getOptionValue("xval"));
	    BigInteger y = new BigInteger(cmd.getOptionValue("yval"));
	    BigInteger salt = new BigInteger(cmd.getOptionValue("salt"));	    

	    app = new TwoPhaseS2(id, k, x, y, salt);
	}

	SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
	ResourcePoolT resourcePool = cmdUtil.getResourcePool();
	TwoPhaseS2Result result = sce.runApplication(app, resourcePool, cmdUtil.getNetwork());

	if (result.detectCheating()){
	    log.error("[E] Cheating detected!");
	    throw new IOException("Protocol failed");
	}
	log.info(result.toString());

	cmdUtil.closeNetwork();
	sce.shutdownSCE();
    }
}
